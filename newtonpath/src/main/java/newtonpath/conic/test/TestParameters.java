/**
 * 
 */
package newtonpath.conic.test;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.PrintStream;

import newtonpath.conic.drawing.ConicMatrix;


public class TestParameters {
	final public String id;
	final public Rectangle rect;
	// final public double coef[];
	final public ConicMatrix matrix;
	public String error;

	public TestParameters(String _id) {
		this.id = _id;
		this.rect = new Rectangle();
		this.matrix = new ConicMatrix();
	}

	@Override
	public String toString() {
		return this.id;
	}

	public String getId() {
		return this.id;
	}

	public void printDescription() {
		System.out.print(this.id);
		System.out.print(" ");
		System.out.print(this.rect);
		for (double d : this.matrix.getValues()) {
			System.out.print(" ");
			System.out.print(d);
		}
		System.out.println();
	}

	public static String rectangleToString(Rectangle _r) {
		return "Rectangle(" + _r.x + "," + _r.y + "," + _r.width + ","
				+ _r.height + ")";
	}

	public boolean testHyperbola(PrintStream _report) {
		boolean retVal = true;
		StringBuffer sb = new StringBuffer();
		HyperbolaTester conicPlotterTester = new HyperbolaTester();
		conicPlotterTester.prepareTest(this.matrix, this.rect);
		conicPlotterTester.drawHyperbola(this.rect, -1);
		if (conicPlotterTester.overlap > 0) {
			sb.append(getId() + " Warning: Path overlap ("
					+ conicPlotterTester.overlap + " pixels)\n");
			retVal = false;
		}
		if (conicPlotterTester.outOfArea > 0) {
			sb.append(getId() + " Error: Plot out of area ("
					+ conicPlotterTester.outOfArea + " pixels\n");
			retVal = false;
		}
		int numErr = conicPlotterTester.missingFrontierPixels();
		if (numErr > 0) {
			sb.append(getId() + " Error: Missing frontier pixels (" + numErr
					+ ")\n");
			retVal = false;
		}

		if (_report != null && !retVal) {
			_report.print(getId());
			_report.println(this.matrix.toString());
			_report.print(getId());
			_report.println(TestParameters.rectangleToString(this.rect));
			_report.println(sb);
		}
		return retVal;
	}

	public void saveTestImage() {
		BufferedImage b = new BufferedImage(this.rect.width, this.rect.height,
				BufferedImage.TYPE_INT_RGB);
		HyperbolaImagePlotter p = new HyperbolaImagePlotter(b, this.rect,
				Color.YELLOW.getRGB());
		p.normalizeAndAssign(100D, this.matrix);
		p.plotContent(b, this.rect);
		p.drawHyperbola(this.rect, -1);
		p.saveImage(getId());
	}
}