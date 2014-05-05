package newtonpath.ui;

public interface ParameterManager<E> {
	public E getObject();

	public E getCopy(E original);

	public void copyTo(E origin, E destination);
}
