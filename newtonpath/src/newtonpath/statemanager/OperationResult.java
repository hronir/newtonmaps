package newtonpath.statemanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class OperationResult {
	private final Operation exec;
	private final Object resultObject;
	private final Collection<Parameter> parameters;
	private final String description;
	private final String comment;

	public String getSaveString() {
		return this.exec.getSaveString();
	}

	public OperationResult(Object _obj, Operation base,
			Collection<Parameter> _param, String description) {
		this(_obj, base, _param, description, null);
	}

	public OperationResult(Object _obj, Operation base,
			Collection<Parameter> _param, String description, String comment) {
		this.resultObject = _obj;
		this.exec = base;
		this.parameters = (_param);
		this.description = description;
		this.comment = comment;
	}

	public String getDescription() {
		return this.description;
	}

	public Object getResultObject() {
		return this.resultObject;
	}

	@Override
	public String toString() {
		return this.exec.toString();
	}

	public final Collection<Parameter> getParameters() {
		return Collections.unmodifiableCollection(this.parameters);
	}

	public Operation getOperation() {
		return this.exec;
	}

	public static List<Observable> addObservables(List<Observable> list,
			ObservableArray memb, Object o) {
		List<Observable> lst;
		if (list == null) {
			lst = new ArrayList<Observable>();
		} else {
			lst = list;
		}
		ObservableArray[] sub;
		if (o != null) {
			sub = memb.getArrays(o);
			if (sub != null) {
				for (ObservableArray subitem : sub) {
					addObservables(lst, subitem, o);
				}
			}
			Observable[] obs = memb.getComponents(o);
			if (obs != null) {
				lst.addAll(Arrays.asList(obs));
			}
		}
		return lst;
	}

	public String getComment() {
		return comment;
	}
}
