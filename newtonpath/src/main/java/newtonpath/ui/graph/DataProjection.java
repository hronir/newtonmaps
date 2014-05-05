package newtonpath.ui.graph;

public abstract class DataProjection<T extends DataPoint> {
	public static class Range {
		public double min, max;
		public double scaleUnit;

		public Range() {
			this.min = this.max = 0D;
			this.scaleUnit = 1D;
		}

		public void normalize() {
			double ld;
			if (this.max > this.min) {
				ld = Math.log10(this.max - this.min);
				this.scaleUnit = Math.pow(10D, Math.floor(ld));
				this.max = scaleUnit * Math.ceil(this.max / scaleUnit);
				this.min = scaleUnit * Math.floor(this.min / scaleUnit);
			} else {
				this.scaleUnit = 1D;
			}
		}

		public double calculateTickUnit(int maxNumber) {
			double tickUnit = this.scaleUnit;
			double h = tickUnit;
			while (Math.floor(max / h) - Math.ceil(min / h) <= maxNumber) {
				tickUnit = h;
				h = h / 10D;
			}
			return tickUnit;
		}

		public void include(Range r) {
			if (this.min == Double.NaN || r.min < this.min) {
				this.min = r.min;
			}
			if (this.max == Double.NaN || r.max > this.max) {
				this.max = r.max;
			}
		}

		public void include(double v) {
			if (this.min == Double.NaN) {
				this.min = this.max = v;
			} else {
				if (v < this.min) {
					this.min = v;
				} else if (v > this.max) {
					this.max = v;
				}
			}
		}
	}

	public Range dataRange;

	public DataProjection() {
		this.dataRange = new Range();
	}

	public abstract double value(T p);

}
