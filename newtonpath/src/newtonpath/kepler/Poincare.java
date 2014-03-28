package newtonpath.kepler;

import newtonpath.kepler.Funcions.Parameter;
import newtonpath.kepler.Funcions.Read;
import newtonpath.kepler.Funcions.Write;

public class Poincare {
	@Write
	@Parameter({ "Distance Optimization", "Poincare Map" })
	public double startBase[][] = { { 1D, 0D, 0D }, { 0D, 1D, 0D },
			{ 0D, 0D, 1D } };
	@Write
	@Parameter({ "Distance Optimization", "Poincare Map" })
	public double endBase[][] = { { 1D, 0D, 0D }, { 0D, 1D, 0D },
			{ 0D, 0D, 1D } };
	@Write
	@Parameter({ "Distance Optimization", "Poincare Map" })
	public int poincareMaxIterRef = 20;
	@Write
	@Parameter({ "Distance Optimization", "Poincare Map" })
	public double poincareStartVector[] = new double[6];
	@Read
	public double poincareEndVector[];
	@Read
	public double poincareDifMap[];
	@Write
	@Parameter({ "Distance Optimization", "Poincare Map" })
	public double poincareMinTime = 0D;
	@Write
	@Parameter({ "Distance Optimization", "Poincare Map" })
	public double poincareMaxTime = 10000D;
	@Read
	public double poincareStartPoint[];
	@Read
	public double poincareEndPoint[];
	@Write
	@Parameter({ "Distance Optimization", "Launch to planet", "Poincare Map" })
	public int poincareStartBody = 1;
	@Write
	@Parameter({ "Distance Optimization", "Launch to planet", "Poincare Map" })
	public int poincareEndBody = 2;
	@Read
	public int poincareResult = -1;
	@Write
	public int poincareSense = 1;
	@Read
	public int poincarePoints = 0;

	@Write
	@Parameter({ "Distance Optimization", "Launch to planet", "Poincare Map" })
	public double epoch = 0;

	public NewtonVar integrator;

	public Poincare(double[] _masses) {
		this.integrator = new NewtonVar(_masses);
		this.poincareStartBody = 1;
		this.poincareEndBody = 2;
		this.epoch = this.integrator.epoch;
	}

	public void setAllBases(int body) throws Exception {
		double de1[], de2[], de3[];
		de1 = new double[3];
		de2 = new double[3];
		de3 = new double[3];
		this.integrator.reference_planete(body, this.startBase[0],
				this.startBase[1], this.startBase[2], de1, de2, de3);
		for (int i = 0; i < this.startBase.length; i++) {
			for (int j = 0; j < this.startBase[i].length; j++) {
				this.endBase[i] = this.startBase[i];
			}
		}
	}

	public void normalizeStartPointHorizontalBase() throws Exception {
		setBaseHorizontal(this.poincareStartBody, this.startBase);
	}

	public void normalizeEndPointHorizontalBase() throws Exception {
		setBaseHorizontal(this.poincareEndBody, this.endBase);
	}

	public void setBaseHorizontal(int body, double[][] x) throws Exception {
		double de1[], de2[], de3[];
		de1 = new double[3];
		de2 = new double[3];
		de3 = new double[3];
		this.integrator
				.reference_planete(body, x[1], x[2], x[0], de1, de2, de3);
	}

	public double getPosition(int coordinate) {
		return this.integrator.position[coordinate];
	}

	public double getSpeed(int coordinate) {
		return this.integrator.position[this.integrator.firstSpeedPos
				+ coordinate];
	}

