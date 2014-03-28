/**
 * 
 */
package newtonpath.sim;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import newtonpath.ui.JKView;


public abstract class SimulatorRunner<E extends Simulator> implements Runnable {
	final static int paintingStepMillisec = 40;

	final private SimulationDisplay displayer;
	final private E simBkg;
	final protected E simCpy;
	final protected E simView;
	final protected JKView<E> view;
	protected volatile boolean running;

	private volatile boolean paused;

	protected volatile long hits, delay, timeCpy;

	private final Timer displayClock;

	public SimulatorRunner(JKView<E> _view, E _sim) {
		this.view = _view;
		this.simView = _sim;
		this.simCpy = copySim(_sim);
		this.simBkg = copySim(_sim);
		this.displayer = new SimulationDisplay();
		this.running = false;
		this.paused = true;
		this.hits = 0;
		this.delay = 0;
		this.displayClock = new Timer(10, this.displayer);
		this.displayClock.setCoalesce(true);
		this.displayClock.setRepeats(true);
	}

	@SuppressWarnings("unchecked")
	private E copySim(E _sim) {
		return (E) _sim.getCopy();
	}

	abstract protected long work(E integ, long _untillTime);

	public void run() {
		this.running = true;
		while (this.running) {
			long timeToShow = 0, timeWait, untillTime;

			synchronized (this) {
				while (this.paused && this.running) {
					try {
						wait();
					} catch (InterruptedException e) {
						kill();
					}
				}
			}

			timeToShow = untillTime = System.currentTimeMillis()
					+ paintingStepMillisec;

			if (this.running) {
				timeToShow = work(this.simBkg, untillTime);
				synchronized (this.simCpy) {
					this.simCpy.setState(this.simBkg);
					this.timeCpy = timeToShow;
				}
			}

			timeWait = timeToShow - System.currentTimeMillis();
			if (timeWait > paintingStepMillisec) {
				timeWait = paintingStepMillisec;
			}

			if (this.running && (timeWait > 0)) {
				synchronized (this) {
					try {
						if (this.running) {
							wait(timeWait);
						}
					} catch (InterruptedException e) {
						kill();
					}
				}
			}
			// SwingUtilities.invokeLater(displayer);
		}
	}

	public void kill() {
		synchronized (this) {
			this.displayClock.stop();
			this.running = false;
			notifyAll();
		}
	}

	public void start() {
		synchronized (this) {
			this.displayClock.start();
			this.paused = false;
			notifyAll();
		}
	}

	public void stop() {
		synchronized (this) {
			this.displayClock.stop();
			this.paused = true;
			notifyAll();
		}
	}

	public boolean isPaused() {
		return this.paused;
	}

	public class SimulationDisplay implements Runnable, ActionListener {
		long lastDisplay = 0;

		public void run() {
			boolean show = false;
			if (SimulatorRunner.this.running) {
				synchronized (SimulatorRunner.this.simCpy) {
					show = (SimulatorRunner.this.timeCpy > this.lastDisplay);
					if (show) {
						double d;
						this.lastDisplay = SimulatorRunner.this.timeCpy;
						SimulatorRunner.this.hits++;
						d = System.currentTimeMillis()
								- SimulatorRunner.this.timeCpy;

						SimulatorRunner.this.simView
								.setState(SimulatorRunner.this.simCpy);

						if (d > 0) {
							SimulatorRunner.this.delay += d;
						}
					}
				}
				if (show) {
					SimulatorRunner.this.simView.postProcess();
					SimulatorRunner.this.view
							.updateView(SimulatorRunner.this.simView);
				}
			}
		}

		public void actionPerformed(ActionEvent e) {
			run();
		}
	}
}