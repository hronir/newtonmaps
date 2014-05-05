package newtonpath.conic.test;

import java.awt.Rectangle;
import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import newtonpath.conic.drawing.ConicMatrix;
import newtonpath.conic.drawing.SingularMatrixException;
import newtonpath.conic.drawing.TriangularMatrix;


public class RandomTest {
	private int testNum = 0;
	public final String prefixId = new SimpleDateFormat("yyyyMMdd_HHmmss")
			.format(new Date());

	private static int[] randomInterval(int min, int max) {
		int a, b;
		a = (int) (Math.random() * (max - 1 - min)) + min;
		do {
			b = (int) (Math.random() * (max - 1 - min)) + min;
		} while (a == b);
		if (a < b) {
			return new int[] { a, b + 1 };
		}
		return new int[] { b, a + 1 };
	}

	private boolean randomTest(PrintStream _report) {
		boolean bOk = false;
		ConicMatrix matrix = new ConicMatrix();
		int[] x, y;
		x = randomInterval(-400, 400);
		y = randomInterval(-400, 400);
		Rectangle r = new Rectangle(x[0], y[0], x[1] - x[0], y[1] - y[0]);
		do {
			randomHyperbola(matrix);
			scaleConic(matrix, Math.exp(-Math.log(10) * (1 + Math.random())));
		} while (isSingular(matrix));

		try {
			bOk = testHyperbola(matrix, r, _report);
		} catch (SingularMatrixException e) {
			System.err.println(matrix.toString());
			throw e;
		}
		return bOk;
	}

	public static void main(String[] args) throws Exception {
		int tests = 0, errors = 0;
		RandomTest t = new RandomTest();
		PrintStream report = new PrintStream(new File(t.prefixId + ".txt"));
		try {
			for (int i = 0; i < 10000; i++) {
				Thread.sleep(10);
				try {
					if (!t.randomTest(report)) {
						errors++;
					}
					tests++;
				} catch (SingularMatrixException e) {
					e.printStackTrace();
				}
			}
		} finally {
			report.close();
			System.out.println(tests + " tests: " + errors + " errors");
		}
	}

	private boolean testHyperbola(ConicMatrix _c, Rectangle _r,
			PrintStream _report) {
		this.testNum++;
		String id = "[" + this.prefixId + "_" + this.testNum + "]";
		TestParameters param = new TestParameters(id);
		param.rect.setBounds(_r);
		param.matrix.setValues(_c.getValues());

		boolean retVal = param.testHyperbola(_report);
		if (!retVal) {
			param.saveTestImage();
		}
		return retVal;
	}

	private static void changeColumnSign(TriangularMatrix _m, int _col) {
		for (int i = 0; i <= _col; i++) {
			_m.setCoefficient(_col, i, -_m.getCoefficient(_col, i));
		}
	}

	private static void randomHyperbola(ConicMatrix _m) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j <= i; j++) {
				_m.setCoefficient(i, j, 2D * Math.random() - 1D);
			}
		}

		if (_m.getCoefficient(0, 0) < 0) {
			changeColumnSign(_m, 0);
		}

		if (_m.getCoefficient(0, 0) * _m.getCoefficient(1, 1)
				- _m.getCoefficient(1, 0) * _m.getCoefficient(1, 0) > 0) {
			changeColumnSign(_m, 1);
		}

		if (_m.getCoefficient(0, 0) * _m.getCoefficient(1, 1)
				* _m.getCoefficient(2, 2) + _m.getCoefficient(1, 0)
				* _m.getCoefficient(2, 1) * _m.getCoefficient(1, 0)
				+ _m.getCoefficient(2, 0) * _m.getCoefficient(1, 0)
				* _m.getCoefficient(2, 0) - _m.getCoefficient(0, 0)
				* _m.getCoefficient(2, 1) * _m.getCoefficient(2, 1)
				- _m.getCoefficient(1, 0) * _m.getCoefficient(1, 0)
				* _m.getCoefficient(2, 2) - _m.getCoefficient(2, 0)
				* _m.getCoefficient(1, 1) * _m.getCoefficient(2, 2) < 0) {
			changeColumnSign(_m, 2);
		}
	}

	private static void scaleConic(ConicMatrix _m, double _factor) {
		_m.setCoefficient(0, 0, _m.getCoefficient(0, 0) * _factor * _factor);
		_m.setCoefficient(1, 0, _m.getCoefficient(1, 0) * _factor * _factor);
		_m.setCoefficient(1, 1, _m.getCoefficient(1, 1) * _factor * _factor);
		_m.setCoefficient(2, 0, _m.getCoefficient(2, 1) * _factor * _factor);
		_m.setCoefficient(2, 1, _m.getCoefficient(2, 1) * _factor * _factor);
	}

	private static boolean isSingular(ConicMatrix _m) {
		return Math.abs(_m.getCoefficient(0, 0) * _m.getCoefficient(1, 1)
				* _m.getCoefficient(2, 2) + _m.getCoefficient(1, 0)
				* _m.getCoefficient(2, 1) * _m.getCoefficient(1, 0)
				+ _m.getCoefficient(2, 0) * _m.getCoefficient(1, 0)
				* _m.getCoefficient(2, 0) - _m.getCoefficient(0, 0)
				* _m.getCoefficient(2, 1) * _m.getCoefficient(2, 1)
				- _m.getCoefficient(1, 0) * _m.getCoefficient(1, 0)
				* _m.getCoefficient(2, 2) - _m.getCoefficient(2, 0)
				* _m.getCoefficient(1, 1) * _m.getCoefficient(2, 2)) < 1.e-12;
	}

}
