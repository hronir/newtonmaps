package newtonpath.ui.graph;

public class MultiGraphPoint extends DataPoint {
	final public double x, y[];

	public MultiGraphPoint(double _x, double _y[]) {
		super();
		this.x = _x;
		this.y = _y.clone();
	}

	public static class DataMultiXProjection extends
			DataProjection<MultiGraphPoint> {
		
		@Override
		public double value(MultiGraphPoint p) {
			return p.x;
		}
	}

	public static class DataMultiYProjection extends
			DataProjection<MultiGraphPoint> {
		final int index;

		public DataMultiYProjection(int _index) {
			this.index = _index;
		}

		
		@Override
		public double value(MultiGraphPoint p) {
			return p.y[this.index];
		}
	}
}
