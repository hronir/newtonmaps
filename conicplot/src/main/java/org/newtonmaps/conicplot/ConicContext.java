package org.newtonmaps.conicplot;

public class ConicContext {
	private ConicMatrix[] workData = new ConicMatrix[] { new ConicMatrix(),
			new ConicMatrix() };
	private ConicMatrix[] workSlope = new ConicMatrix[] { new ConicMatrix(),
			new ConicMatrix() };
	private int current = 0;

	private double[][] limits = new double[2][2];
	private double[][] transformation = new double[][] { { 1D, 0D }, { 0D, 1D } };

	private double cursor;
	private double floor;
	private int sign = 1;
	private int signAxe = 0;

	private ConicMatrix currentMatrix() {
		return this.workData[this.current];
	}

	private ConicMatrix tempMatrix() {
		return this.workData[1 - this.current];
	}

	private ConicMatrix currentSlope() {
		return this.workSlope[this.current];
	}

	private ConicMatrix tempSlope() {
		return this.workSlope[1 - this.current];
	}

	private void swapWork() {
		this.current = 1 - this.current;
	}

	private void swapValues() {
		double t = this.floor;
		this.floor = this.cursor;
		this.cursor = t;
	}

	private void swapWithSign(double[] data) {
		double t = data[0];
		data[0] = -data[1];
		data[1] = -t;
	}

	private void swap(double[][] data) {
		double[] t = data[0];
		data[0] = data[1];
		data[1] = t;
	}

	private void swapVariables() {
		currentMatrix().swapVariable(0, 1, tempMatrix());
		currentSlope().swapVariable(0, 1, tempSlope());
		swapWork();
		swap(this.limits);
		swap(this.transformation);
		swapValues();
		this.signAxe = 1 - this.signAxe;
	}

	private void variableSymmetry(int axe) {
		currentMatrix().variableSymetry(axe, tempMatrix());
		currentSlope().variableSymetry(axe, tempSlope());
		swapWork();
		swapWithSign(this.limits[axe]);
		for (int i = 0; i < 2; i++) {
			this.transformation[axe][i] = -this.transformation[axe][i];
		}
		if (axe == 0) {
			this.cursor = -this.cursor;
		} else {
			this.floor = 1D - this.floor;
		}
		// if (axe == this.signAxe) {
		// this.sign = -this.sign;
		// }
	}

	public double evaluateOuter() {
		return currentMatrix().evaluate(this.cursor, this.floor + 1D, 1);
	}

	public double evaluateInner() {
		return currentMatrix().evaluate(this.cursor, this.floor, 1);
	}

	public void getCurrentOuterPoint(double[] p) {
		getCurrentPoint(p, this.floor + 1D);
	}

	public void getCurrentInnerPoint(double[] p) {
		getCurrentPoint(p, this.floor);
	}

	private void getCurrentPoint(double[] p, double y) {
		for (int i = 0; i < 2; i++) {
			p[i] = this.transformation[i][0] * this.cursor
					+ this.transformation[i][1] * y;
		}
	}

	public String getTransformation() {
		return this.transformation[0][0] + "\t" + this.transformation[1][0]
				+ "\n" + this.transformation[0][1] + "\t"
				+ this.transformation[1][1];
	}

	public void initialize(ConicMatrix c, double x, double y, double minX,
			double maxX, double minY, double maxY) {
		c.copyTo(this.currentMatrix());
		this.currentMatrix().slopeConic(0, 1, this.currentSlope());

		this.limits[0][0] = minX;
		this.limits[0][1] = maxX;
		this.limits[1][0] = minY;
		this.limits[1][1] = maxY;
		this.transformation[0][0] = this.transformation[1][1] = 1D;
		this.transformation[1][0] = this.transformation[0][1] = 0D;
		this.cursor = x;
		this.floor = y;

		if (this.currentSlope().evaluate(this.cursor, this.floor, 1D) < 0D) {
			swapVariables();
		}
		if (evaluateInner() * evaluateOuter() > 0D) {
			this.floor -= 1D;
		}
		if (evaluateInner() * evaluateOuter() > 0D) {
			System.out.println("Error");
			// throw new RuntimeException("Invalid start point");
		}
		this.sign = 1;
		this.signAxe = 0;
	}

	public boolean next() {
		if (this.currentSlope().evaluate(this.cursor, this.floor, 1D) > 0D) {
			boolean invertSense = currentMatrix().evaluateSlopeSign(
					this.cursor, this.floor, 1D) > 0;
			swapVariables();
			if (invertSense) {
				// this.variableSymmetry(0);
				this.sign = -this.sign;
			}
		}

		this.cursor += this.sign;
		double val0 = currentMatrix().evaluate(this.cursor, this.floor, 1D);
		double val1 = currentMatrix()
				.evaluate(this.cursor, this.floor + 1D, 1D);
		if (val1 * val0 < 0) {
			// floor unchanged;
		} else {
			double val1Plus = currentMatrix().evaluate(this.cursor,
					this.floor + 2D, 1D);
			if (val1 * val1Plus < 0) {
				this.floor += 1D;
			} else {
				double val0Minus = currentMatrix().evaluate(this.cursor,
						this.floor - 1D, 1D);
				if (val0 * val0Minus < 0D) {
					this.floor -= 1D;
				}
			}
		}

		return true;
	}
}
