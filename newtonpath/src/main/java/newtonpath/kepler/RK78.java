package newtonpath.kepler;

import newtonpath.kepler.Funcions.Read;
import newtonpath.kepler.Funcions.Write;

public abstract class RK78 {
	private final static double rk78_Alfa[] = { 0.e0D, 2.e0D / 27.e0D,
			1.e0D / 9.e0D, 1.e0D / 6.e0D, 5.e0D / 12.e0D, .5e0, 5.e0D / 6.e0D,
			1.e0D / 6.e0D, 2.e0D / 3.e0D, 1.e0D / 3.e0D, 1.e0D, 0.e0D, 1.e0D };
	private final static double rk78_Beta[] = { 0.e0D, 2.e0D / 27.e0D,
			1.e0D / 36.e0D, 1.e0D / 12.e0D, 1.e0D / 24.e0D, 0.e0D,
			1.e0D / 8.e0D, 5.e0D / 12.e0D, 0.e0D, -25.e0D / 16.e0D,
			25.e0D / 16.e0D, .5e-1, 0.e0D, 0.e0D, .25e0, .2e0,
			-25.e0D / 108.e0D, 0.e0D, 0.e0D, 125.e0D / 108.e0D,
			-65.e0D / 27.e0D, 125.e0D / 54.e0D, 31.e0D / 300.e0D, 0.e0D, 0.e0D,
			0.e0D, 61.e0D / 225.e0D, -2.e0D / 9.e0D, 13.e0D / 900.e0D, 2.e0D,
			0.e0D, 0.e0D, -53.e0D / 6.e0D, 704.e0D / 45.e0D, -107.e0D / 9.e0D,
			67.e0D / 90.e0D, 3.e0D, -91.e0D / 108.e0D, 0.e0D, 0.e0D,
			23.e0D / 108.e0D, -976.e0D / 135.e0D, 311.e0D / 54.e0D,
			-19.e0D / 60.e0D, 17.e0D / 6.e0D, -1.e0D / 12.e0D,
			2383.e0D / 4100.e0D, 0.e0D, 0.e0D, -341.e0D / 164.e0D,
			4496.e0D / 1025.e0D, -301.e0D / 82.e0D, 2133.e0D / 4100.e0D,
			45.e0D / 82.e0D, 45.e0D / 164.e0D, 18.e0D / 41.e0D,
			3.e0D / 205.e0D, 0.e0D, 0.e0D, 0.e0D, 0.e0D, -6.e0D / 41.e0D,
			-3.e0D / 205.e0D, -3.e0D / 41.e0D, 3.e0D / 41.e0D, 6.e0D / 41.e0D,
			0.e0D, -1777.e0D / 4100.e0D, 0.e0D, 0.e0D, -341.e0D / 164.e0D,
			4496.e0D / 1025.e0D, -289.e0D / 82.e0D, 2193.e0D / 4100.e0D,
			51.e0D / 82.e0D, 33.e0D / 164.e0D, 12.e0D / 41.e0D, 0.e0D, 1.e0D };
	private final static double rk78_c[] = { 41.e0D / 840.e0D, 0.e0D, 0.e0D,
			0.e0D, 0.e0D, 34.e0D / 105.e0D, 9.e0D / 35.e0D, 9.e0D / 35.e0D,
			9.e0D / 280.e0D, 9.e0D / 280.e0D, 41.e0D / 840.e0D };
	private final static double rk78_cp[] = { 0.e0D, 0.e0D, 0.e0D, 0.e0D,
			0.e0D, 34.e0D / 105.e0D, 9.e0D / 35.e0D, 9.e0D / 35.e0D,
			9.e0D / 280.e0D, 9.e0D / 280.e0D, 0.e0D, 41.e0D / 840.e0D,
			41.e0D / 840.e0D };

	@Write
	public int flag;
	@Write
	protected double timeStep;
	@Write
	public double paramMinStep;
	@Write
	public double paramMaxStep;
	@Write
	protected double paramRelErr;
	@Write
	protected double paramAbsErr;
	@Read
	final protected int dimension;
	@Write
	final public double position[];
	@Write
	protected double time;
	@Write
	final private double work[], subwork[];

	public RK78(double h, double hmin, double hmax, double relerr,
			double abserr, int n) {
		super();
		this.timeStep = h;
		this.paramMinStep = hmin;
		this.paramMaxStep = hmax;
		this.paramRelErr = relerr;
		this.paramAbsErr = abserr;
		this.dimension = n;
		this.work = new double[15 * this.dimension];
		this.subwork = new double[this.dimension];
		this.position = new double[this.dimension];
		this.flag = 4;
	}

