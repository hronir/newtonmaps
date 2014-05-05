package newtonpath.kepler;

import newtonpath.kepler.Funcions.Action;
import newtonpath.kepler.Funcions.Inspect;
import newtonpath.kepler.Funcions.Operation;
import newtonpath.kepler.Funcions.Parameter;
import newtonpath.kepler.Funcions.Read;
import newtonpath.kepler.Funcions.Write;

public class Aprox {
	@Write
	@Parameter({ "Flyby" })
	public double aproxTolErr = 1.e-6;
	@Write
	@Parameter({ "Flyby" })
	public double aproxMetricPar[] = { 365D, 0.001D, Math.PI, 2D * Math.PI,
			0.001D, 2D * Math.PI };
	@Write
	@Parameter({ "Flyby" })
	public double aproxStepPar = 0.0005D;
	@Write
	@Parameter({ "Flyby" })
	public double aproxMinStepPar = 1.0e-9D;
	@Write
	@Parameter({ "Flyby" })
	public int aproxMaxIter = 200;
	@Write
	@Parameter({ "Flyby" })
	public double aproxEndBodyDistance = 0.005D;
	@Write
	@Parameter({ "Launch" })
	public double aproxInitialStartBodyDistance = 0.01D;
	@Write
	@Parameter({ "Launch" })
	public double aproxInitialEndBodyDistance = 5D;
	@Write
	@Parameter({ "Launch" })
	public double aproxInitialTime = 0;
	@Read
	transient private int totalPendingIter;

	final private static int AB_POINTS = 1;

	private final int centralBody;

	@Write
	@Parameter({ "Flyby", "Launch", "Poincare Map" })
	public Poincare section;

	public Aprox(double[] _masses, int _centralBody) {
		// super(_masses);
		this.section = new Poincare(_masses);
		this.centralBody = _centralBody;
	}

	protected void copyState(Aprox orig, Aprox newObj) {
		newObj.aproxEndBodyDistance = orig.aproxEndBodyDistance;
		newObj.aproxInitialEndBodyDistance = orig.aproxInitialEndBodyDistance;
		newObj.aproxInitialStartBodyDistance = orig.aproxInitialStartBodyDistance;
		newObj.aproxInitialTime = orig.aproxInitialTime;
		newObj.aproxMaxIter = orig.aproxMaxIter;
		newObj.aproxMetricPar = orig.aproxMetricPar.clone();
		newObj.aproxMinStepPar = orig.aproxMinStepPar;
		newObj.aproxStepPar = orig.aproxStepPar;
		newObj.aproxTolErr = orig.aproxTolErr;
		newObj.section.copyState(orig.section, newObj.section);
	}

	private void copyVector(double[] origin, double[] destination) {
		for (int i = 0; i < origin.length; i++) {
			destination[i] = origin[i];
		}
	}

	private int gradient_optimize_with_step(double step) throws Exception {
		double grad[][] = { { 0D, 0D, 0D, 0D, 0D, 0D },
				{ 0D, 0D, 0D, 0D, 0D, 0D }, { 0D, 0D, 0D, 0D, 0D, 0D },
				{ 0D, 0D, 0D, 0D, 0D, 0D }, { 0D, 0D, 0D, 0D, 0D, 0D } };
		int i, j, l;
		int result = 0;
		double minDist = Double.MAX_VALUE;
		double copiaStartVec[] = this.section.poincareStartVector.clone();
		double copiaEndVec[] = this.section.poincareEndVector.clone();
		double copiaDifMap[] = this.section.poincareDifMap.clone();

		i = 0;
		l = 0;
		while (result == 0
				&& this.totalPendingIter > 0
				&& this.section.poincareEndVector[1] < minDist
				&& this.section.poincareEndVector[1] > this.aproxEndBodyDistance) {

			// Copia vector inicial i diferencial
			copyVector(this.section.poincareStartVector, copiaStartVec);
			copyVector(this.section.poincareEndVector, copiaEndVec);
			copyVector(this.section.poincareDifMap, copiaDifMap);
			minDist = this.section.poincareEndVector[1];
			l++;

			for (j = 0; j < 6; j++) {
				grad[i][j] = -this.aproxMetricPar[j]
						* this.section.poincareDifMap[1 + 6 * j];
			}

			if (Funcions.normaliserd(grad[i], 6) == 0D) {
				result = ErrorCode.GradientUnderFlow.getErrorNumber();
				break;
			}

			Funcions.adamsBashforth(this.section.poincareStartVector, 6,
					grad[i], grad[(i + 4) % 5], grad[(i + 3) % 5],
					grad[(i + 2) % 5], l >= AB_POINTS ? AB_POINTS : 1, step);

			result = this.section.poincareMap();

			if (result != 0 || minDist < this.section.poincareEndVector[1]) {
				// Restaura vector inicial i diferencial
				copyVector(copiaStartVec, this.section.poincareStartVector);
				this.section.poincareEndVector = copiaEndVec.clone();
				this.section.poincareDifMap = copiaDifMap.clone();
			} else {
				i = (i + 1) % 5;
				this.totalPendingIter--;
			}
		}
		return result;
	}

