package newtonpath.tasks.step;

import java.util.HashMap;
import java.util.Map;

import newtonpath.statemanager.Observable;
import newtonpath.statemanager.OperationResult;
import newtonpath.ui.AproxObs;

public class StepFormat extends AbstractStep {
	private final String format;

	public StepFormat(String calculationRef, String format) {
		super(calculationRef);
		this.format = format;
	}

	@Override
	public AproxObs execute(AproxObs obj) throws Exception {
		String val = this.format;
		Map<String, Observable> paramsByName = new HashMap<String, Observable>();
		for (Observable par : OperationResult.addObservables(null,
				obj.getObservables(), obj)) {
			paramsByName.put(par.getDescription(), par);
		}
		for (String key : paramsByName.keySet()) {
			String token = "{" + key + "}";
			if (val.contains(token)) {
				val = val.replace(token,
						paramsByName.get(key).getStringValue(obj));
			}
		}

		setOperationResult(new OperationResult(obj, null, null, val));
		return obj;
	}
}