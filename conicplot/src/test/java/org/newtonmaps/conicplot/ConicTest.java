package org.newtonmaps.conicplot;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class ConicTest extends TestCase {
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

	/**
	 * Rigourous Test :-)
	 */
	public void testFunction() {
		ConicMatrix cm = new ConicMatrix().setValues(1, 0.5, 1, 0, 0, -50);
		for (double x = -80D; x < 80D; x++) {
			for (double y = -80D; y < 80D; y++) {
				double v = cm.evaluate(x, y, 1D);
				System.out.print(v < 0 ? "@" : " ");
			}
			System.out.println();
		}
		assertTrue(true);
	}

	/**
	 * Rigourous Test :-)
	 */
	public void testGradient() {
		ConicMatrix cm = new ConicMatrix().setValues(0, 1, 0, 0, 0, -50);
		for (double x = -80D; x < 80D; x++) {
			for (double y = -80D; y < 80D; y++) {
				int s = gradientSign(cm, x, y, 1D);
				System.out.print(s < 0 ? "-" : s > 0 ? "|" : " ");
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
}
