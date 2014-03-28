package newtonpath.statemanager;

public interface Parameter extends Executable {
	public void setValue(String _s);

	public Object getValue();

	public Observable getObservable();
}
