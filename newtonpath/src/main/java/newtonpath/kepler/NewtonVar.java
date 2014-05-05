package newtonpath.kepler;

import java.util.Arrays;

public class NewtonVar extends Newton {

	protected final int firstVarPos;

	private static final int COLSVAR = 6;

	public NewtonVar(double[] _masses) {
		super(_masses, _masses.length, _masses.length * COLSVAR + COLSVAR
				* COLSVAR);
		this.firstVarPos = this.bodies * 6;
	}

	double energie_cin_sonde() {
		double vcdm[] = new double[3];
		double vrel[] = new double[3];
		vitesse_cdm(this.position, vcdm);
		vrel[0] = this.position[this.firstSpeedPos + 0] - vcdm[0];
		vrel[1] = this.position[this.firstSpeedPos + 1] - vcdm[1];
		vrel[2] = this.position[this.firstSpeedPos + 2] - vcdm[2];
		return 0.5 * Funcions.scal(vrel, vrel);
	}

	double energie_pot_sonde() {
		double e;
		double y[] = new double[3];
		int i;
		e = 0.;
		for (i = 1; i < this.bodies; i++) {
			Funcions.copySubTable(this.position, 3 * i, y, 0, 3);
			e += -G * this.M[i] / Funcions.dist(this.position, y);
		}
		return e;
	}

	double energie_sonde() {
		return energie_cin_sonde() + energie_pot_sonde();
	}

	private final double newtonDifMap[] = new double[3 * 3];

	@Override
	void vectorField(double t, double A[], double F[]) throws Exception {
		int i, j, k, l;
		int comp;
		double d, d2, d3, d5;
		double mu, phi;
		double TOL2 = this.paramTOL * this.paramTOL;

		for (i = 0; i < this.firstSpeedPos; i++) {
			F[i] = A[this.firstSpeedPos + i];
			F[this.firstSpeedPos + i] = 0.;
		}

		for (i = this.firstVarPos; i < this.dimension; i++) {
			F[i] = 0.;
		}

		for (k = 0; k < 3 * 3; k++) {
			this.newtonDifMap[k] = 0.;
		}

		for (i = 1; i < this.bodies; i++) {
			d2 = d3 = 0D;
			for (j = i - 1; j >= 0; j--) {
				d2 = Funcions.dist2(A, 3 * i, A, 3 * j);
				if (d2 < TOL2) {
					throw new Exception("Collision " + Integer.toString(i)
							+ " " + Integer.toString(j));
				}
				d = Math.sqrt(d2);
				d3 = d * d2;
				for (comp = 0; comp < 3; comp++) {
					F[this.firstSpeedPos + 3 * i + comp] += G * this.M[j]
							* (A[3 * j + comp] - A[3 * i + comp]) / d3;
					F[this.firstSpeedPos + 3 * j + comp] += G * this.M[i]
							* (A[3 * i + comp] - A[3 * j + comp]) / d3;
				}
			}
			d5 = d3 * d2;
			for (k = 0; k < 3; k++) {
				for (l = 0; l < 3; l++) {
					mu = (A[3 * i + l] - A[l]) * (A[3 * i + k] - A[k]);
					phi = (3. * mu - (k == l ? d2 : 0.)) / d5;
					this.newtonDifMap[3 * k + l] += G * this.M[i] * phi;
				}
			}

		}
		for (i = 0; i < COLSVAR; i++) {
			for (k = 0; k < 3; k++) {
				F[this.firstVarPos + 6 * i + k] = A[this.firstVarPos + 6 * i
						+ k + 3];
				F[this.firstVarPos + 6 * i + k + 3] = 0.;
				for (l = 0; l < 3; l++) {
					F[this.firstVarPos + 6 * i + k + 3] += this.newtonDifMap[3
							* l + k]
							* A[this.firstVarPos + 6 * i + l];
				}
			}
		}

	}

	@Override
	protected void cond_ini(double X[]) {
		super.cond_ini(X);
		cond_ini_var(X);
	}

	@Override
	public void initPos(double _t0) throws Exception {
		super.initPos(_t0);
		cond_ini_var(this.position);
	}

	private void cond_ini_var(double x[]) {
		int i, j;
		for (i = 0; i < COLSVAR; i++) {
			for (j = 0; j < 6; j++) {
				x[this.firstVarPos + i * 6 + j] = 0.;
			}
			x[this.firstVarPos + i * 6 + i] = 1.;
		}
	}

