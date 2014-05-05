package newtonpath.member;

import java.lang.annotation.Annotation;

import newtonpath.statemanager.Observable;
import newtonpath.statemanager.ObservableArray;
import newtonpath.statemanager.Operation;


public class MemberItemRoot implements ObservableArray {
	private final Class<?> rootClass;
	private final Class<? extends Annotation>[] annotations;
	private final String operationParameters;

	public MemberItemRoot(Class<?> c) {
		this(c, null, null);
	}

	public MemberItemRoot(Class<?> c, String _operationParameters) {
		this(c, null, _operationParameters);
	}

	public MemberItemRoot(Class<?> c, Class<? extends Annotation>[] _annotations) {
		this(c, _annotations, null);
	}

	protected MemberItemRoot(Class<?> c,
			Class<? extends Annotation>[] _annotations,
			String _operationParameters) {
		super();
		this.rootClass = c;
		this.annotations = _annotations == null ? null : _annotations.clone();
		this.operationParameters = _operationParameters;
	}

	public MemberItemRoot getRoot() {
		return this;
	}

	public String getDescription() {
		return this.operationParameters == null ? "Orbital data" : "Parameters";
	}

	public String getSaveString() {
		return null;
	}

	public String getStringValue(Object _o) {
		return _o.toString();
	}

	public Object getValue(Object _o) {
		return _o;
	}

	public Class<?> getValueType() {
		return this.rootClass;
	}

	public boolean isAnnotationPresent(
			Class<? extends Annotation> annotationType) {
		return false;
	}

	private MemberItem[] getMembers(Object o) {
		return memberList(this.rootClass, null);
	}

	public ObservableArray[] getArrays(Object o) {
		return AbstractMemberItem.getObservableArrays(getMembers(o));
	}

	public Observable[] getComponents(Object o) {
		return AbstractMemberItem.getObservables(getMembers(o));
	}

	public Operation[] getOperations(Object o) {
		return AbstractMemberItem.getOperations(AbstractMemberItem
				.operationsList(this, this.rootClass, this.annotations));
	}

	protected MemberItem[] memberList(Class<?> _c, MemberItemField MemberOf) {
		if (this.operationParameters == null) {
			return AbstractMemberItem.memberList(this, _c, this.annotations,
					MemberOf);
		}
		return AbstractMemberItem.getParameters(this, _c,
				this.operationParameters, MemberOf);
	}

	@Override
	public String toString() {
		return getDescription();
	}
}
