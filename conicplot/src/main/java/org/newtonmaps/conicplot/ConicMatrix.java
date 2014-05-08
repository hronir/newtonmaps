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

	public ConicMatrix calculateSlopeConic(int a, int b) {
		ConicMatrix result = new ConicMatrix();
		for (int i = 0; i < DIMENSION; i++) {
			for (int j = 0; j <= i; j++) {
				result.coefficients[i][j] = -getCoefficient(i, a)
						* getCoefficient(j, a) + getCoefficient(i, b)
						* getCoefficient(j, b);
			}
		}
		return result;
	}

	public ConicMatrix calculateVariableSwap(int a, int b) {
		ConicMatrix result = new ConicMatrix();
		for (int i = 0; i < DIMENSION; i++) {
			int i1 = swap(a, b, i);
			for (int j = 0; j <= i; j++) {
				int j1 = swap(a, b, j);
				result.coefficients[i][j] = getCoefficient(i1, j1);
			}
		}
		return result;
	}

	private int swap(int a, int b, int i) {
		return i == a ? b : i == b ? a : i;
	}
}