	public void reference_planete(int corps, double e1[], double e2[],
			double e3[], double de1[], double de2[], double de3[])
			throws Exception {

		double vcdm[] = new double[3], cdm[] = new double[3];
		vitesse_cdm(this.position, vcdm);
		pos_cdm(this.position, cdm);

		calculateRelativePlanetReferenceHorizontal(corps, cdm, vcdm, e1, e2,
				e3, de1, de2, de3);
	}

	private void calculateRelativePlanetReferenceHorizontal(int corps,
			double[] originPosition, double[] originSpeed, double[] e1,
			double[] e2, double[] e3, double[] de1, double[] de2, double[] de3)
			throws Exception {
		double[] destinationPosition = Arrays.copyOfRange(this.position,
				3 * corps, 3 * (corps + 1));

		double[] destinationVelocity = Arrays.copyOfRange(this.position,
				this.firstSpeedPos + 3 * corps, this.firstSpeedPos + 3
						* (corps + 1));

		double mod_e1, mod_e2;
		double v_e1;

		double r[] = new double[3], v[] = new double[3];

		r[0] = e1[0] = destinationPosition[0] - originPosition[0];
		r[1] = e1[1] = destinationPosition[1] - originPosition[1];
		r[2] = e1[2] = destinationPosition[2] - originPosition[2];
		mod_e1 = Math.sqrt(Funcions.scal(e1, e1));
		e1[0] = e1[0] / mod_e1;
		e1[1] = e1[1] / mod_e1;
		e1[2] = e1[2] / mod_e1;

		v[0] = e2[0] = destinationVelocity[0] - originSpeed[0];
		v[1] = e2[1] = destinationVelocity[1] - originSpeed[1];
		v[2] = e2[2] = destinationVelocity[2] - originSpeed[2];
		v_e1 = Funcions.scal(v, e1);
		e2[0] -= v_e1 * e1[0];
		e2[1] -= v_e1 * e1[1];
		e2[2] -= v_e1 * e1[2];
		mod_e2 = Math.sqrt(Funcions.scal(e2, e2));
		e2[0] = e2[0] / mod_e2;
		e2[1] = e2[1] / mod_e2;
		e2[2] = e2[2] / mod_e2;

		Funcions.Vect(e1, e2, e3);

		double dv_e1, v_de1;
		double dv[] = new double[3], de3_b[] = new double[3];
		double F[] = new double[this.dimension];

		de1[0] = e2[0] * mod_e2 / mod_e1;
		de1[1] = e2[1] * mod_e2 / mod_e1;
		de1[2] = e2[2] * mod_e2 / mod_e1;

		vectorField(0D, this.position, F);
		dv[0] = F[this.firstSpeedPos + 3 * corps + 0];
		dv[1] = F[this.firstSpeedPos + 3 * corps + 1];
		dv[2] = F[this.firstSpeedPos + 3 * corps + 2];

		dv_e1 = Funcions.scal(dv, e1);
		v_de1 = Funcions.scal(v, de1);

		de2[0] = dv[0] - (dv_e1 + v_de1) * e1[0] - v_e1 * de1[0];
		de2[1] = dv[1] - (dv_e1 + v_de1) * e1[1] - v_e1 * de1[1];
		de2[2] = dv[2] - (dv_e1 + v_de1) * e1[2] - v_e1 * de1[2];

		double e2_de2;
		e2_de2 = Funcions.scal(e2, de2);

		de2[0] = (de2[0] - e2_de2 * e2[0]) / mod_e2;
		de2[1] = (de2[1] - e2_de2 * e2[1]) / mod_e2;
		de2[2] = (de2[2] - e2_de2 * e2[2]) / mod_e2;

		Funcions.Vect(de1, e2, de3);
		Funcions.Vect(e1, de2, de3_b);
		de3[0] += de3_b[0];
		de3[1] += de3_b[1];
		de3[2] += de3_b[2];
	}

