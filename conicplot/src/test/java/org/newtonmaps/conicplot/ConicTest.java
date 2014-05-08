package org.newtonmaps.conicplot;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class ConicTest extends TestCase {
	ConicMatrix cm = new ConicMatrix().setValues(2, 1, 0.5, 0, 10, -350);
	ConicMatrix slope = this.cm.calculateSlopeConic(0, 1);
	ConicMatrix cmT = this.cm.calculateVariableSwap(0, 1);

	/**
	 * Create the test case
	 * 
	 * @param testName
	 *            name of the test case
	 */
	public ConicTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(ConicTest.class);
	}

	public void testFunction() {
		for (double x = -80D; x < 80D; x++) {
			for (double y = -80D; y < 80D; y++) {
				double v = this.cm.evaluate(x, y, 1D);
				System.out.print(v < 0 ? "@" : " ");
			}
			System.out.println();
		}
		assertTrue(true);
	}

	public void testGradient() {
		for (double x = -80D; x < 80D; x++) {
			for (double y = -80D; y < 80D; y++) {
				int s = gradientSign(this.cm, x, y, 1D);
				System.out.print(s < 0 ? "-" : s > 0 ? "|" : " ");
			}
			System.out.println();
		}
		assertTrue(true);
	}

	public void testAll() {
		for (double x = -80D; x < 80D; x++) {
			for (double y = -80D; y < 80D; y++) {
				int s = -sign(this.slope, x, y, 1D);// gradientSign(cm, x, y,
													// 1D);
				System.out.print(s < 0 ? "-" : s > 0 ? "|" : "+");
				double v = this.cm.evaluate(x, y, 1D);
				System.out.print(Math.abs(v) <= 1.e-1D ? " " : v < 0 ? "@"
						: "O");
			}
			System.out.println();
		}
		assertTrue(true);
	}

	private static int gradientSign(ConicMatrix m, double... p) {
		double a[] = new double[ConicMatrix.DIMENSION];
		for (int i = 0; i < ConicMatrix.DIMENSION; i++) {
			a[i] = m.evaluateGradient(i, p);
		}
		double d = Math.abs(a[0]) - Math.abs(a[1]);
		return d == 0D ? 0 : d > 0D ? 1 : -1;
	}

	private static int sign(ConicMatrix m, double... p) {
		double d = m.evaluate(p);
		return d == 0D ? 0 : d > 0D ? 1 : -1;
	}

	public void testSection() {
		int min = -80, max = 80;
		int size = max - min;
		char[][] data = new char[size][size];

		int i = 0;
		int j = 0;
		for (double x = min; x < max; x++, i++) {
			j = 0;
			for (double y = min; y < max; y++, j++) {
				double v = this.cm.evaluate(x, y, 1D);
				data[i][j] = (v < 0 ? '.' : ' ');
			}
		}

		double[] table = new double[2];

		j = 0;
		for (double y = min; y < max; y++, j++) {
			int t = QuadricSection.section(this.cm, y, min, max, table);
			for (int k = 0; k < t; k++) {
				double x = table[k];
				if (this.slope.evaluate(x, y, 1) <= 0) {
					i = (int) (x - min);
					if (i < size) {
						data[i][j] = '@';
					}
				}
			}
		}

		i = 0;
		for (double x = min; x < max; x++, i++) {
			int t = QuadricSection.section(this.cmT, x, min, max, table);
			for (int k = 0; k < t; k++) {
				double y = table[k];
				if (this.slope.evaluate(x, y, 1) >= 0) {
					j = (int) (y - min);
					if (j < size) {
						data[i][j] = 'X';
					}
				}
			}
		}

		for (j = 0; j < size; j++) {
			for (i = 0; i < size; i++) {
				System.out.print(data[i][j]);
			}
			System.out.println();
		}
		assertTrue(true);
	}
}
