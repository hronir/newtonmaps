package newtonpath.sim;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JMenuItem;

import newtonpath.images.Images;
import newtonpath.kepler.events.Event;
import newtonpath.ui.JKView;
import newtonpath.ui.JKViewList;
import newtonpath.ui.NewtonSim;
import newtonpath.ui.PlotterView;
import newtonpath.ui.PlotterViewList;
import newtonpath.ui.SimulatorPlotter;
import newtonpath.ui.widget.AbstractSimpleAction;
import newtonpath.ui.widget.AbstractToggleAction;


public class SimulatorEngine<E extends Simulator> extends JKViewList<E>
		implements JKView<E> {
	private E simView;
	protected SimulatorPlayer<E> runner;

	protected double speed = 1D;
	protected boolean backwards = false;

	PlotterViewList<NewtonSim> plotterList = new PlotterViewList<NewtonSim>();

	SimulatorPlotter<NewtonSim> simPlotter;

	@Override
	public void resetView(E integrator) {
		killRunner();
		this.simView = integrator;
		super.resetView(integrator);
		startPlotter(integrator);
	}

	public AbstractSimpleAction actionSimStart = new AbstractSimpleAction(
			"Start", Images.iconPlay) {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7129382372206561759L;

		public void actionPerformed(java.awt.event.ActionEvent evt) {
			startSimulation();
		}
	};

	public AbstractSimpleAction actionSimStartBack = new AbstractSimpleAction(
			"Start", Images.iconPlayBackwards) {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7129382372206561759L;

		public void actionPerformed(java.awt.event.ActionEvent evt) {
			startSimulation();
		}
	};

	public AbstractSimpleAction actionSimPause = new AbstractSimpleAction(
			"Pause", Images.iconStop) {
		/**
		 * 
		 */
		private static final long serialVersionUID = -6728273933444611340L;

		public void actionPerformed(java.awt.event.ActionEvent evt) {
			stopSimulation();
		}
	};
	public AbstractSimpleAction actionSimReset = new AbstractSimpleAction(
			"Reset", Images.iconRestart) {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8188766382872819982L;

		public void actionPerformed(java.awt.event.ActionEvent evt) {
			resetSimulation(false);
		}
	};

	public AbstractToggleAction actionSimBack = new AbstractToggleAction(
			"Backwards", Images.iconBack) {
		private static final long serialVersionUID = -1980724883816936587L;

		public void actionPerformed(java.awt.event.ActionEvent evt) {
			this.model.setSelected(!this.model.isSelected());
			SimulatorEngine.this.backwards = this.model.isSelected();
			if (SimulatorEngine.this.runner != null
					&& !SimulatorEngine.this.runner.isPaused()) {
				SimulatorEngine.this.runner.startSimulation(
						SimulatorEngine.this.speed,
						SimulatorEngine.this.backwards);
			}
		}
	};

	public AbstractToggleAction actionSimPlayForward = new AbstractToggleAction(
			"Forward", Images.iconPlay) {
		private static final long serialVersionUID = -1980724883816936587L;

		public void actionPerformed(java.awt.event.ActionEvent evt) {
			// this.model.setSelected(true);
			// SimulatorEngine.this.actionSimPlayBackwards.model
			// .setSelected(false);
			// SimulatorEngine.this.actionSimPlayPause.model.setSelected(false);
			SimulatorEngine.this.backwards = false;
			startSimulation();
		}
	};

	public AbstractToggleAction actionSimPlayBackwards = new AbstractToggleAction(
			"Backwards", Images.iconPlayBackwards) {
		private static final long serialVersionUID = -1980724883816936587L;

		public void actionPerformed(java.awt.event.ActionEvent evt) {
			// this.model.setSelected(true);
			// SimulatorEngine.this.actionSimPlayForward.model.setSelected(false);
			// SimulatorEngine.this.actionSimPlayPause.model.setSelected(false);
			SimulatorEngine.this.backwards = true;
			startSimulation();
		}
	};

	public AbstractToggleAction actionSimPlayPause = new AbstractToggleAction(
			"Pause", Images.iconStop, true) {
		private static final long serialVersionUID = -1980724883816936587L;

		public void actionPerformed(java.awt.event.ActionEvent evt) {
			// this.model.setSelected(true);
			// SimulatorEngine.this.actionSimPlayForward.model.setSelected(false);
			// SimulatorEngine.this.actionSimPlayBackwards.model
			// .setSelected(false);
			SimulatorEngine.this.backwards = false;
			stopSimulation();
		}
	};

	public void setSpeed(double _s) {
		this.speed = _s;
		if (this.runner != null && !this.runner.isPaused()) {
			this.runner.startSimulation(this.speed, this.backwards);
		}
	}

	public void goToAbs(double t) throws Exception {
		goTo(t - this.simView.getEpoch());
	}

	public void goToDelta(double t) throws Exception {
		goTo(this.simView.getTime() + t);
	}

	public void goTo(double t) throws Exception {
		SimulatorEngine.this.actionSimPlayForward.model.setSelected(false);
		SimulatorEngine.this.actionSimPlayBackwards.model.setSelected(false);
		SimulatorEngine.this.actionSimPlayPause.model.setSelected(true);
		killRunner();
		this.simView.initPos();
		this.simView.timeIntegration(t);
		super.resetView(this.simView);
		updateView(this.simView);
	}

	private void launchRunner() {
		if (this.runner == null) {
			this.runner = new SimulatorPlayer<E>(SimulatorEngine.this,
					this.simView);
			new Thread(this.runner).start();
		}
	}

	private void killRunner() {
		if (this.runner != null) {
			this.runner.kill();
		}
		this.runner = null;
	}

	public void resetSimulation(boolean _restart) {
		SimulatorEngine.this.actionSimPlayForward.model.setSelected(false);
		SimulatorEngine.this.actionSimPlayBackwards.model.setSelected(false);
		SimulatorEngine.this.actionSimPlayPause.model.setSelected(true);

		killRunner();

		try {
			this.simView.initPos();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		super.resetView(this.simView);

		if (_restart) {
			launchRunner();
			this.runner.startSimulation(this.speed, this.backwards);
		}

	}

	public void setSimulation(E integ) {
		integ.setEpochNow();
		integ.postProcess();
		resetView(integ);
	}

	public void stopSimulation() {
		SimulatorEngine.this.actionSimPlayForward.model.setSelected(false);
		SimulatorEngine.this.actionSimPlayBackwards.model.setSelected(false);
		SimulatorEngine.this.actionSimPlayPause.model.setSelected(true);
		if (this.runner != null) {
			this.runner.pauseSimulation();
		}
	}

	public void startSimulation() {
		SimulatorEngine.this.actionSimPlayForward.model
				.setSelected(!this.backwards);
		SimulatorEngine.this.actionSimPlayBackwards.model
				.setSelected(this.backwards);
		SimulatorEngine.this.actionSimPlayPause.model.setSelected(false);
		launchRunner();
		this.runner.startSimulation(this.speed, this.backwards);
	}

	@Override
	public void prepareContextMenu(Vector<JMenuItem> mnu, Event dynEvt) {
		super.prepareContextMenu(mnu, dynEvt);
		if (dynEvt != null) {
			mnu.add(new JMenuItemSim(dynEvt));
		}
	}

	public class JMenuItemSim extends JMenuItem {
		private static final long serialVersionUID = -7386816601614312972L;
		protected Event event;

		public JMenuItemSim(Event ev) {
			super("Set time to " + ev.getDescription());
			this.event = ev;
			addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					try {
						goToAbs(JMenuItemSim.this.event.getDate());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	public void interUpdate(E integ) {
	};

	public void restartPlotter() {
		if (this.simPlotter != null) {
			this.simPlotter.restart();
		}
	}

	public void stopPlotter() {
		if (this.simPlotter != null) {
			this.simPlotter.stop();
		}
	}

	public void startPlotter(E ns) {
		launchPlotter((NewtonSim) ns);
		restartPlotter();
	}

	private void launchPlotter(NewtonSim integrador) {
		if (this.simPlotter != null) {
			this.simPlotter.kill();
		}

		NewtonSim x = integrador.getCopy();

		double min, max;
		min = Double.MAX_VALUE;
		max = Double.MIN_VALUE;
		for (Event ev : x.getEvents()) {
			double t = ev.getDate();
			if (t < min) {
				min = t;
			}
			if (t > max) {
				max = t;
			}
		}

		if (min < Double.MAX_VALUE) {
			max += 50;
			// min -= 50;
			try {
				x.initPos(min - x.getEpoch());
				x.setEpochNow();
				x.setParamMaxStep(10D);
				this.simPlotter = new SimulatorPlotter<NewtonSim>(
						this.plotterList, x, max - min);
				new Thread(this.simPlotter).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public boolean addPlotView(PlotterView<NewtonSim> e) {
		return this.plotterList.add(e);
	}

	public boolean removePlotView(PlotterView<NewtonSim> o) {
		return this.plotterList.remove(o);
	}

}
