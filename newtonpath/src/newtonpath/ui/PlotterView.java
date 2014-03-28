package newtonpath.ui;

public interface PlotterView<E> {
	void beforeUpdate(E _int);

	void afterUpdate(E _int, boolean _valid);

	void updatePoint(E _int);
}