	private int integrer_poincare(int corps, double delta_t_max)
			throws Exception {

		boolean signe_pos;
		double denom;
		int maxiter;
		int resultat;

		double val_newton[] = new double[this.integrator.dimension];

		double dpos[] = new double[3], dvel[] = new double[3];

		double valscal;

		double t0 = this.integrator.getTime();

		this.integrator.timeStep = delta_t_max < 0. ? -this.integrator.paramMinStep
				: this.integrator.paramMinStep;

		dpos[0] = getPosition(0) - getPosition(3 * corps + 0);
		dpos[1] = getPosition(1) - getPosition(3 * corps + 1);
		dpos[2] = getPosition(2) - getPosition(3 * corps + 2);

		dvel[0] = getSpeed(0) - getSpeed(3 * corps + 0);
		dvel[1] = getSpeed(1) - getSpeed(3 * corps + 1);
		dvel[2] = getSpeed(2) - getSpeed(3 * corps + 2);

		signe_pos = Funcions.scal(dpos, dvel) > 0D;

		resultat = ErrorCode.SectionDomain.getErrorNumber();

		while (Math.abs(this.integrator.getTime() - t0) < Math.abs(delta_t_max)) {
			this.integrator.oneStep();
			dpos[0] = getPosition(0) - getPosition(3 * corps + 0);
			dpos[1] = getPosition(1) - getPosition(3 * corps + 1);
			dpos[2] = getPosition(2) - getPosition(3 * corps + 2);

			dvel[0] = getSpeed(0) - getSpeed(3 * corps + 0);
			dvel[1] = getSpeed(1) - getSpeed(3 * corps + 1);
			dvel[2] = getSpeed(2) - getSpeed(3 * corps + 2);

			valscal = Funcions.scal(dpos, dvel);

			if (valscal > 0. && !signe_pos || valscal < 0. && signe_pos) {
				for (maxiter = this.poincareMaxIterRef; maxiter > 0; maxiter--) {
					this.integrator.vectorField(this.integrator.getTime(),
							this.integrator.position, val_newton);
					denom = Funcions.scal(val_newton, dvel)
							+ Funcions.scal(dpos, 0, val_newton,
									this.integrator.firstSpeedPos)
							- Funcions.scal(val_newton, 3 * corps, dvel, 0)
							- Funcions.scal(dpos, 0, val_newton,
									this.integrator.firstSpeedPos + 3 * corps);
					if (Math.abs(denom) < this.integrator.paramTOL) {
						resultat = ErrorCode.SingularSectionEndpoint
								.getErrorNumber();
						break;
					}

					double timeStep = -valscal / denom;
					this.integrator.timeStep = timeStep;
					if (Math.abs(timeStep) < this.integrator.paramMinStep) {
						resultat = 0;
						break;
					}

					this.integrator.oneStep(Math.abs(timeStep));

					dpos[0] = getPosition(0) - getPosition(3 * corps + 0);
					dpos[1] = getPosition(1) - getPosition(3 * corps + 1);
					dpos[2] = getPosition(2) - getPosition(3 * corps + 2);

					dvel[0] = getSpeed(0) - getSpeed(3 * corps + 0);
					dvel[1] = getSpeed(1) - getSpeed(3 * corps + 1);
					dvel[2] = getSpeed(2) - getSpeed(3 * corps + 2);

					valscal = Funcions.scal(dpos, dvel);
					if (Math.abs(valscal) < this.integrator.paramAbsErr) {
						resultat = 0;
						break;
					}
				}
				break;
			}
		}
		return resultat;
	}

