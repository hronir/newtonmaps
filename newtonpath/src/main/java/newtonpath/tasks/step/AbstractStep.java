package newtonpath.tasks.step;

import newtonpath.statemanager.Operation;
import newtonpath.statemanager.OperationResult;
import newtonpath.ui.AproxObs;

abstract public class AbstractStep {
	private OperationResult operationResult = null;
	private final String calculationReference;

	public AbstractStep(String calculationRef) {
		super();
		this.calculationReference = calculationRef;
	}

	public OperationResult getOperationResult() {
		return this.operationResult;
	}

	public String getCalculationReference() {
		return this.calculationReference;
	}

	public void setOperationResult(OperationResult operationResult) {
		this.operationResult = operationResult;
	}

	public Operation getOperation() {
		return null;
	}

	public abstract AproxObs execute(AproxObs obj) throws Exception;
}