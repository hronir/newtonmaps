/**
 * 
 */
package newtonpath.ui;

import newtonpath.sim.Simulator;

public class SimulatorPlotter<E extends Simulator> implements Runnable {
	final private E simBkg, simCpy;
	final private PlotterView<E> view;

	private volatile boolean alive, running, reload;
	private final double endTime;

	public SimulatorPlotter(PlotterView<E> _view, E _sim, double _time) {
		this.view = _view;
		this.simCpy = makeCopy(_sim);
		this.simCpy.initPos();
		this.simBkg = makeCopy(this.simCpy);
		this.endTime = _time;
		this.alive = true;
		this.reload = this.running = false;
	}

	@SuppressWarnings("unchecked")
	private E makeCopy(E _sim) {
		return (E) _sim.getCopy();
	}

	public void work() {
		E _int = this.simBkg;
		boolean finished = false;
		int iter = 1000000;
		this.view.beforeUpdate(_int);
		try {
			while (this.running && (iter-- > 0)) {
				this.view.updatePoint(_int);
				if (_int.getTime() > this.endTime) {
					double oldStep = _int.getTimeStep();
					_int.setTimeStep(this.endTime - _int.getTime());
					_int.oneStep(Math.abs(_int.getTimeStep()));
					_int.setTimeStep(oldStep);
					stop();
					finished = true;
					break;
				}
				_int.oneStep();
			}
			if (this.running) {
				this.view.updatePoint(_int);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			stop();
		}
		this.view.afterUpdate(_int, finished);
	}

	public void paint() {
		this.running = true;
		work();
	}

	public void run() {
		this.running = true;
		while (this.alive) {
			synchronized (this) {
				while (this.alive && (!this.running) && (!this.reload)) {
					try {
						wait();
					} catch (InterruptedException e) {
						kill();
					}
				}
			}
			synchronized (this) {
				if (this.alive && this.reload) {
					this.running = true;
					this.simBkg.setState(this.simCpy);
				}
			}
			if (this.running) {
				work();
			}
		}
	}

	public void kill() {
		synchronized (this) {
			this.reload = this.alive = this.running = false;
			notifyAll();
		}
	}

	public void stop() {
		synchronized (this) {
			this.reload = this.running = false;
			notifyAll();
		}
	}

	public void restart() {
		synchronized (this) {
			this.reload = true;
			this.running = false;
			notifyAll();
		}
	}
}