package newtonpath.ui;

import java.util.ArrayList;
import java.util.List;

import newtonpath.sim.Simulator;


public class PlotterViewList<E extends Simulator> implements PlotterView<E> {
	private List<PlotterView<E>> list;

	public PlotterViewList() {
		this.list = new ArrayList<PlotterView<E>>();
	}

	public void afterUpdate(E _int, boolean _valid) {
		for (PlotterView<E> i : this.list) {
			i.afterUpdate(_int, _valid);
		}
	}

	public void beforeUpdate(E _int) {
		for (PlotterView<E> i : this.list) {
			i.beforeUpdate(_int);
		}
	}

	public void updatePoint(E _int) {
		for (PlotterView<E> i : this.list) {
			i.updatePoint(_int);
		}
	}

	public boolean add(PlotterView<E> e) {
		return this.list.add(e);
	}

	public boolean remove(PlotterView<E> o) {
		return this.list.remove(o);
	}
}
