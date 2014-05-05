package newtonpath.member;

import newtonpath.statemanager.Observable;
import newtonpath.statemanager.Parameter;

public class MemberItemFieldWrite implements Parameter {
	final MemberItemField member;
	private Object value;
	private String description;

	public MemberItemFieldWrite(MemberItemField _member) {
		this.member = _member;
		this.value = null;
		resetDescription();
	}

	public boolean execute(Object _o) throws Exception {
		boolean retVal = this.member.execSetValue(_o, this.value);
		resetDescription();
		return retVal;
	}

	public Observable getObservable() {
		return this.member;
	}

	public void setValue(String _s) {
		Class<?> c = this.member.getValueType();
		if (c.equals(java.lang.Double.TYPE)) {
			this.value = new Double(Double.parseDouble(_s));
		} else if (c.equals(java.lang.Integer.TYPE)) {
			this.value = new Integer(Integer.parseInt(_s));
		}
		resetDescription();
	}

	public Object getValue() {
		return this.value;
	}

	public String getSaveString() {
		StringBuffer sbRet;
		sbRet = new StringBuffer();
		sbRet.append('F');
		sbRet.append(';');
		sbRet.append(this.member.getDescription());
		sbRet.append(';');
		sbRet.append(this.value.toString());
		return sbRet.toString();
	}

	public String getDescription() {
		String result;
		result = this.member.getDescription();
		if (this.value != null) {
			result = result.concat("=").concat(this.value.toString());
		}
		return result;
	}

	public void resetDescription() {
		this.description = getDescription();
	}

	@Override
	public String toString() {
		return this.description;
	}
}