	abstract void vectorField(double t, double x[], double y[])
			throws Exception;

	public final int oneStep() throws Exception {
		return this.flag = oneStep(this.paramMaxStep, this.flag);
	}

	public final int oneStep(double paramMaxStep) throws Exception {
		return oneStep(paramMaxStep, 4);
	}

	public final int oneStep(double paramMaxStep, int flag) throws Exception
	/*----------------------------------------------------------------------

	    this subroutine is an implementation of the runge-kutta-fehlberg
	    method of orders  7  and  8   using a total of 13 steps (and
	    evaluations of the vectorfield) it computes two different
	    estimations of the next point. the difference between both
	    estimations (with local errors of order 8 and 9) is computed
	    and the l1 norm obteined. this norm is divided by n (the number
	    of equations). the number obteined in this way is required to
	    be less than a given tolerance e1 times  (1+.01*dd), were dd is the
	    l1 norm of the point computed to the order 8. if this requirement
	    is satisfied the order 8 estimation is taken as a next point.
	    if not, a suitable value of the step h is obteined and the
	    computation is started again.
	    in any case, when the next point is computed, a prediction
	    of the step h, to be used in the next step, is done.

	    input parameters

	    difeq----------------- the name of the subroutine computing the
	                           vector field (to be declared external in
	                           the calling program) writen as a system
	                           of first order differrential equations.
	    n--------------------- the dimension of the dependent variable.
	                           It is equal to the number of differential
	                           equations.
	    y--------------------- the current value of the dependent variable
	    x--------------------- the current value of the independent variable
	    h--------------------- the time step to be used
	    hmin------------------ the minimum allowed value for the absolute
	                           value of h
	    hmax------------------ the maximum allowed value for the absolute
	                           value of h
	    relerr,abserr--------- relative and absolute error tolerances
	                           for local error test.
	    iflag----------------- indicates status of integration.
	                           set iflag = 4 for the first call to rk78.

	   the user must provide storage in his calling program for the arrays
	   in the call list:
	        y(n)

	   difeq must be declared in an external statement and it has to be of
	   the form :
	        difeq(t,y,n,yp)
	   to evaluate:
	        dy/dt = yp,  where  y  = (y(1) ,...,y(n))
	                            yp = (yp(1),...,yp(n))

	   output parameters:

	    difeq----------------- unchanged
	    n--------------------- unchanged
	    y--------------------- the  estimated next value of the dependent.
	                              variable.
	    x--------------------- the next value of the independent variable.
	    h--------------------- the time step to be used in the next call
	                              of this subroutine.
	    hmin------------------ unchanged
	    hmax------------------ unchanged
	    relerr, abserr-------- unchanged
	    iflag  = 4  ---------- normal return. neither hmin nor hmax have
	                              been used.
	    iflag  = 1  ---------- hmin has been used in any step.
	    iflag  = 2  ---------- hmax has been used in any step.
	    iflag  = 3  ---------- hmin and hmax have been used in any step.

	-----------------------------------------------------------------------*/
	{
		int j, k, l, jk, jl, kl;
		double a, bet, d, dd, e3, v;
		double signe;

		signe = (((this.timeStep) > 0D) ? 1D : (((this.timeStep) < 0D) ? -1D
				: 0D));

		for (;;) {
			jk = 0;
			for (j = 0; j < 13; j++) {
				for (l = 0; l < this.dimension; l++) {
					this.work[l] = this.position[l];
				}
				a = this.time + rk78_Alfa[j] * (this.timeStep);
				for (k = 0; k < j; k++) {
					++jk;
					bet = rk78_Beta[jk] * (this.timeStep);
					for (l = 0, kl = (k + 2) * this.dimension; l < this.dimension; l++, kl++) {
						this.work[l] += bet * this.work[kl];
					}
				}
				vectorField(a, this.work, this.subwork);
				for (l = 0, jl = (j + 2) * this.dimension; l < this.dimension; l++, jl++) {
					this.work[jl] = this.work[this.dimension + l] = this.subwork[l];
				}
			}
			d = dd = 0.e0;
			for (l = 0; l < this.dimension; l++) {
				this.work[l] = this.work[l + this.dimension] = this.position[l];
				for (k = 0, kl = 2 * this.dimension + l; k < 11; k++, kl += this.dimension) {
					bet = (this.timeStep) * this.work[kl];
					this.work[l] += bet * rk78_c[k];
					this.work[l + this.dimension] += bet * rk78_cp[k];
				}
				this.work[l + this.dimension] += (this.timeStep)
						* (rk78_cp[11] * this.work[13 * this.dimension + l] + rk78_cp[12]
								* this.work[14 * this.dimension + l]);
				d += Math.abs(this.work[l + this.dimension] - this.work[l]);
				dd += Math.abs(this.work[l + this.dimension]);
			}
			e3 = this.paramAbsErr + this.paramRelErr * dd;
			if (d < e3) {
				break; // goto e4;
			}
			v = this.timeStep - this.paramMinStep;
			if (Math.abs(v) <= (2.e-16) * (1 + Math.abs(this.paramMinStep))) {
				break; // goto e4;
			}

			(this.timeStep) *= 0.9e0 * Math.exp(0.125e0 * Math.log(e3 / d));
			if (Math.abs((this.timeStep)) < this.paramMinStep) {
				this.timeStep = this.paramMinStep * signe;
				if (flag == 2 || flag == 3) {
					flag = 3;
				} else {
					flag = 1;
				}
			}
			if (Math.abs((this.timeStep)) > paramMaxStep) {
				this.timeStep = paramMaxStep * signe;
				if (flag == 1 || flag == 3) {
					flag = 3;
				} else {
					flag = 2;
				}
			}
		}
		// e4:
		d = (d > (v = e3 / 256)) ? d : v;
		this.time += (this.timeStep);
		(this.timeStep) *= 0.9e0 * Math.exp(0.125e0 * Math.log(e3 / d));
		if (Math.abs((this.timeStep)) > paramMaxStep) {
			this.timeStep = paramMaxStep * signe;
			flag = (flag & 3) | 2;
		}
		if (Math.abs((this.timeStep)) < this.paramMinStep) {
			this.timeStep = this.paramMinStep * signe;
			flag = (flag & 3) | 1;
		}
		for (l = 0, k = this.dimension; l < this.dimension; l++, k++) {
			this.position[l] = this.work[k];
		}
		return flag;
	}

