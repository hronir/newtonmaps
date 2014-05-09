package org.newtonmaps.conicplot;

public class ConicMatrix {
	public static final int DIMENSION = 3;
	private double[][] coefficients;

	public ConicMatrix() {
		this.coefficients = new double[DIMENSION][];
		for (int i = 0; i < DIMENSION; i++) {
			this.coefficients[i] = new double[i + 1];
		}
	}

	public ConicMatrix setValues(double... values) {
		int k = 0;
		for (int i = 0; i < DIMENSION; i++) {
			for (int j = 0; j <= i; j++) {
				this.coefficients[i][j] = values[k];
				k++;
			}
		}
		return this;
	}

	public ConicMatrix copyTo(ConicMatrix result) {
		for (int i = 0; i < DIMENSION; i++) {
			for (int j = 0; j <= i; j++) {
				result.coefficients[i][j] = this.coefficients[i][j];
			}
		}
		return result;
	}

	public double evaluate(double... p) {
		double v = 0;
		for (int i = 0; i < DIMENSION; i++) {
			v += this.coefficients[i][i] * p[i] * p[i];
			for (int j = 0; j < i; j++) {
				v += 2D * this.coefficients[i][j] * p[i] * p[j];
			}
		}
		return v;
	}

	public double evaluateGradient(int i, double... p) {
		double v = 0;
		for (int j = 0; j < DIMENSION; j++) {
			v += 2D * getCoefficient(i, j) * p[j];
		}
		return v;
	}

	public double getCoefficient(int i, int j) {
		return i < j ? this.coefficients[j][i] : this.coefficients[i][j];
	}

	public ConicMatrix slopeConic(int a, int b, ConicMatrix result) {
		for (int i = 0; i < DIMENSION; i++) {
			for (int j = 0; j <= i; j++) {
				result.coefficients[i][j] = -getCoefficient(i, a)
						* getCoefficient(j, a) + getCoefficient(i, b)
						* getCoefficient(j, b);
			}
		}
		return result;
	}

	// public int evaluateSlopeSign(int a, double... p) {
	// double d = 0;
	// for (int k = 0; k < DIMENSION; k++) {
	// d += getCoefficient(a, k) * p[k];
	// }
	// return d < 0D ? -1 : d > 0D ? 1 : 0;
	// }

	public ConicMatrix swapVariable(int a, int b, ConicMatrix result) {
		for (int i = 0; i < DIMENSION; i++) {
			int i1 = swap(a, b, i);
			for (int j = 0; j <= i; j++) {
				int j1 = swap(a, b, j);
				result.coefficients[i][j] = getCoefficient(i1, j1);
			}
		}
		return result;
	}

	public ConicMatrix variableSymetry(int a, ConicMatrix result) {
		for (int i = 0; i < DIMENSION; i++) {
			for (int j = 0; j <= i; j++) {
				result.coefficients[i][j] = this.coefficients[i][j];
			}
		}
		for (int i = 0; i < a; i++) {
			result.coefficients[a][i] = -result.coefficients[a][i];
		}
		for (int i = a + 1; i < DIMENSION; i++) {
			result.coefficients[i][a] = -result.coefficients[i][a];
		}
		return result;
	}

	private static int swap(int a, int b, int i) {
		return i == a ? b : i == b ? a : i;
	}

	public int evaluateSlopeSign(double... p) {
		double x = 0;
		double y = 0;
		for (int i = 0; i < DIMENSION; i++) {
			x += getCoefficient(0, i) * p[i];
			y += getCoefficient(1, i) * p[i];
		}
		double k = x * y;
		return k > 0 ? 1 : k < 0 ? -1 : 0;
	}
}
