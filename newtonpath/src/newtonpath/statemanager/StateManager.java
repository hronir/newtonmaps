package newtonpath.statemanager;

public interface StateManager {
	public ObservableArray getParameters();

	public ObservableArray getObservables();

	public Operation[] getOperations();
}