	int integrer_dfix(int corps, double distance, double delta_t_max)
			throws Exception {
		// int flag;
		boolean signe_pos;
		double denom;
		int maxiter;

		int resultat;

		double val_newton[] = new double[this.dimension];

		double dpos[] = new double[3], dvel[] = new double[3];

		double valscal;

		// flag = 4;
		this.time = 0.;

		this.timeStep = delta_t_max < 0. ? -this.paramMinStep
				: this.paramMinStep;

		dpos[0] = this.position[0] - this.position[3 * corps + 0];
		dpos[1] = this.position[1] - this.position[3 * corps + 1];
		dpos[2] = this.position[2] - this.position[3 * corps + 2];

		dvel[0] = this.position[this.firstSpeedPos + 0]
				- this.position[this.firstSpeedPos + 3 * corps + 0];
		dvel[1] = this.position[this.firstSpeedPos + 1]
				- this.position[this.firstSpeedPos + 3 * corps + 1];
		dvel[2] = this.position[this.firstSpeedPos + 2]
				- this.position[this.firstSpeedPos + 3 * corps + 2];

		signe_pos = Funcions.scal(dpos, dpos) - distance * distance > 0.;

		resultat = 1;

		while (Math.abs(this.time) < Math.abs(delta_t_max)) {
			this.oneStep();

			dpos[0] = this.position[0] - this.position[3 * corps + 0];
			dpos[1] = this.position[1] - this.position[3 * corps + 1];
			dpos[2] = this.position[2] - this.position[3 * corps + 2];

			dvel[0] = this.position[this.firstSpeedPos + 0]
					- this.position[this.firstSpeedPos + 3 * corps + 0];
			dvel[1] = this.position[this.firstSpeedPos + 1]
					- this.position[this.firstSpeedPos + 3 * corps + 1];
			dvel[2] = this.position[this.firstSpeedPos + 2]
					- this.position[this.firstSpeedPos + 3 * corps + 2];

			valscal = Funcions.scal(dpos, dpos) - distance * distance;

			if (valscal > 0. && !signe_pos || valscal < 0. && signe_pos) {
				for (maxiter = 20; maxiter > 0; maxiter--) {
					vectorField(0., this.position, val_newton);
					denom = 2. * Funcions.scal(dpos, dvel);
					if (Math.abs(denom) < this.paramTOL) {
						resultat = 2;
						break;
					}

					double step = -valscal / denom;

					this.timeStep = step;
					if (Math.abs(step) < this.paramMinStep) {
						resultat = 0;
						break;
					}

					this.oneStep(Math.abs(step));

					dpos[0] = this.position[0] - this.position[3 * corps + 0];
					dpos[1] = this.position[1] - this.position[3 * corps + 1];
					dpos[2] = this.position[2] - this.position[3 * corps + 2];

					dvel[0] = this.position[this.firstSpeedPos + 0]
							- this.position[this.firstSpeedPos + 3 * corps + 0];
					dvel[1] = this.position[this.firstSpeedPos + 1]
							- this.position[this.firstSpeedPos + 3 * corps + 1];
					dvel[2] = this.position[this.firstSpeedPos + 2]
							- this.position[this.firstSpeedPos + 3 * corps + 2];

					valscal = Funcions.scal(dpos, dpos) - distance * distance;
					if (Math.abs(valscal) < this.paramAbsErr) {
						// _temps.d += this.temps;
						resultat = 0;
						break;
					}
				}
				break;
			}
		}
		return resultat;
	}

	public void vect_esf_etat(int corps, double ref[][], double vpar[],
			double X[]) {
		// double dtau[]=new double[6],dtaudt;
		// int result;
		double calpha, ctheta, cphi;
		double salpha, stheta, sphi;
		double a1[] = new double[3], a2[] = new double[3], a3[] = new double[3];
		int i;

		// double Nini[]=new double[X.length],Nfin[]=new double[X.length];

		ctheta = Math.cos(vpar[2]);
		stheta = Math.sin(vpar[2]);
		cphi = Math.cos(vpar[3]);
		sphi = Math.sin(vpar[3]);
		calpha = Math.cos(vpar[5]);
		salpha = Math.sin(vpar[5]);

		for (i = 0; i < 3; i++) {
			a1[i] = stheta * cphi * ref[0][i] + stheta * sphi * ref[1][i]
					+ ctheta * ref[2][i];
			a2[i] = -ctheta * cphi * ref[0][i] - ctheta * sphi * ref[1][i]
					+ stheta * ref[2][i];
			a3[i] = sphi * ref[0][i] - cphi * ref[1][i];

			X[i] = X[3 * corps + i] + vpar[1] * a1[i];
			X[this.firstSpeedPos + i] = X[this.firstSpeedPos + 3 * corps + i]
					+ vpar[4] * (calpha * a2[i] + salpha * a3[i]);
		}
	}

