/*
 * Newton.java
 *
 * Created on 12 de octubre de 2007, 19:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package newtonpath.kepler;

import newtonpath.kepler.Funcions.Action;
import newtonpath.kepler.Funcions.Inspect;
import newtonpath.kepler.Funcions.Read;
import newtonpath.kepler.Funcions.Write;

/**
 * 
 * @author oriol
 */
public class Newton extends RK78 {

	protected static final double G = 2.9591220828559e-4;

	protected static final double HMIN_def = 1.e-15;

	protected static final double HMAX_def = 1.0;

	protected static final double RELERR_def = 1.e-10;

	protected static final double ABSERR_def = 1.e-10;

	public double paramTOL = 1.e-18;

	@Read
	public final int bodies;

	@Read
	public final int firstSpeedPos;

	public final double M[];

	public final static double EPOCA_INI = 2454501.5;

	@Read
	public double epochPosition[];

	@Write
	public double epoch;

	@Write
	public double initTimeStep = 0;

	public Newton(double _masses[]) {
		this(_masses, _masses.length, _masses.length * 6);
	}

	public Newton(double _masses[], int _bodies, int _dim) {
		super(HMIN_def, HMIN_def, HMAX_def, RELERR_def, ABSERR_def,
				_bodies * 6 > _dim ? _bodies * 6 : _dim);
		this.M = _masses.clone();
		this.bodies = _bodies;
		this.firstSpeedPos = this.bodies * 3;
		this.epochPosition = new double[this.dimension];
	}

	protected void setMasses(double _m[]) {
		Funcions.copySubTable(_m, 0, this.M, 0, this.bodies);
	}

	public static Newton getCopy(Newton _o) {
		Newton ret = new Newton(_o.M);
		ret.makeACopyFrom(_o);
		return ret;
	}

	public void makeACopyFrom(Newton _o) {
		final Newton ret = this;
		ret.copyVector(_o.position, ret.position);
		ret.copyVector(_o.epochPosition, ret.epochPosition);

		ret.epoch = _o.epoch;
		ret.flag = _o.flag;
		ret.paramAbsErr = _o.paramAbsErr;
		ret.paramMaxStep = _o.paramMaxStep;
		ret.paramMinStep = _o.paramMinStep;
		ret.paramRelErr = _o.paramRelErr;
		ret.paramTOL = _o.paramTOL;

		ret.time = _o.time;
		ret.timeStep = _o.timeStep;
		ret.initTimeStep = _o.initTimeStep;
	}

	public void setStateNewton(Newton _o) {
		copyVector(_o.position, this.position);
		this.time = _o.time;
		this.initTimeStep = _o.initTimeStep;
		this.timeStep = _o.timeStep;
	}

	@Override
	void vectorField(double t, double A[], double F[]) throws Exception {
		int i, j;
		int comp;
		double d, d2, d3;
		double TOL2 = this.paramTOL * this.paramTOL;

		for (i = 0; i < this.firstSpeedPos; i++) {
			F[i] = A[this.firstSpeedPos + i];
			F[this.firstSpeedPos + i] = 0.;
		}

		for (i = 1; i < this.bodies; i++) {
			d2 = d3 = 0D;
			for (j = i - 1; j >= 0; j--) {
				if (this.M[i] > this.paramTOL || this.M[j] > this.paramTOL) {
					d2 = Funcions.dist2(A, 3 * i, A, 3 * j);
					if (d2 < TOL2) {
						throw new Exception("Collision " + Integer.toString(i)
								+ " " + Integer.toString(j));
					}
					d = Math.sqrt(d2);
					d3 = d * d2;
					for (comp = 0; comp < 3; comp++) {
						double aux = (A[3 * j + comp] - A[3 * i + comp]) / d3;
						F[this.firstSpeedPos + 3 * i + comp] += this.M[j] * aux;
						F[this.firstSpeedPos + 3 * j + comp] -= this.M[i] * aux;
					}
				}
			}
		}
		for (i = 0; i < this.firstSpeedPos; i++) {
			F[this.firstSpeedPos + i] *= G;
		}
	}