	private int poincare_prep(int corps, double x[], double t0, double p[],
			double dp[], double dt0p[], double dtau[], DoubleRef _dt0tau,
			double dpla[], double dt0pla[], DoubleRef _dist, double dd_dx0[],
			DoubleRef _dd_dt0, DoubleRef _vel, double dvel_dx[],
			DoubleRef _dvel_dt, double tmin, double tmax) throws Exception {
		int result;
		int i, j;
		double Nfin[] = new double[this.integrator.dimension], Nini[] = new double[this.integrator.dimension];
		double denom;

		double X_ini[];
		double X_fin[];

		X_ini = this.integrator.clonePosition();
		if (tmin != 0) {
			this.integrator.minTimeIntegration(tmax > 0D ? Math.abs(tmin)
					: -Math.abs(tmin), false);
		}
		result = integrer_poincare(corps, tmax);
		X_fin = this.integrator.clonePosition();
		for (i = 0; i < 6; i++) {
			p[i] = 0.;
			dt0p[i] = 0.;
			dtau[i] = 0.;
		}
		for (i = 0; i < 6 * 6; i++) {
			dp[i] = 0.;
		}
		int firstSpeedPos = this.integrator.firstSpeedPos;
		int firstVarPos = this.integrator.firstVarPos;
		if (result == 0) {
			p[0] = X_fin[0];
			p[1] = X_fin[1];
			p[2] = X_fin[2];
			p[3 + 0] = X_fin[firstSpeedPos + 0];
			p[3 + 1] = X_fin[firstSpeedPos + 1];
			p[3 + 2] = X_fin[firstSpeedPos + 2];
			this.integrator.vectorField(t0, X_fin, Nfin);
			this.integrator.vectorField(this.integrator.getTime(), X_ini, Nini);
			denom = 0.;
			for (i = 0; i < 3; i++) {
				denom += (X_fin[firstSpeedPos + i] - X_fin[firstSpeedPos + 3
						* corps + i])
						* (Nfin[i] - Nfin[3 * corps + i])
						+ (X_fin[i] - X_fin[3 * corps + i])
						* (Nfin[firstSpeedPos + i] - Nfin[firstSpeedPos + 3
								* corps + i]);
			}
			if (Math.abs(denom) < this.integrator.paramTOL) {
				result = ErrorCode.UnefinedSectionDifferential.getErrorNumber();
			} else {

				for (i = 0; i < 6; i++) {
					for (j = 0; j < 3; j++) {
						dtau[i] -= ((X_fin[firstSpeedPos + j] - X_fin[firstSpeedPos
								+ 3 * corps + j])
								* X_fin[firstVarPos + 6 * i + j] + (X_fin[j] - X_fin[3
								* corps + j])
								* X_fin[firstVarPos + 6 * i + 3 + j])
								/ denom;
					}
				}
				for (i = 0; i < 6; i++) {
					for (j = 0; j < 3; j++) {
						dp[6 * i + j] += Nfin[j] * dtau[i]
								+ X_fin[firstVarPos + 6 * i + j];
						dp[6 * i + 3 + j] += Nfin[firstSpeedPos + j] * dtau[i]
								+ X_fin[firstVarPos + 6 * i + 3 + j];
					}
				}
				_dt0tau.d = 0.;
				for (i = 0; i < 3; i++) {
					_dt0tau.d -= dtau[i] * Nini[i] + dtau[3 + i]
							* Nini[firstSpeedPos + i];
				}
				for (i = 0; i < 3; i++) {
					dt0p[i] = _dt0tau.d * Nfin[i];
					dt0p[3 + i] = _dt0tau.d * Nfin[firstSpeedPos + i];
				}
				for (i = 0; i < 6; i++) {
					for (j = 0; j < 3; j++) {
						dt0p[i] -= X_fin[firstVarPos + 6 * j + i] * Nini[j]
								+ X_fin[firstVarPos + 6 * (j + 3) + i]
								* Nini[firstSpeedPos + j];
					}
				}

				for (i = 0; i < 6; i++) {
					for (j = 0; j < 3; j++) {
						dpla[i * 6 + j] = Nfin[3 * corps + j] * dtau[i];
						dpla[i * 6 + 3 + j] = Nfin[firstSpeedPos + 3 * corps
								+ j]
								* dtau[i];
					}
				}

				for (j = 0; j < 3; j++) {
					dt0pla[j] = Nfin[3 * corps + j] * _dt0tau.d;
					dt0pla[3 + j] = Nfin[firstSpeedPos + 3 * corps + j]
							* _dt0tau.d;
				}
			}
		}

		_dist.d = Funcions.dist(X_fin, 0, X_fin, 3 * corps);
		_vel.d = Math.sqrt(Funcions.scal(X_fin, firstSpeedPos, X_fin,
				firstSpeedPos));
		for (i = 0; i < 6; i++) {
			dd_dx0[i] = 0.;
			dvel_dx[i] = 0.;
			for (j = 0; j < 3; j++) {
				dd_dx0[i] += (X_fin[j] - X_fin[3 * corps + j])
						* (dp[6 * i + j] - dpla[6 * i + j]) / _dist.d;
				dvel_dx[i] += X_fin[firstSpeedPos + j] * dp[6 * i + 3 + j]
						/ _vel.d;
			}
		}

		_dd_dt0.d = 0.;
		_dvel_dt.d = 0.;
		for (j = 0; j < 3; j++) {
			_dd_dt0.d += (X_fin[j] - X_fin[3 * corps + j])
					* (dt0p[j] - dt0pla[j]) / _dist.d;
			_dvel_dt.d += X_fin[firstSpeedPos + j] * dt0p[3 + j] / _vel.d;
		}

		return result;
	}