	public void vect_etat_esf(int corps, double ref[][], double X[],
			double vpar[]) {
		int j;
		double r[] = new double[3], v[] = new double[3];
		double dist, vel, vcosth, vsinth, vcosph, vsinph, vcosal, vsinal;
		for (j = 0; j < 3; j++) {
			r[j] = X[j] - X[3 * corps + j];
			v[j] = X[this.firstSpeedPos + j]
					- X[this.firstSpeedPos + 3 * corps + j];
		}
		vel = Math.sqrt(Funcions.scal(v, v));
		dist = Math.sqrt(Funcions.scal(r, r));
		vcosth = Funcions.scal(r, 0, ref[2], 0) / dist;
		vsinth = Math.sqrt(1. - vcosth * vcosth);
		vcosph = Funcions.scal(r, 0, ref[0], 0) / (dist * vsinth);
		vsinph = Funcions.scal(r, 0, ref[1], 0) / (dist * vsinth);
		vcosal = Funcions.scal(v, 0, ref[2], 0) / (vel * vsinth);
		vsinal = (Funcions.scal(v, 0, ref[0], 0) * vsinph - Funcions.scal(v, 0,
				ref[1], 0) * vcosph)
				/ vel;

		vpar[1] = dist;
		vpar[2] = Math.acos(vcosth);
		vpar[3] = Math.atan2(vsinph, vcosph);
		vpar[4] = vel;
		vpar[5] = Math.atan2(vsinal, vcosal);
	}

	protected void copyState(NewtonVar orig, NewtonVar newObj) {
		// newObj.aproxEndBodyDistance=orig.aproxEndBodyDistance;
		// newObj.aproxInitialEndBodyDistance=orig.aproxInitialEndBodyDistance;
		// newObj.aproxInitialStartBodyDistance=orig.aproxInitialStartBodyDistance;
		// newObj.aproxInitialTime=orig.aproxInitialTime;
		// newObj.aproxMaxIter=orig.aproxMaxIter;
		// newObj.aproxMetricPar=(double[])orig.aproxMetricPar.clone();
		// newObj.aproxMinStepPar=orig.aproxMinStepPar;
		// newObj.aproxStepPar=orig.aproxStepPar;
		// newObj.aproxTolErr=orig.aproxTolErr;
		// newObj.base[0]=(double[])orig.base[0].clone();
		// newObj.base[1]=(double[])orig.base[1].clone();
		// newObj.base[2]=(double[])orig.base[2].clone();
		newObj.epoch = orig.epoch;
		newObj.epochPosition = cloneArray(orig.epochPosition);
		newObj.flag = orig.flag;
		newObj.paramAbsErr = orig.paramAbsErr;
		newObj.paramMaxStep = orig.paramMaxStep;
		newObj.paramMinStep = orig.paramMinStep;
		newObj.paramRelErr = orig.paramRelErr;
		newObj.paramTOL = orig.paramTOL;
		// newObj.poincareDifMap=cloneArray(orig.poincareDifMap);
		// newObj.poincareEndBody=orig.poincareEndBody;
		// newObj.poincareEndPoint=cloneArray(orig.poincareEndPoint);
		// newObj.poincareEndVector=cloneArray(orig.poincareEndVector);
		// newObj.poincareMaxIterRef=orig.poincareMaxIterRef;
		// newObj.poincareMaxTime=orig.poincareMaxTime;
		// newObj.poincareMinTime =orig.poincareMinTime;
		// newObj.poincareResult =orig.poincareResult;
		// newObj.poincareStartBody =orig.poincareStartBody;
		// newObj.poincareStartPoint=cloneArray(orig.poincareStartPoint);
		// newObj.poincareStartVector=cloneArray(orig.poincareStartVector);
		copyVector(orig.position, newObj.position);
		newObj.time = orig.time;
		newObj.timeStep = orig.timeStep;
		newObj.initTimeStep = orig.initTimeStep;
		// newObj.poincareSense=orig.poincareSense;
	}

}