package newtonpath.conic.drawing;

public class ConicMatrix extends TriangularMatrix {
	public ConicMatrix() {
		super(3);
	}

	public double eval(double x, double y) {
		return x * x * this.coefficient[0][0] + 2D * x * y
				* this.coefficient[1][0] + y * y * this.coefficient[1][1] + 2D
				* x * this.coefficient[2][0] + 2D * y * this.coefficient[2][1]
				+ this.coefficient[2][2];
	}

	public double eval(int x, int y) {
		return eval((double) x, (double) y);

	}

	public void setValues(double[][] v) {
		setCoefficient(0, 0, v[0][0]);
		setCoefficient(1, 0, v[1][0]);
		setCoefficient(1, 1, v[1][1]);
		setCoefficient(2, 0, v[2][0]);
		setCoefficient(2, 1, v[2][1]);
		setCoefficient(2, 2, v[2][2]);
	}

	public void setValues(double[] v) {
		setCoefficient(0, 0, v[0]);
		setCoefficient(1, 0, v[1]);
		setCoefficient(1, 1, v[2]);
		setCoefficient(2, 0, v[3]);
		setCoefficient(2, 1, v[4]);
		setCoefficient(2, 2, v[5]);
	}

	public double[] getValues() {
		return new double[] { getCoefficient(0, 0), getCoefficient(1, 0),
				getCoefficient(1, 1), getCoefficient(2, 0),
				getCoefficient(2, 1), getCoefficient(2, 2),

		};
	}
}
