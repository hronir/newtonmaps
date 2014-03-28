package newtonpath.ui;

import newtonpath.statemanager.ObservableArray;
import newtonpath.statemanager.Operation;
import newtonpath.statemanager.OperationSubmitter;
import newtonpath.ui.widget.MemberExpl;

public class JKParameters extends MemberExpl {
	private final Object integrator;
	private final Operation operation;
	private final OperationSubmitter opExecutor;

	public JKParameters(Object integ, Operation _op,
			OperationSubmitter _actionPerformer, ObservableArray _rootArray) {
		super(integ, _op.getParameters(integ));
		this.integrator = integ;
		this.operation = _op;
		this.opExecutor = _actionPerformer;
	}

	public final Operation getOperation() {
		return this.operation;
	}

	public void doOperation() {
		this.opExecutor.execOperation(this.integrator, this.operation,
				getParameters());
	}

	public void cancelOperation() {

	}
}