	public void timeIntegration(double _dtemps) throws Exception {
		double t;
		int i;

		this.flag = 4;
		t = this.time + _dtemps;
		this.timeStep = Math.abs(this.timeStep);
		this.timeStep = (_dtemps < 0.) ? -this.timeStep : this.timeStep;
		if (_dtemps < 0) {
			while (this.time > t + this.timeStep) {
				oneStep();
			}
		} else {
			while (this.time < t - this.timeStep) {
				oneStep();
			}
		}
		double oldh = this.timeStep;

		int oldflag = this.flag;
		this.flag = 4;
		for (i = 0; i < 4 && ((this.flag & 2) == 0); i++) {
			this.timeStep = t - this.time;
			oneStep(Math.abs(this.timeStep));
		}
		this.timeStep = oldh;
		this.flag = oldflag;
	}

	public void minTimeIntegration(double _dtemps, boolean _dibuixar)
			throws Exception {
		double t, oldMaxTime, oldStep;

		this.flag = 4;
		t = 0.;
		this.timeStep = (_dtemps < 0.) ? -this.paramMinStep : this.paramMinStep;
		oldMaxTime = this.paramMaxStep;
		oldStep = this.timeStep;
		while (Math.abs(t) < Math.abs(_dtemps) - this.paramMinStep) {
			oldStep = this.timeStep;
			oneStep();
			t += this.timeStep;
			if (Math.abs(t - _dtemps) < this.paramMaxStep) {
				this.paramMaxStep = Math.abs(t - _dtemps);
			}
		}
		this.timeStep = oldStep;
		this.paramMaxStep = oldMaxTime;
	}

	protected void copyVector(double de[], double a[]) {
		int i;
		for (i = 0; i < this.dimension; i++) {
			a[i] = de[i];
		}
	}

	public void copyPositionInto(double destination[]) {
		copyVector(this.position, destination);
	}

	public double[] clonePosition() {
		return this.position.clone();
	}

	public void setPosition(double newTime, double newVal[]) {
		this.time = newTime;
		copyVector(newVal, this.position);
		this.flag = 4;
	}

	public static double[] cloneArray(double x[]) {
		if (x == null) {
			return null;
		}
		return x.clone();
	}

	public static int[] cloneArrayInt(int x[]) {
		if (x == null) {
			return null;
		}
		return x.clone();
	}

	public double getTime() {
		return this.time;
	}

}
