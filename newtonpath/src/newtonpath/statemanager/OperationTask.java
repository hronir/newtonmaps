package newtonpath.statemanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

public class OperationTask implements Callable<OperationResult> {
	private final Operation exec;
	private final Object obj;
	private final Collection<Parameter> parameters;

	public OperationTask(Object _obj, Operation _op,
			Collection<Parameter> _param) {
		this.exec = _op;
		this.obj = _obj;
		this.parameters = _param == null ? null : new ArrayList<Parameter>(
				_param);
	}

	public boolean prepare() {
		boolean bOk = true;
		if (this.parameters != null) {
			for (Parameter p : this.parameters) {
				bOk = false;
				try {
					bOk = p.execute(this.obj);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (!bOk) {
					break;
				}
			}
		}
		return bOk;
	}

	
	public OperationResult call() throws Exception {

		try {
			this.exec.execute(this.obj);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new OperationResult(this.obj, this.exec, this.parameters,
				this.exec.toString());
	}
}