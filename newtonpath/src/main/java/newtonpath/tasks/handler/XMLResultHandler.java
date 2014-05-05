package newtonpath.tasks.handler;

import newtonpath.statemanager.OperationResult;
import newtonpath.ui.XMLMapper;

public class XMLResultHandler implements ResultHandler {
	public void saveResult(OperationResult x) {
		synchronized (this) {
			XMLMapper.getInstance().printOperationResult(x);
		}
	}
}