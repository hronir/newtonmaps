package newtonpath.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import newtonpath.kepler.BodySystemRef;
import newtonpath.kepler.Funcions;
import newtonpath.kepler.Newton;
import newtonpath.kepler.OsculatoryElements;
import newtonpath.kepler.Funcions.Inspect;
import newtonpath.kepler.Funcions.Read;
import newtonpath.kepler.events.Event;
import newtonpath.member.MemberItemRoot;
import newtonpath.sim.Simulator;
import newtonpath.statemanager.ObservableArray;
import newtonpath.statemanager.Operation;
import newtonpath.statemanager.StateManager;


public class NewtonSim extends Newton implements Simulator, StateManager {
	@Read
	public final TBRPParam[] tbrpParam;

	// @Read
	public OsculatoryElements[] keplerElements;

	private Collection<Event> events;

	public BodySystemRef systemRef;

	private double lastKeplerCalcTime;

	public NewtonSim(BodySystemRef _sys) {
		super(_sys.getMasses());
		this.systemRef = _sys;
		calcMassTotals();
		this.tbrpParam = new TBRPParam[this.bodies];
		initTBRP();
		this.events = Collections.emptyList(); // _o.getEvents();
	}

	public static NewtonSim getCopy(AproxObs _ap) {
		NewtonSim n = new NewtonSim(_ap.systemRef);
		n.makeACopyFrom(_ap.getIntegrator());
		n.events = new ArrayList<Event>(_ap.getEvents());
		return n;
	}

	public NewtonSim getCopy() {
		NewtonSim n = new NewtonSim(this.systemRef);
		n.makeACopyFrom(this);
		n.events = new ArrayList<Event>(getEvents());
		return n;
	}

	public void setState(Object _o) {
		setStateNewton((NewtonSim) _o);
	}

	public ObservableArray getParameters() {
		return new MemberItemRoot(this.getClass(), Funcions.settingsAnnotations);
	}

	public ObservableArray getObservables() {
		return new MemberItemRoot(this.getClass(),
				Funcions.inspectionAnnotations);
	}

	public Operation[] getOperations() {
		return new MemberItemRoot(this.getClass(), Funcions.actionAnnotations)
				.getOperations(this);
	}

	public void setTimeStep(double ts) {
		this.timeStep = ts;
	}

	public void postProcess() {
		fillKeplerElements(this.keplerElements);
		calcPRTBParam();
	}

	public double getParamMaxStep() {
		return this.paramMaxStep;
	}

	public void setParamMaxStep(double s) {
		this.paramMaxStep = s;
	}

	public double getTimeStep() {
		return this.timeStep;
	}

	public double getEpoch() {
		return this.epoch;
	}

	@Override
	protected void setMasses(double[] _m) {
		super.setMasses(_m);
		calcMassTotals();
	}

	public void calcMassTotals() {
		this.keplerElements = getKeplerElements();
	}

	@Inspect
	public OsculatoryElements spacecraftKeplerElements() {
		if (this.lastKeplerCalcTime != getTime()) {
			fillKeplerElements(this.keplerElements);
		}
		return this.keplerElements[0];
	}

	public OsculatoryElements[] getKeplerElements() {
		OsculatoryElements[] el;
		el = new OsculatoryElements[this.bodies];
		int body, i;
		double M1, M2;
		double totalMass;

		for (body = 0; body < this.bodies; body++) {
			el[body] = new OsculatoryElements();

			M2 = this.M[body];
			if (getCentralBody() < 0) {
				M1 = 0D;
				for (i = 0; i < this.bodies; i++) {
					if (i != body) {
						M1 += this.M[i];
					}
				}
			} else {
				M1 = this.M[getCentralBody()];
			}

			totalMass = M1 + M2;
			if (totalMass > this.paramTOL) {
				el[body].reducedMass = M1 / totalMass;
			} else {
				el[body].reducedMass = 0D;
			}

			el[body].mu = G * totalMass;
		}
		return el;
	}

