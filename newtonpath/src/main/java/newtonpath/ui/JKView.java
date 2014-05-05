package newtonpath.ui;

public interface JKView<E> {

	void resetView(E integrador);

	void updateView(E integ);
	
	void setParent(JKViewList<E> parent);

}
