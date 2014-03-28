package newtonpath.statemanager;

public interface Executable {
	public String getSaveString();

	public boolean execute(Object _o) throws Exception;
}