	public void fillKeplerElements(OsculatoryElements[] _el) {
		int body;
		int i;
		double s[] = new double[3];
		double p[] = new double[3];
		double r2[] = new double[3];
		double v2[] = new double[3];
		double rr[] = new double[3];
		double vv[] = new double[3];
		double b1[] = new double[3];
		double b2[] = new double[3];

		double e, costh, sinth, a, c, h, r;

		if (getCentralBody() < 0) {
			getPositionCOM(p);
			getSpeedCOM(s);
		} else {
		}

		for (body = 0; body < _el.length; body++) {
			double axeFactor = _el[body].reducedMass;
			if (getCentralBody() >= 0) {
				for (i = 0; i < 3; i++) {
					p[i] = (getPosition(getCentralBody(), i) - getPosition(
							body, i))
							* axeFactor + getPosition(body, i);
					s[i] = (getSpeed(getCentralBody(), i) - getSpeed(body, i))
							* axeFactor + getSpeed(body, i);
				}
			}
			for (i = 0; i < 3; i++) {
				r2[i] = getPosition(body, i);
				v2[i] = getSpeed(body, i);

				rr[i] = (r2[i] - p[i]) / (axeFactor);// r2[i]-r1[i];
				vv[i] = (v2[i] - s[i]) / (axeFactor); // v2[i]-v1[i];

				b1[i] = rr[i];
			}

			Funcions.Vect(rr, vv, _el[body].angularMomVec);
			c = _el[body].angularMom = Math.sqrt(Funcions.scal(
					_el[body].angularMomVec, _el[body].angularMomVec));
			r = Math.sqrt(Funcions.scal(rr, rr));

			Funcions.normaliser(b1);
			Funcions.Vect(_el[body].angularMomVec, b1, b2);
			Funcions.normaliser(b2);
			double mu = _el[body].mu;
			h = 0.5D * Funcions.scal(vv, vv) - mu / r;
			e = Math.sqrt(1D + 2D * h * c * c / (mu * mu));

			double aux;
			aux = (mu * (1D - e * e));

			if (Math.abs(aux) > this.paramTOL) {
				a = c * c / aux;

				aux = r * e;
				if (Math.abs(aux) > this.paramTOL) {
					costh = ((a * (1D - e * e) - r) / aux);
					sinth = Math.sqrt(1D - costh * costh);
					if (Funcions.scal(rr, vv) < 0D) {
						sinth = -sinth;
					}
				} else {
					costh = 1D;
					sinth = 0D;
				}
				_el[body].angularMom = (axeFactor) * (axeFactor) * c;
				_el[body].excentricity = e;
				_el[body].major = (axeFactor) * a;
				_el[body].minor = (axeFactor) * a * Math.sqrt(1D - e * e);

				for (i = 0; i < 3; i++) {
					_el[body].angularMomVec[i] = (axeFactor) * (axeFactor)
							* _el[body].angularMomVec[i];
					_el[body].majorVec[i] = (axeFactor) * a
							* (costh * b1[i] - sinth * b2[i]);
					_el[body].center[i] = p[i] - e * _el[body].majorVec[i];
					_el[body].minorVec[i] = _el[body].minor
							* (sinth * b1[i] + costh * b2[i]);
				}
			}
		}
		this.lastKeplerCalcTime = getTime();
	}

	public void calcPRTBParam() {
		int i;
		for (i = 0; i < this.bodies; i++) {
			if (this.tbrpParam[i] != null) {
				this.tbrpParam[i].transformPosition();
			}
		}
	}

	private void initTBRP() {
		int i;
		for (i = 0; i < this.bodies; i++) {
			if (i != getCentralBody()) {
				this.tbrpParam[i] = new TBRPParam(0, getCentralBody(), i);
			}
		}
		this.tbrpParam[getCentralBody()] = null;
	}

	public class TBRPParam {
		final private int body1, body2, body0;
		@Read
		final public double mu, cMu;
		@Read
		final public double synodic[] = new double[6];
		@Read
		final public double sidereal[] = new double[6];
		@Read
		final public double reference[][] = { { 0D, 0D, 0D }, { 0D, 0D, 0D },
				{ 0D, 0D, 0D } };

		public TBRPParam(int i0, int i1, int i2) {
			this.body0 = i0;
			this.body1 = i1;
			this.body2 = i2;
			double tm = NewtonSim.this.M[this.body1]
					+ NewtonSim.this.M[this.body2];
			this.mu = NewtonSim.this.M[this.body2] / tm;
			this.cMu = NewtonSim.this.M[this.body1] / tm;
		}

