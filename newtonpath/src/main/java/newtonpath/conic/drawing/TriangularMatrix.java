package newtonpath.conic.drawing;

public class TriangularMatrix {
	final protected double coefficient[][];

	public TriangularMatrix(int _dim) {
		this.coefficient = new double[_dim][];
		for (int i = 0; i < _dim; i++) {
			this.coefficient[i] = new double[i + 1];
		}
	}

	public double getCoefficient(int i, int j) {
		return this.coefficient[i][j];
	}

	public void setCoefficient(int i, int j, double _val) {
		this.coefficient[i][j] = _val;
	}

	public double getMaxNorm() {
		double retVal = 0D;

		for (int i = 0; i < 2; i++) {
			for (int j = 0; j <= i; j++) {
				double v = Math.abs(this.coefficient[i][j]);
				if (v > retVal) {
					retVal = v;
				}
			}
		}

		return retVal;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getClass().getName()).append("(");
		for (double[] col : this.coefficient) {
			for (double d : col) {
				sb.append(d);
				sb.append(",");
			}
		}
		sb.setLength(sb.length() - 1);
		sb.append(")");
		return sb.toString();
	}
}
