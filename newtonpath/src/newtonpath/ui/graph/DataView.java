package newtonpath.ui.graph;


import java.util.Collection;

import newtonpath.ui.graph.DataProjection.Range;

public class DataView<T extends DataPoint> {
	final private DataSet<T> data;
	public DataProjection<T> x, y;

	public DataView(DataSet<T> _data, DataProjection<T> _x, DataProjection<T> _y) {
		this.data = _data;
		this.x = _x;
		this.y = _y;
	}

	public static class GraphRange {
		public Range x, y;

		public GraphRange() {
			this.x = new Range();
			this.y = new Range();
		}
	}

	public void calcRange() {
		int i;
		if (this.data.size() == 0) {
			this.x.dataRange.min = this.y.dataRange.min = 0D;
			this.x.dataRange.max = this.y.dataRange.max = 1D;
		} else {
			T p;
			p = this.data.get(0);
			this.x.dataRange.min = this.x.dataRange.max = this.x.value(p);
			this.y.dataRange.min = this.y.dataRange.max = this.y.value(p);
			for (i = 1; i < this.data.size(); i++) {
				double v;
				p = this.data.get(i);
				v = this.x.value(p);
				if (v < this.x.dataRange.min) {
					this.x.dataRange.min = v;
				} else if (v > this.x.dataRange.max) {
					this.x.dataRange.max = v;
				}
				v = this.y.value(p);
				if (v < this.y.dataRange.min) {
					this.y.dataRange.min = v;
				} else if (v > this.y.dataRange.max) {
					this.y.dataRange.max = v;
				}
			}
		}
	}

	public Collection<Integer> getGaps() {
		return this.data.getGaps();
	}

	public T get(int index) {
		return this.data.get(index);
	}

	public int size() {
		return this.data.size();
	}

	public double x(int i) {
		return this.x.value(get(i));
	}

	public double y(int i) {
		return this.y.value(get(i));
	}

	public double x(T p) {
		return this.x.value(p);
	}

	public double y(T p) {
		return this.y.value(p);
	}

	public void getPoint(int i, double point[]) {
		T p = get(i);
		point[0] = this.x.value(p);
		point[1] = this.y.value(p);
	}

	public DataProjection<T> getX() {
		return this.x;
	}

	public void setX(DataProjection<T> x) {
		this.x = x;
	}

	public DataProjection<T> getY() {
		return this.y;
	}

	public void setY(DataProjection<T> y) {
		this.y = y;
	}
}