	protected void cond_ini(double X[]) {
		int i;
		for (i = 0; i < this.dimension; i++) {
			X[i] = this.epochPosition[i];
		}
	}

	public void initPos() {
		this.time = 0D;
		this.flag = 4;
		if (this.initTimeStep > this.paramMinStep) {
			this.timeStep = this.initTimeStep;
		}
		cond_ini(this.position);
	}

	public void initPos(double _t0) throws Exception {
		initPos();
		timeIntegration(_t0);
	}

	public void setInitStep() throws Exception {
		this.initTimeStep = 0;
		initPos();
		this.timeStep = this.paramMaxStep;
		oneStep();
		this.initTimeStep = this.timeStep;
		initPos();
	}

	@Inspect
	public double getAbsoluteTime() {
		return this.epoch + this.time;
	}

	public void toAbsoluteTime(double t) throws Exception {
		initPos(t - this.epoch);
	}

	@Action
	public void setEpochNow() {
		this.epoch += this.time;
		this.time = 0D;
		copyVector(this.position, this.epochPosition);
	}

	public void vitesse_cdm(double X[], double vcdm[]) {
		int i, comp;
		double Mtotale;
		Mtotale = 0.;
		for (comp = 0; comp < 3; comp++) {
			vcdm[comp] = 0.;
		}
		for (i = 0; i < this.bodies; i++) {
			Mtotale += this.M[i];
			for (comp = 0; comp < 3; comp++) {
				vcdm[comp] += this.M[i] * X[this.firstSpeedPos + 3 * i + comp];
			}
		}
		for (comp = 0; comp < 3; comp++) {
			vcdm[comp] = vcdm[comp] / Mtotale;
		}
	}

	public void pos_cdm(double X[], double cdm[]) {
		int i, comp;
		double Mtotale;
		Mtotale = 0.;
		for (comp = 0; comp < 3; comp++) {
			cdm[comp] = 0.;
		}
		for (i = 0; i < this.bodies; i++) {
			Mtotale += this.M[i];
			for (comp = 0; comp < 3; comp++) {
				cdm[comp] += this.M[i] * X[3 * i + comp];
			}
		}
		for (comp = 0; comp < 3; comp++) {
			cdm[comp] = cdm[comp] / Mtotale;
		}
	}

	protected double getPosition(int _body, int _comp) {
		return this.position[3 * _body + _comp];
	}

	protected double getSpeed(int _body, int _comp) {
		return this.position[this.firstSpeedPos + 3 * _body + _comp];
	}

	public void setBodyState(int body, double[] positionAndSpeed) {
		for (int i = 0; i < 3; i++) {
			this.position[body * 3 + i] = positionAndSpeed[i];
			this.position[this.firstSpeedPos + body * 3 + i] = positionAndSpeed[3 + i];
		}
	}

	public void getSpeedCOM(double[] s) {
		vitesse_cdm(this.position, s);
	}

	public void getPositionCOM(double[] p) {
		pos_cdm(this.position, p);
	}

	public void checkFlyBy(double limit, double checkTime, double[] bodies)
			throws Exception {

		double t;
		int i;
		double limit2 = limit * limit;
		for (i = 0; i < this.bodies; i++) {
			bodies[i] = Double.NaN;
		}
		this.flag = 4;
		t = this.time + checkTime;
		double timeStep = Math.abs(this.timeStep);
		this.timeStep = (checkTime < 0.) ? -timeStep : timeStep;
		if (checkTime < 0) {
			while (this.time > t) {
				this.oneStep();
				for (i = 1; i < this.bodies; i++) {
					if (Funcions.dist2(this.position, 0, this.position, i * 3) < limit2) {
						bodies[i] = this.getTime();
					}
				}
			}
		} else {
			while (this.time < t) {
				this.oneStep();
				for (i = 1; i < this.bodies; i++) {
					if (Funcions.dist2(this.position, 0, this.position, i * 3) < limit2) {
						bodies[i] = this.getTime();
					}
				}
			}
		}
	}
}
