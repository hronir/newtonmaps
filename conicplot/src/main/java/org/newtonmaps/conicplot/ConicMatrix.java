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
			int k;
			int l;
			if (i < j) {
				k = j;
				l = i;
			} else {
				k = i;
				l = j;
			}
			v += 2D * this.coefficients[k][l] * p[j];
		}
		return v;
	}

}