	protected int poincare_esf(int _startBody, int _endBody, double vpar[],
			double val[], double dif[], double tmin, double tmax)
			throws Exception {
		this.poincareMinTime = tmin;
		this.poincareMaxTime = tmax;
		int result = poincare_esf(_startBody, _endBody, vpar);
		if (result == 0) {
			Funcions.copySubTable(this.poincareEndVector, 0, val, 0, val.length);
			Funcions.copySubTable(this.poincareDifMap, 0, dif, 0, dif.length);
		}
		return result;
	}

	protected int poincare_esf(int _startBody, int _endBody, double vpar[])
			throws Exception {
		this.poincareStartBody = _startBody;
		this.poincareEndBody = _endBody;
		this.poincareStartVector = vpar.clone();
		int result = poincareMap();
		return result;
	}

	public int poincareMap() throws Exception {
		this.poincarePoints = 0;
		double refA[] = Funcions.refenlinia(this.startBase);
		double refB[] = Funcions.refenlinia(this.endBase);

		this.poincareEndVector = new double[6];
		this.poincareDifMap = new double[6 * 6];

		DoubleRef dtaudt = new DoubleRef();
		DoubleRef dist = new DoubleRef();
		DoubleRef dd_dt0 = new DoubleRef();
		DoubleRef vel = new DoubleRef();
		DoubleRef dvel_dt = new DoubleRef();

		double dtau[] = new double[6];
		double p[] = new double[6], dp[] = new double[6 * 6], dt0p[] = new double[6];
		double x[] = new double[6];
		double calpha, ctheta, cphi;
		double salpha, stheta, sphi;

		double dpla[] = new double[6 * 6], dt0pla[] = new double[6];

		double dd_dx0[] = new double[6];

		double dvel_dx[] = new double[6];

		double a1[] = new double[3], a2[] = new double[3], a3[] = new double[3], da1_dtheta[] = new double[3], da2_dtheta[] = new double[3], da1_dphi[] = new double[3], da2_dphi[] = new double[3], da3_dphi[] = new double[3];

		int i, j;

		double vcosth, vsinth, vcosph, vsinph, vcosal, vsinal;
		double r[] = new double[3], v[] = new double[3], dr[] = new double[6 * 3], dv[] = new double[6 * 3], dt[] = new double[6];

		int dim = this.integrator.dimension;
		int speedPos = this.integrator.firstSpeedPos;

		double Nini[] = new double[dim];
		double Nfin[] = new double[dim];

		this.poincareResult = ErrorCode.SectionDomain.getErrorNumber();
		this.poincareStartPoint = this.poincareEndPoint = null;

		this.integrator.initPos(this.poincareStartVector[0]);

		ctheta = Math.cos(this.poincareStartVector[2]);
		stheta = Math.sin(this.poincareStartVector[2]);
		cphi = Math.cos(this.poincareStartVector[3]);
		sphi = Math.sin(this.poincareStartVector[3]);
		calpha = Math.cos(this.poincareStartVector[5]);
		salpha = Math.sin(this.poincareStartVector[5]);

		for (i = 0; i < 3; i++) {
			a1[i] = stheta * cphi * refA[i] + stheta * sphi * refA[3 + i]
					+ ctheta * refA[6 + i];
			a2[i] = -ctheta * cphi * refA[i] - ctheta * sphi * refA[3 + i]
					+ stheta * refA[6 + i];
			a3[i] = sphi * refA[i] - cphi * refA[3 + i];
			x[i] = this.integrator.position[3 * this.poincareStartBody + i]
					+ this.poincareStartVector[1] * a1[i];
			x[3 + i] = this.integrator.position[speedPos + 3
					* this.poincareStartBody + i]
					+ this.poincareStartVector[4]
					* (calpha * a2[i] + salpha * a3[i]);

			da1_dtheta[i] = -a2[i];
			da2_dtheta[i] = a1[i];
			da1_dphi[i] = stheta * (cphi * refA[3 + i] - sphi * refA[i]);
			da2_dphi[i] = ctheta * (sphi * refA[i] - cphi * refA[3 + i]);
			da3_dphi[i] = cphi * refA[i] + sphi * refA[3 + i];
		}

		this.integrator.setBodyState(0, x);

		this.poincareStartPoint = this.integrator.position.clone();
		this.poincareResult = poincare_prep(this.poincareEndBody, x,
				this.poincareStartVector[0], p, dp, dt0p, dtau, dtaudt, dpla,
				dt0pla, dist, dd_dx0, dd_dt0, vel, dvel_dx, dvel_dt,
				this.poincareMinTime,
				this.poincareSense > 0 ? this.poincareMaxTime
						: -this.poincareMaxTime);

		if (this.poincareResult == 0) {
			this.poincareEndPoint = this.integrator.position.clone();
			this.integrator.vectorField(this.poincareStartVector[0],
					this.poincareEndPoint, Nfin);
			this.integrator.vectorField(this.integrator.getTime(),
					this.poincareStartPoint, Nini);

			for (j = 0; j < 3; j++) {
				r[j] = this.poincareEndPoint[j]
						- this.poincareEndPoint[3 * this.poincareEndBody + j];
				v[j] = this.poincareEndPoint[speedPos + j]
						- this.poincareEndPoint[speedPos + 3
								* this.poincareEndBody + j];
			}
			vel.d = Math.sqrt(Funcions.scal(v, v));
			vcosth = Funcions.scal(r, 0, refB, 6) / dist.d;
			vsinth = Math.sqrt(1. - vcosth * vcosth);
			vcosph = Funcions.scal(r, 0, refB, 0) / (dist.d * vsinth);
			vsinph = Funcions.scal(r, 0, refB, 3) / (dist.d * vsinth);
			vcosal = Funcions.scal(v, 0, refB, 6) / (vel.d * vsinth);
			vsinal = (Funcions.scal(v, 0, refB, 0) * vsinph - Funcions.scal(v,
					0, refB, 3) * vcosph)
					/ vel.d;

			this.poincareEndVector[0] = this.integrator.getTime();
			this.poincareEndVector[1] = dist.d;
			this.poincareEndVector[2] = Math.acos(vcosth);
			this.poincareEndVector[3] = Math.atan2(vsinph, vcosph);
			this.poincareEndVector[4] = vel.d;
			this.poincareEndVector[5] = Math.atan2(vsinal, vcosal);

			for (j = 0; j < 6; j++) {
				dt[j] = 0.;
				for (i = 0; i < 3; i++) {
					dr[3 * j + i] = 0.;
					dv[3 * j + i] = 0.;
				}
			}

			for (j = 0; j < 3; j++) {
				r[j] = this.poincareEndPoint[j]
						- this.poincareEndPoint[3 * this.poincareEndBody + j];
				dr[j + 3 * 0] = dt0p[j] - Nfin[3 * this.poincareEndBody + j]
						* dtaudt.d;
				dv[j + 3 * 0] = dt0p[3 + j]
						- Nfin[speedPos + 3 * this.poincareEndBody + j]
						* dtaudt.d;
				for (i = 0; i < 3; i++) {
					// Deriv resp t
					dr[j + 3 * 0] += (dp[6 * i + j] - Nfin[3
							* this.poincareEndBody + j]
							* dtau[i])
							* Nini[3 * this.poincareStartBody + i]
							+ (dp[6 * (i + 3) + j] - Nfin[3
									* this.poincareEndBody + j]
									* dtau[i + 3])
							* Nini[speedPos + 3 * this.poincareStartBody + i];

					dv[j + 3 * 0] += (dp[6 * i + j + 3] - Nfin[speedPos + 3
							* this.poincareEndBody + j]
							* dtau[i])
							* Nini[3 * this.poincareStartBody + i]
							+ (dp[6 * (i + 3) + j + 3] - Nfin[speedPos + 3
									* this.poincareEndBody + j]
									* dtau[i + 3])
							* Nini[speedPos + 3 * this.poincareStartBody + i];

					// Deriv resp d
					dr[j + 3 * 1] += (dp[6 * i + j] - Nfin[3
							* this.poincareEndBody + j]
							* dtau[i])
							* a1[i];
					dv[j + 3 * 1] += (dp[6 * i + j + 3] - Nfin[speedPos + 3
							* this.poincareEndBody + j]
							* dtau[i])
							* a1[i];
					// Deriv resp theta
					dr[j + 3 * 2] += (dp[6 * i + j] - Nfin[3
							* this.poincareEndBody + j]
							* dtau[i])
							* this.poincareStartVector[1]
							* da1_dtheta[i]
							+ (dp[6 * (i + 3) + j] - Nfin[3
									* this.poincareEndBody + j]
									* dtau[i + 3])
							* this.poincareStartVector[4]
							* (calpha * da2_dtheta[i]);
					dv[j + 3 * 2] += (dp[6 * i + j + 3] - Nfin[speedPos + 3
							* this.poincareEndBody + j]
							* dtau[i])
							* this.poincareStartVector[1]
							* da1_dtheta[i]
							+ (dp[6 * (i + 3) + j + 3] - Nfin[speedPos + 3
									* this.poincareEndBody + j]
									* dtau[i + 3])
							* this.poincareStartVector[4]
							* (calpha * da2_dtheta[i]);
					// Deriv resp phi
					dr[j + 3 * 3] += (dp[6 * i + j] - Nfin[3
							* this.poincareEndBody + j]
							* dtau[i])
							* this.poincareStartVector[1]
							* da1_dphi[i]
							+ (dp[6 * (i + 3) + j] - Nfin[3
									* this.poincareEndBody + j]
									* dtau[i + 3])
							* this.poincareStartVector[4]
							* (calpha * da2_dphi[i] + salpha * da3_dphi[i]);
					dv[j + 3 * 3] += (dp[6 * i + j + 3] - Nfin[speedPos + 3
							* this.poincareEndBody + j]
							* dtau[i])
							* this.poincareStartVector[1]
							* da1_dphi[i]
							+ (dp[6 * (i + 3) + j + 3] - Nfin[speedPos + 3
									* this.poincareEndBody + j]
									* dtau[i + 3])
							* this.poincareStartVector[4]
							* (calpha * da2_dphi[i] + salpha * da3_dphi[i]);
					// Deriv resp v
					dr[j + 3 * 4] += (dp[6 * (i + 3) + j] - Nfin[3
							* this.poincareEndBody + j]
							* dtau[i + 3])
							* (calpha * a2[i] + salpha * a3[i]);
					dv[j + 3 * 4] += (dp[6 * (i + 3) + j + 3] - Nfin[speedPos
							+ 3 * this.poincareEndBody + j]
							* dtau[i + 3])
							* (calpha * a2[i] + salpha * a3[i]);
					// Deriv resp alpha
					dr[j + 3 * 5] += (dp[6 * (i + 3) + j] - Nfin[3
							* this.poincareEndBody + j]
							* dtau[i + 3])
							* this.poincareStartVector[4]
							* (-salpha * a2[i] + calpha * a3[i]);
					dv[j + 3 * 5] += (dp[6 * (i + 3) + j + 3] - Nfin[speedPos
							+ 3 * this.poincareEndBody + j]
							* dtau[i + 3])
							* this.poincareStartVector[4]
							* (-salpha * a2[i] + calpha * a3[i]);
				}
			}

			dt[0] = dtaudt.d;
			for (i = 0; i < 3; i++) {
				dt[0] += dtau[i] * Nini[3 * this.poincareStartBody + i]
						+ dtau[i + 3]
						* Nini[speedPos + 3 * this.poincareStartBody + i];
				dt[1] += dtau[i] * a1[i];
				dt[2] += dtau[i] * this.poincareStartVector[1] * da1_dtheta[i]
						+ dtau[i + 3] * this.poincareStartVector[4]
						* (calpha * da2_dtheta[i]);
				dt[3] += dtau[i] * this.poincareStartVector[1] * da1_dphi[i]
						+ dtau[i + 3] * this.poincareStartVector[4]
						* (calpha * da2_dphi[i] + salpha * da3_dphi[i]);
				dt[4] += dtau[i + 3] * (calpha * a2[i] + salpha * a3[i]);
				dt[5] += dtau[i + 3] * this.poincareStartVector[4]
						* (-salpha * a2[i] + calpha * a3[i]);
			}

			for (i = 0; i < 6; i++) {
				this.poincareDifMap[0 + 6 * i] = dt[i];
				this.poincareDifMap[1 + 6 * i] = Funcions.scal(r, 0, dr, 3 * i)
						/ dist.d;
				this.poincareDifMap[2 + 6 * i] = (vcosth
						* this.poincareDifMap[1 + 6 * i] - Funcions.scal(dr,
						3 * i, refB, 6)) / (dist.d * vsinth);
				this.poincareDifMap[3 + 6 * i] = (vcosph
						* (this.poincareDifMap[1 + 6 * i] * vsinth + dist.d
								* vcosth * this.poincareDifMap[2 + 6 * i]) - Funcions
							.scal(dr, 3 * i, refB, 0))
						/ (dist.d * vsinth * vsinph);
				this.poincareDifMap[4 + 6 * i] = Funcions.scal(v, 0, dv, 3 * i)
						/ vel.d;
				this.poincareDifMap[5 + 6 * i] = (vcosal
						* (this.poincareDifMap[4 + 6 * i] * vsinth + vel.d
								* vcosth * this.poincareDifMap[2 + 6 * i]) - Funcions
							.scal(dv, 3 * i, refB, 6))
						/ (vel.d * vsinth * vsinal);
				this.poincareDifMap[5 + 6 * i] = ((Funcions.scal(dv, 3 * i,
						refB, 0)
						* vsinph
						+ Funcions.scal(v, 0, refB, 0)
						* vcosph
						* this.poincareDifMap[3 + 6 * i]
						- Funcions.scal(dv, 3 * i, refB, 3) * vcosph + Funcions
						.scal(v, 0, refB, 3)
						* vsinph
						* this.poincareDifMap[3 + 6 * i])
						/ vel.d - vsinal * Funcions.scal(dv, 3 * i, v, 0)
						/ (vel.d * vel.d))
						/ vcosal;
			}
		} else {
			this.poincareEndVector = this.poincareDifMap = null;
		}
		return this.poincareResult;
	}

