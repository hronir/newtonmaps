package newtonpath.ui;

import java.awt.Point;

import newtonpath.kepler.Funcions;

public class Projection {

	protected final double invertMap[][] = newMap();
	protected final double projArray[][] = { { 50D, 0D, 0D, 0D },
			{ 0D, -50D, 0D, 0D }, { 0D, 0D, -50D, 0D }, { 0D, 0D, 0D, 1D } };
	protected volatile boolean invalidInvertMap = false;

	protected static final double[][] newMap() {
		return new double[][] { new double[4], new double[4], new double[4],
				new double[4] };
	}

	public double[][] getInvertMap() throws Exception {
		if (this.invalidInvertMap) {
			synchronized (this.invertMap) {
				invertMapProj(this.projArray, this.invertMap);
				this.invalidInvertMap = false;
			}
		}
		return this.invertMap;
	}

	public Projection() {
		super();
	}

	protected final void copyMap(double[][] from, double[][] to) {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				to[i][j] = from[i][j];
			}
		}
	}

	public void copyFrom(Projection from) {
		copyMap(from.projArray, this.projArray);
		this.invalidInvertMap = true;
	}

	public void applyProjection(double b[], double result[]) {
		int j;
		applyProjectionLin(b, result);
		double m = result[3];
		if (Math.abs(m) > 1.E-6) {
			for (j = 0; j < 4; j++) {
				result[j] /= m;
			}
		}
	}

	public void applyProjectionLin(double[] b, double result[]) {
		int i, j;
		for (i = 0; i < 4; i++) {
			result[i] = 0D;
			for (j = 0; j < 4; j++) {
				result[i] += this.projArray[j][i] * b[j];
			}
		}
	}

	public void project(int plan, double X[], double[] centerPoint,
			Point _result) {
		double[] v = new double[4], vv = new double[4];
		int i;
		for (i = 0; i < 3; i++) {
			v[i] = X[3 * plan + i] - centerPoint[i];
		}
		v[3] = 1D;
		applyProjection(v, vv);
		if (vv[2] < 0) {
			_result.x = (int) vv[0];
			_result.y = (int) vv[1];
		} else {
			_result.x = _result.y = Integer.MIN_VALUE;
		}
	}

	private static void invertMapProj(double A[][], double result[][])
			throws Exception {
		int i;
		double z[] = new double[4];
		double a[];
		for (i = 0; i < 4; i++) {
			z[0] = z[1] = z[2] = z[3] = 0D;
			z[i] = 1D;
			a = Funcions.refenlinia4x4(A);
			Funcions.gaussElim(a, z, 4, result[i], 1.e-8D);
		}
	}
}