package newtonpath.statemanager;

import java.util.Collection;

public interface OperationSubmitter {

	public void execOperation(Object _o, Operation _e,
			Collection<Parameter> _par);

}