package newtonpath.conic.test;

import java.awt.Rectangle;
import java.io.PrintStream;

import newtonpath.conic.drawing.ConicMatrix;


public class ExecTest {
	private int testNum = 0;

	public static void main(String[] args) throws Exception {

		ConicMatrix c = new newtonpath.conic.drawing.ConicMatrix();
		c.setValues(new double[] { 0.003909332524541632, -5.246089607239634E-5,
				-0.0038377708312854874, -0.0030396815854656857,
				-0.0030396815854656857, -0.04531542253279497 });
		Rectangle r = new Rectangle(-212, -62, 510, 381);

		ExecTest t = new ExecTest();
		t.testHyperbola("20110110_214419_783", c, r, System.out);
	}

	private boolean testHyperbola(String id, ConicMatrix _c, Rectangle _r,
			PrintStream _report) {
		this.testNum++;

		TestParameters param = new TestParameters(id);
		param.rect.setBounds(_r);
		param.matrix.setValues(_c.getValues());

		boolean retVal = param.testHyperbola(_report);

		param.saveTestImage();
		return retVal;
	}

}