	private int gradient_optimize() throws Exception {
		int result;
		double step;

		step = this.aproxStepPar;
		result = this.section.poincareMap();
		if (result == 0) {
			while (this.totalPendingIter > 0
					&& this.section.poincareEndVector[1] > this.aproxEndBodyDistance
					&& step >= this.aproxMinStepPar) {

				result = gradient_optimize_with_step(step);
				if (this.section.poincareEndVector[1] > this.aproxEndBodyDistance) {
					step *= 0.5D;
				}
			}
			if (this.totalPendingIter <= 0) {
				result = ErrorCode.IterationOverFlow.getErrorNumber();
			} else if (step < this.aproxMinStepPar) {
				result = ErrorCode.StepUnderflow.getErrorNumber();
			} else {
				result = this.section.poincareMap();
			}
		}
		return result;
	}

	private int approcher_rafiner() throws Exception {
		// double totalPendingIter;
		int j, k;
		int result;
		result = 0;
		while (this.totalPendingIter > 0) {
			result = this.section.poincareMap();
			if (result != 0) {
				break;
			}
			if (Math.abs(this.section.poincareEndVector[1]
					- this.aproxEndBodyDistance)
					/ this.aproxEndBodyDistance < this.aproxTolErr) {
				break;
			}
			k = 0;
			for (j = 0; j < 6; j++) {
				if (j == 1) {
					continue;
				}
				if (Math.abs(this.section.poincareDifMap[1 + 6 * j]) > Math
						.abs(this.section.poincareDifMap[1 + 6 * k])) {
					k = j;
				}
			}
			if (Math.abs(this.section.poincareDifMap[1 + 6 * k]) < getIntegrator().paramTOL
					* Math.abs(this.section.poincareEndVector[1]
							- this.aproxEndBodyDistance)) {
				result = 12;
				break;
			}
			this.section.poincareStartVector[k] -= (this.section.poincareEndVector[1] - this.aproxEndBodyDistance)
					/ this.section.poincareDifMap[1 + 6 * k];
			this.totalPendingIter--;
		}
		if (this.totalPendingIter <= 0) {
			result = 13;
		}
		return result;
	}

	private void param_aproche_orbite(int planet1, int planet2, double dst1,
			double dst2, double X[], double par[]) {
		double v[] = new double[3], r[] = new double[3], l[] = new double[3];
		int i;
		double a1, a2;
		double euler, v0, vf;
		for (i = 0; i < 3; i++) {
			r[i] = X[planet1 * 3 + i] - X[this.centralBody * 3 + i];
			v[i] = X[getIntegrator().firstSpeedPos + planet1 * 3 + i]
					- X[getIntegrator().firstSpeedPos + this.centralBody * 3
							+ i];
		}
		// Construccio referencia
		Funcions.Vect(r, v, l);
		Funcions.Vect(l, r, v);
		Funcions.normaliser(r);
		Funcions.normaliser(v);

		//
		for (i = 0; i < 3; i++) {
			X[i] = X[planet1 * 3 + i] + dst1 * r[i];
		}

		a1 = Funcions.dist(X, this.centralBody * 3, X, planet1 * 3);
		a2 = Funcions.dist(X, this.centralBody * 3, X, planet2 * 3);

		double f0 = 1D / a1;
		double f1 = 1D * Math
				.sqrt(a1
						/ (Newton.G * (getIntegrator().M[this.centralBody] + getIntegrator().M[planet1])));
		double mu = getIntegrator().M[planet1]
				/ (getIntegrator().M[this.centralBody] + getIntegrator().M[planet1]);

		double cMu = getIntegrator().M[this.centralBody]
				/ (getIntegrator().M[this.centralBody] + getIntegrator().M[planet1]);

		double a = 0.5 * (a1 + a2 + dst1 + dst2);
		double h = -0.5 * Newton.G * getIntegrator().M[this.centralBody] / a;
		vf = f1
				* Math.sqrt(2. * (h + Newton.G
						* (getIntegrator().M[this.centralBody] / (a2 + dst2))));

		// ---

		double d1prtc, d2prtc, dorigprtc;
		dorigprtc = f0 * (a2 + dst2);
		d1prtc = f0 * (a2 + dst2);
		euler = 0.5 * vf * vf + vf * dorigprtc - cMu / d1prtc;

		dorigprtc = f0 * dst1 + cMu;
		d1prtc = 1D + f0 * dst1;
		d2prtc = f0 * dst1;

		double A, B, C;
		A = 0.5D;
		B = dorigprtc;
		C = -euler - cMu / d1prtc - mu / d2prtc;

		v0 = 0.5D * (-B + Math.sqrt(B * B - 4D * A * C)) / A;
		v0 = v0 / f1;

		v0 = v0
				- Math.sqrt(Funcions.scal(X, getIntegrator().firstSpeedPos
						+ planet1 * 3, X, getIntegrator().firstSpeedPos
						+ planet1 * 3));
		// ---

		for (i = 0; i < 3; i++) {
			X[getIntegrator().firstSpeedPos + i] = X[getIntegrator().firstSpeedPos
					+ planet1 * 3 + i]
					+ v0 * v[i];
		}

		getIntegrator().vect_etat_esf(planet1, this.section.startBase, X, par);
	}

