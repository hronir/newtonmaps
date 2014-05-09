package org.newtonmaps.conicplot;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class ConicTest extends TestCase {
	ConicMatrix cm = new ConicMatrix().setValues(2, 1, 2.5, 0, 0.2, -650);
	ConicMatrix slope = this.cm.slopeConic(0, 1, new ConicMatrix());
	ConicMatrix cmT = this.cm.swapVariable(0, 1, new ConicMatrix());

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
				int s = this.cm.evaluateSlopeSign(x, y, 1D);
				System.out.print(s < 0 ? "\\" : s > 0 ? "/" : " ");
				double v = this.cm.evaluate(x, y, 1D);
				System.out.print(Math.abs(v) <= 1.e-1D ? " " : v < 0 ? "."
						: " ");
			}
			System.out.println();
		}
		assertTrue(true);
	}

	public void testSign() {
		for (double x = -80D; x < 80D; x++) {
			for (double y = -80D; y < 80D; y++) {
				int s = this.cm.evaluateSlopeSign(0, x, y, 1D);
				System.out.print(s < 0 ? "<" : s > 0 ? ">" : " ");
				int sY = this.cm.evaluateSlopeSign(1, x, y, 1D);
				System.out.print(sY < 0 ? "v" : sY > 0 ? "^" : " ");
				double v = this.cm.evaluate(x, y, 1D);
				System.out.print(Math.abs(v) <= 1.e-1D ? " " : v < 0 ? "¤"
						: " ");
			}
			System.out.println();
		}
		assertTrue(true);
	}

	public void testAll() {
		for (double x = -80D; x < 80D; x++) {
			for (double y = -80D; y < 80D; y++) {
				int s = -sign(this.slope, x, y, 1D);
				System.out.print(s < 0 ? "-" : s > 0 ? "|" : "+");
				double v = this.cm.evaluate(x, y, 1D);
				System.out.print(Math.abs(v) <= 1.e-1D ? " " : v < 0 ? "@"
						: "O");
			}
			System.out.println();
		}
		assertTrue(true);
	}

	public void testContext() {
		int min = -80;
		int max = 80;
		int size = max - min;
		for (double v : new double[] { -15D, -10D, -5D, 5D, 15D }) {
			double[] table = new double[2];

			char[][] data = new char[size][size];

			int i = 0;
			int j = 0;
			for (double x = min; x < max; x++, i++) {
				j = 0;
				for (double y = min; y < max; y++, j++) {
					double vv = this.cm.evaluate(x, y, 1D);
					data[i][j] = (vv < 0 ? '.' : ' ');
				}
			}

			ConicContext ctx = new ConicContext();

			double[] p = new double[2];

			int t = QuadricSection.section(this.cm, v, min, max, table);
			for (int z = 0; z < t; z++) {
				double d = Math.floor(table[z]);
				data[(int) (d - min)][(int) (v - min)] = '*';
				ctx.initialize(this.cm, d, v, min, max, min, max);

				for (int tm = 0; tm < 30; tm++) {
					ctx.getCurrentInnerPoint(p);
					data[(int) (p[0] - min)][(int) (p[1] - min)] = '+';
					ctx.getCurrentOuterPoint(p);
					data[(int) (p[0] - min)][(int) (p[1] - min)] = '@';
					ctx.next();
				}
			}

			for (j = 0; j < size; j++) {
				for (i = 0; i < size; i++) {
					System.out.print(data[i][j]);
				}
				System.out.println();
			}
		}
		assertTrue(true);
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
