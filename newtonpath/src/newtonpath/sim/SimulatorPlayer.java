package newtonpath.sim;

import newtonpath.ui.JKView;

public class SimulatorPlayer<E extends Simulator> extends SimulatorRunner<E> {

	public SimulatorPlayer(JKView<E> _view, E _sim) {
		super(_view, _sim);
		this.ctrl = new SimController<E>();
	}

	private final SimController<E> ctrl;

	@Override
	protected long work(E _int, long _execTime) {

		int iter = 100000;
		boolean backwards;
		double endTime;
		long sysTime = _execTime;
		boolean seek;

		synchronized (this.ctrl) {
			backwards = this.ctrl.isBackwards();
			endTime = this.ctrl.getIntTime(_execTime);
			seek = this.ctrl.isSeeking();
			if (this.ctrl.changed) {
				this.ctrl.config(_int);
				this.ctrl.changed = false;
			}
		}
		try {
			while (iter-- > 0) {
				if ((endTime > _int.getTime()) == backwards) {
					if (seek) {
						double oldStep = _int.getTimeStep();
						_int.setTimeStep(endTime - _int.getTime());
						_int.oneStep(Math.abs(_int.getTimeStep()));
						_int.setTimeStep(oldStep);
						stop();
					}
					break;
				}
				if (_execTime < System.currentTimeMillis()) {
					break;
				}
				_int.oneStep();
			}
			sysTime = this.ctrl.getSysTime(_int.getTime());
		} catch (Exception ex) {
			ex.printStackTrace();
			stop();
		}
		return sysTime;
	}

	private static class SimController<E extends Simulator> {
		private long initialSystemTime = 0L;
		private double initialIntegTime = 0D;

		private boolean backwards = false;
		private double speed = 1D;

		private final double maxIntegStep = 0.1D;
		private final long sysTimeStep = 200;
		private double maxStep = 0.1D;

		private double seek = Double.NaN;

		protected volatile boolean changed = true;

		public SimController() {
		}

		public void config(E _int) {
			_int.setParamMaxStep(this.maxStep);
			double s = Math.abs(_int.getTimeStep());
			if (s > this.maxStep) {
				s = this.maxStep;
			}
			if (this.backwards) {
				s = -s;
			}
			_int.setTimeStep(s);
		}

		public synchronized void start(double _currentTime, double _speed,
				boolean _backwards) {
			this.seek = Double.NaN;
			this.initialSystemTime = System.currentTimeMillis();
			this.initialIntegTime = _currentTime;
			this.speed = Math.abs(_speed);
			this.maxStep = this.sysTimeStep * this.speed;
			this.backwards = _backwards;
			if (this.backwards) {
				this.speed = -this.speed;
			}
			if (this.maxStep > this.maxIntegStep) {
				this.maxStep = this.maxIntegStep;
			}
			this.changed = true;
		}

		public synchronized void seek(double _currentTime, double _seekTime) {
			this.initialSystemTime = System.currentTimeMillis();
			this.initialIntegTime = _currentTime;
			this.maxStep = this.maxIntegStep;
			this.seek = _seekTime;
			this.backwards = _currentTime > _seekTime;
			this.changed = true;
		}

		public double getIntTime(long _millis) {
			if (!Double.isNaN(this.seek)) {
				return this.seek;
			}
			return (_millis - this.initialSystemTime) * this.speed
					+ this.initialIntegTime;
		}

		public long getSysTime(double _t) {
			if (!Double.isNaN(this.seek)) {
				return this.initialSystemTime;
			}
			return (long) ((_t - this.initialIntegTime) / this.speed)
					+ this.initialSystemTime;
		}

		public boolean isBackwards() {
			return this.backwards;
		}

		public boolean isSeeking() {
			return (!Double.isNaN(this.seek));
		}
	}

	public void startSimulation(double _speed, boolean _backwards) {
		this.ctrl.start(this.simView.getTime(), _speed, _backwards);
		start();
	}

	public void pauseSimulation() {
		stop();
	}

	public void seekSimulation(double _time) {
		this.ctrl.seek(this.simView.getTime(), _time);
		start();
	}

	public void goTo(double t) throws Exception {
		this.simView.initPos();
		this.simView.timeIntegration(t);
	}

}