	public NewtonVar getIntegrator() {
		return this.section.integrator;
	}

	private int calc_angle_aproche_orbite(int de_pl, int a_pl, DoubleRef _ang)
			throws Exception {
		double r1[] = new double[3], r2[] = new double[3], s[] = new double[3], m;
		int i;
		int result;
		double old_pos[] = getIntegrator().position.clone();
		result = getIntegrator().integrer_dfix(
				this.centralBody,
				Funcions.dist(getIntegrator().position, 3 * this.centralBody,
						getIntegrator().position, 3 * a_pl),
				this.section.poincareMaxTime);
		if (result == 0) {
			for (i = 0; i < 3; i++) {
				s[i] = getIntegrator().position[i]
						- getIntegrator().position[this.centralBody * 3 + i];
				r1[i] = getIntegrator().position[a_pl * 3 + i]
						- getIntegrator().position[this.centralBody * 3 + i];
				r2[i] = getIntegrator().position[getIntegrator().firstSpeedPos
						+ a_pl * 3 + i]
						- getIntegrator().position[getIntegrator().firstSpeedPos
								+ this.centralBody * 3 + i];
			}
			Funcions.normaliser(r1);
			Funcions.normaliser(r2);
			Funcions.normaliser(s);
			m = Funcions.scal(r1, r2);
			for (i = 0; i < 3; i++) {
				r2[i] -= m * r1[i];
			}
			Funcions.normaliser(r2);

			_ang.d = Math.atan2(Funcions.scal(s, r2), Funcions.scal(s, r1));
		}
		getIntegrator().copyVector(old_pos, getIntegrator().position);
		return result;
	}

	@Operation("Launch")
	@Action
	public void planetLaunch() throws Exception {
		initialPlanetAprox();
		this.section.poincareMap();
		this.section.toStartPosition();
	}

	public double initialPlanetAprox() throws Exception {
		double vang;
		DoubleRef ang = new DoubleRef();
		double tini;
		int i;
		double d1, d2;
		double periode;

		getIntegrator().initPos();
		this.section.poincareStartVector = new double[6];
		d1 = Funcions.dist(getIntegrator().position,
				3 * this.section.poincareStartBody, getIntegrator().position,
				3 * this.centralBody);
		d2 = Funcions.dist(getIntegrator().position,
				3 * this.section.poincareEndBody, getIntegrator().position,
				3 * this.centralBody);
		vang = Math
				.sqrt(Newton.G
						* (getIntegrator().M[this.centralBody] + getIntegrator().M[this.section.poincareStartBody])
						/ (d1 * d1 * d1))
				- Math.sqrt(Newton.G
						* (getIntegrator().M[this.centralBody] + getIntegrator().M[this.section.poincareEndBody])
						/ (d2 * d2 * d2));
		periode = 2. * Math.PI / vang;
		tini = this.aproxInitialTime - getIntegrator().epoch;
		i = 4;
		while (true) {
			while (tini < this.aproxInitialTime - getIntegrator().epoch) {
				tini += Math.abs(periode);
			}
			getIntegrator().initPos(tini);
			param_aproche_orbite(this.section.poincareStartBody,
					this.section.poincareEndBody,
					this.aproxInitialStartBodyDistance,
					this.aproxInitialEndBodyDistance, getIntegrator().position,
					this.section.poincareStartVector);
			this.section.poincareStartVector[0] = tini;

			if (i-- == 0) {
				break;
			}
			calc_angle_aproche_orbite(this.section.poincareStartBody,
					this.section.poincareEndBody, ang);
			tini -= ang.d / vang;
		}

		return periode;
	}

	@Action
	@Operation("Flyby")
	public int distanceApproximation() throws Exception {
		setEpochNow();
		int result;
		this.totalPendingIter = this.aproxMaxIter;
		result = gradient_optimize();
		if (result == 0) {
			result = approcher_rafiner();
		}
		this.section.toStartPosition();
		return result;
	}

	public void setEpochNow() {
		this.section.setEpochNow();
	}

	@Inspect
	public double getFunctionEnergy() {
		return 0;
	}

	@Action
	@Operation("Poincare Map")
	public int poincareMap() throws Exception {
		int res = this.section.poincareMap();
		this.section.toStartPosition();
		return res;
	}

}
