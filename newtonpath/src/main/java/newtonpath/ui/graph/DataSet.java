package newtonpath.ui.graph;

import java.util.ArrayList;
import java.util.Collection;

public class DataSet<T extends DataPoint> {
	public ArrayList<T> points;
	private ArrayList<Integer> gaps;
	private int maxPoints;

	public DataSet(int _maxPoints) {
		super();
		this.maxPoints = _maxPoints;
		this.points = new ArrayList<T>(_maxPoints);
		this.gaps = new ArrayList<Integer>();
	}

	public void addPoint(T point) {
		if (this.points.size() >= this.maxPoints) {
			this.points.remove(0);
			while (this.gaps.size() > 0 && this.gaps.get(0) <= 1) {
				this.gaps.remove(0);
			}
			int i;
			for (i = 0; i < this.gaps.size(); i++) {
				this.gaps.set(i, new Integer(this.gaps.get(i).intValue() - 1));
			}
		}
		this.points.add(point);
	}

	public void addGap() {
		if (this.points.size() > 0) {
			this.gaps.add(new Integer(this.points.size()));
		}
	}

	public void clear() {
		this.points.clear();
		this.gaps.clear();
	}

	public T get(int index) {
		return this.points.get(index);
	}

	public int size() {
		return this.points.size();
	}

	public Collection<Integer> getGaps() {
		return this.gaps;
	}
}
