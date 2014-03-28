package newtonpath.tasks.step;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

import newtonpath.logging.KLogger;
import newtonpath.statemanager.Operation;
import newtonpath.statemanager.OperationResult;
import newtonpath.statemanager.Parameter;
import newtonpath.tasks.BatchException;
import newtonpath.ui.AproxObs;
import newtonpath.ui.OperationException;
import newtonpath.ui.XMLMapper;

public class StepOperation extends AbstractStep {
	private static final KLogger LOGGER = KLogger
			.getLogger(StepOperation.class);

	private final Operation operation;
	private final Map<Parameter, String> parameterList;

	public StepOperation(Operation operation,
			Map<Parameter, String> parameterList, String calculationRef) {
		super(calculationRef);
		this.operation = operation;
		this.parameterList = parameterList;
	}

	@Override
	public AproxObs execute(AproxObs obj) throws Exception {
		AproxObs newObj = obj.clone();
		boolean result = false;

		long tm = System.currentTimeMillis();
		for (Map.Entry<Parameter, String> ent : getParameterList().entrySet()) {

			LOGGER.debug(toString() + " "
					+ ent.getKey().getObservable().getDescription() + " = "
					+ ent.getValue());
			ent.getKey().setValue(ent.getValue());
			ent.getKey().execute(newObj);
		}
		LOGGER.debug(toString() + " " + getOperation().toString());

		try {
			result = getOperation().execute(newObj);
		} catch (OperationException e) {
			throw new BatchException(e.getErrorNumber(),
					getCalculationReference(), newObj, e);
		}
		tm = (System.currentTimeMillis() - tm) / 1000;

		String resultDescription = DateFormat.getInstance().format(new Date())
				+ " " + getCalculationReference() + " after " + tm + "s.";

		OperationResult res = new OperationResult(newObj, getOperation(),
				getParameterList().keySet(), resultDescription);
		if (result) {
			setOperationResult(res);
		} else {
			XMLMapper.getInstance().printOperationResult(res);
			newObj = null;
		}
		return newObj;
	}

	@Override
	public Operation getOperation() {
		return this.operation;
	}

	public Map<Parameter, String> getParameterList() {
		return this.parameterList;
	}

}