	protected int poincare_esf2(int corps0, int corps1, int corps2,
			double par0[], double val1[], double val2[], double dif1[],
			double dif2[], double X0[], double X1[], double X2[], double tmin,
			double tmax) throws Exception {
		int result;
		int i, j, k;
		double difaux[] = new double[6 * 6];

		this.integrator.copyPositionInto(X0);
		result = poincare_esf(corps0, corps1, par0, val1, dif1, tmin, tmax);
		this.integrator.copyPositionInto(X1);
		if (result == 0) {
			result = poincare_esf(corps1, corps2, val1, val2, difaux, tmin,
					tmax);
		}
		this.integrator.copyPositionInto(X2);
		if (result == 0) {
			for (i = 0; i < 6; i++) {
				for (j = 0; j < 6; j++) {
					dif2[i + 6 * j] = 0.;
					for (k = 0; k < 6; k++) {
						dif2[i + 6 * j] += difaux[i + 6 * k] * dif1[k + 6 * j];
					}
				}
			}
		} else {
			this.poincareStartPoint = this.poincareEndPoint = null;
		}
		return result;
	}

	public void toStartPosition() throws Exception {
		if (this.poincareStartPoint != null) {
			this.integrator.setPosition(this.poincareStartVector[0],
					this.poincareStartPoint);
		}
	}