		public void transformPosition() {
			int i;
			double scal, module, beta;
			// double pCOM[],sCOM[];
			// pCOM=new double[3];
			// sCOM=new double[3];
			for (i = 0; i < 3; i++) {
				this.sidereal[i] = NewtonSim.this.position[3 * this.body0 + i]
						- this.cMu
						* NewtonSim.this.position[3 * this.body1 + i] - this.mu
						* NewtonSim.this.position[3 * this.body2 + i];
				this.sidereal[3 + i] = NewtonSim.this.position[NewtonSim.this.firstSpeedPos
						+ 3 * this.body0 + i]
						- this.cMu
						* NewtonSim.this.position[NewtonSim.this.firstSpeedPos
								+ 3 * this.body1 + i]
						- this.mu
						* NewtonSim.this.position[NewtonSim.this.firstSpeedPos
								+ 3 * this.body2 + i];
				this.reference[0][i] = NewtonSim.this.position[3 * this.body2
						+ i]
						- NewtonSim.this.position[3 * this.body1 + i];
				this.reference[1][i] = NewtonSim.this.position[NewtonSim.this.firstSpeedPos
						+ 3 * this.body2 + i]
						- NewtonSim.this.position[NewtonSim.this.firstSpeedPos
								+ 3 * this.body1 + i];
			}

			module = Math.sqrt(Funcions.scal(this.reference[0],
					this.reference[0]));

			beta = Math
					.sqrt(module
							/ (G * (NewtonSim.this.M[this.body1] + NewtonSim.this.M[this.body2])));

			for (i = 0; i < 3; i++) {
				this.sidereal[i] = this.sidereal[i] / module;
				this.sidereal[3 + i] = this.sidereal[3 + i] * beta;
			}

			for (i = 0; i < 3; i++) {
				this.reference[0][i] = this.reference[0][i] / module;
			}

			scal = Funcions.scal(this.reference[0], this.reference[1]);
			for (i = 0; i < 3; i++) {
				this.reference[1][i] = this.reference[1][i] - scal
						* this.reference[0][i];
			}

			Funcions.normaliser(this.reference[1]);
			Funcions.Vect(this.reference[0], this.reference[1],
					this.reference[2]);
			for (i = 0; i < 3; i++) {
				this.synodic[i] = Funcions.scal(this.sidereal, 0,
						this.reference[i], 0);
				this.synodic[3 + i] = Funcions.scal(this.sidereal, 3,
						this.reference[i], 0);
			}
			this.synodic[3 + 0] += Funcions.scal(this.sidereal, 0,
					this.reference[1], 0);
			this.synodic[3 + 1] -= Funcions.scal(this.sidereal, 0,
					this.reference[0], 0);
		}

		// @Inspect
		// public double test01(){
		// transformPosition();
		// return Funcions.scal(reference[0],reference[1]);
		// }
		// @Inspect
		// public double test02(){
		// transformPosition();
		// return Funcions.scal(reference[0],reference[2]);
		// }
		// @Inspect
		// public double test12(){
		// transformPosition();
		// return Funcions.scal(reference[1],reference[2]);
		// }
		// @Inspect
		// public double test00(){
		// transformPosition();
		// return Funcions.scal(reference[0],reference[0]);
		// }
		// @Inspect
		// public double test11(){
		// transformPosition();
		// return Funcions.scal(reference[1],reference[1]);
		// }
		// @Inspect
		// public double test22(){
		// transformPosition();
		// return Funcions.scal(reference[2],reference[2]);
		// }

		@Inspect
		public double eulerConst() {
			double r1, r2, aux;
			double p[] = new double[3];
			transformPosition();

			r1 = r2 = this.synodic[1] * this.synodic[1] + this.synodic[2]
					* this.synodic[2];
			aux = this.synodic[0] + this.mu;
			r1 += aux * aux;
			aux = this.synodic[0] - this.cMu;
			r2 += aux * aux;
			r1 = Math.sqrt(r1);
			r2 = Math.sqrt(r2);

			p[0] = this.synodic[3] - this.synodic[1];
			p[1] = this.synodic[4] + this.synodic[0];
			p[2] = this.synodic[5];
			return 0.5D * Funcions.scal(p, p) + p[0] * this.synodic[1] - p[1]
					* this.synodic[0] - this.cMu / r1 - this.mu / r2;
		}
	}

	public Collection<Event> getEvents() {
		return this.events;
	}

	private int getCentralBody() {
		return this.systemRef.getCentralBody();
	}

}