	public void toEndPosition() {
		if (this.poincareEndPoint != null) {
			this.integrator.setPosition(this.poincareEndVector[0],
					this.poincareEndPoint);
		}
	}

	public void setEpochNow() {
		if (this.poincareStartVector != null) {
			this.poincareStartVector[0] -= this.integrator.getTime();
		}
		if (this.poincareEndVector != null) {
			this.poincareEndVector[0] -= this.integrator.getTime();
		}
		this.integrator.setEpochNow();
		this.epoch = this.integrator.epoch;
	}

	public void refreshEpoch() throws Exception {
		this.integrator.toAbsoluteTime(this.epoch);
		this.integrator.setEpochNow();
	}

	public void poincareInvertMap() {
		int i;
		i = this.poincareEndBody;
		this.poincareEndBody = this.poincareStartBody;
		this.poincareStartBody = i;
		this.poincareStartVector = this.poincareEndVector;
		this.poincareEndVector = null;
		this.poincareStartPoint = this.poincareEndPoint;
		this.poincareEndPoint = null;
		this.poincareSense = -this.poincareSense;
		this.poincareResult = -1;
	}

	public void copyState(Poincare orig, Poincare newObj) {
		newObj.startBase[0] = orig.startBase[0].clone();
		newObj.startBase[1] = orig.startBase[1].clone();
		newObj.startBase[2] = orig.startBase[2].clone();
		newObj.endBase[0] = orig.endBase[0].clone();
		newObj.endBase[1] = orig.endBase[1].clone();
		newObj.endBase[2] = orig.endBase[2].clone();
		newObj.poincareDifMap = RK78.cloneArray(orig.poincareDifMap);
		newObj.poincareEndBody = orig.poincareEndBody;
		newObj.poincareEndPoint = RK78.cloneArray(orig.poincareEndPoint);
		newObj.poincareEndVector = RK78.cloneArray(orig.poincareEndVector);
		newObj.poincareMaxIterRef = orig.poincareMaxIterRef;
		newObj.poincareMaxTime = orig.poincareMaxTime;
		newObj.poincareMinTime = orig.poincareMinTime;
		newObj.poincareResult = orig.poincareResult;
		newObj.poincareStartBody = orig.poincareStartBody;
		newObj.poincareStartPoint = RK78.cloneArray(orig.poincareStartPoint);
		newObj.poincareStartVector = RK78.cloneArray(orig.poincareStartVector);
		newObj.poincareSense = orig.poincareSense;
		newObj.epoch = orig.epoch;

		orig.integrator.copyState(orig.integrator, newObj.integrator);
	}

	@Read
	public double calculateStartDV() {
		double r = this.poincareStartVector[1];
		double mass = this.integrator.M[this.poincareStartBody];
		double grav = Newton.G * mass / (r * r);
		double circularOrbitVelocity = Math.sqrt(grav * r);
		return this.poincareStartVector[4] - circularOrbitVelocity;
	}
}
