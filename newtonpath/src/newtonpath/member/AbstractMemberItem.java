package newtonpath.member;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import newtonpath.kepler.Funcions;
import newtonpath.statemanager.Observable;
import newtonpath.statemanager.ObservableArray;
import newtonpath.statemanager.Operation;


/**
 * 
 * @author oriol
 */
public abstract class AbstractMemberItem implements MemberItem {

	protected String description;
	private final MemberItemRoot root;

	AbstractMemberItem(MemberItemRoot _root) {
		this.root = _root;
		this.description = "";
	}

	protected static Class<?> finalComponentType(Class<?> _c) {
		Class<?> v;
		v = _c;
		while (v.isArray()) {
			v = v.getComponentType();
		}
		return v;
	}

	public static Object getMemberArrayValue(java.lang.reflect.Member _p,
			Object _o, int _i[]) {
		if (_p instanceof Field) {
			return getFieldArrayValue((Field) _p, _o, _i);
		}
		return getMethodArrayValue((Method) _p, _o, _i);
	}

	public static Object getFieldArrayValue(java.lang.reflect.Field _p,
			Object _o, int _i[]) {
		int n, k;
		Object o = null;
		n = (_i == null) ? 0 : _i.length;
		try {
			if (_p.getType().equals(java.lang.Integer.TYPE)) {
				o = new Integer(_p.getInt(_o));
			} else if (_p.getType().equals(java.lang.Double.TYPE)) {
				o = new Double(_p.getDouble(_o));
			} else {
				o = _p.get(_o);
				for (k = 0; k < n && o != null && o.getClass().isArray(); k++) {
					if (o.getClass() == (double[].class)) {
						o = new Double(((double[]) o)[_i[k]]);
					} else if (o.getClass() == (int[].class)) {
						o = new Integer(((int[]) o)[_i[k]]);
					} else {
						o = (((Object[]) o)[_i[k]]);
					}
				}
			}
		} catch (Exception ex) {
			System.out.println(_p.getType().getComponentType().getName() + "\t"
					+ _p.getName() + "\t" + _o.getClass().getName() + "\t"
					+ Integer.toString(n));
		}
		return o;
	}

	public static Object getMethodArrayValue(java.lang.reflect.Method _p,
			Object _o, int _i[]) {
		int n, k;
		Object o = null;
		n = (_i == null) ? 0 : _i.length;
		try {
			if (_p.getReturnType().equals(java.lang.Integer.TYPE)) {
				o = _p.invoke(_o);
			} else if (_p.getReturnType().equals(java.lang.Double.TYPE)) {
				o = _p.invoke(_o);
			} else {
				o = _p.invoke(_o);
				for (k = 0; k < n && o != null && o.getClass().isArray(); k++) {
					if (o.getClass() == (double[].class)) {
						o = new Double(((double[]) o)[_i[k]]);
					} else if (o.getClass() == (int[].class)) {
						o = new Integer(((int[]) o)[_i[k]]);
					} else {
						o = (((Object[]) o)[_i[k]]);
					}
				}
			}
		} catch (Exception ex) {
			System.out.println(_p.getReturnType().getComponentType().getName()
					+ "\t" + _p.getName() + "\t" + _o.getClass().getName()
					+ "\t" + Integer.toString(n));
		}
		return o;
	}

	public static String getTypeDescription(Object o) {
		StringBuffer sbArray;
		Class<?> v;
		if (o == null) {
			return "null";
		}
		v = o.getClass();
		sbArray = new StringBuffer();
		while (v.isArray()) {
			v = v.getComponentType();
			sbArray.append("[ ]");
		}
		return v.getName().concat(sbArray.toString());
	}

	protected static boolean hasAnnotation(
			java.lang.reflect.AnnotatedElement f,
			Class<? extends Annotation>[] annot) {
		int i;
		if (annot == null) {
			return false;
		}
		for (i = 0; i < annot.length; i++) {
			if (f.isAnnotationPresent(annot[i])) {
				return true;
			}
		}
		return false;
	}

	protected static MemberItem[] memberList(MemberItemRoot _root, Class<?> _c,
			Class<? extends Annotation>[] annot, MemberItemField MemberOf) {
		Vector<MemberItem> x = new Vector<MemberItem>();
		int i;
		java.lang.reflect.Field f;
		java.lang.reflect.Method m;
		for (i = 0; i < _c.getFields().length; i++) {
			f = _c.getFields()[i];
			if (!java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
				if (hasAnnotation(f, annot)) {
					x.add(new MemberItemField(_root, MemberOf, f));
				}
			}
		}
		for (i = 0; i < _c.getMethods().length; i++) {
			m = _c.getMethods()[i];
			if ((!java.lang.reflect.Modifier.isStatic(m.getModifiers()))) {
				if (hasAnnotation(m, annot)) {
					if (MemberOf == null) {
						x.add(new MemberItemField(_root, m));
					} else {
						x.add(MemberItemField.extendMethod(_root, MemberOf, m));
					}
				}
			}
		}
		return x.toArray(new MemberItem[x.size()]);
	}

	protected static MemberItem[] operationsList(MemberItemRoot _root,
			Class<?> _c, Class<? extends Annotation>[] annot) {
		Vector<MemberItem> x = new Vector<MemberItem>();
		int i;
		java.lang.reflect.Field f;
		java.lang.reflect.Method m;

		for (i = 0; i < _c.getMethods().length; i++) {
			m = _c.getMethods()[i];
			if ((!java.lang.reflect.Modifier.isStatic(m.getModifiers()))) {
				if (hasAnnotation(m, annot)) {
					x.add(new MemberItemMethod(_root, m));
				}
			}
		}
		return x.toArray(new MemberItem[x.size()]);
	}

	void copyTo(AbstractMemberItem c) {
		c.description = this.description;
	}

	// public abstract boolean execute(Object _o) throws Exception;

	// public MemberItem[] getAssociativeMembers(Object o, boolean parents) {
	// Vector<MemberItem> retval = new Vector<MemberItem>();
	// MemberItem[] currentlevel = getMembers(o), subLevel;
	// int i, j;
	// if (currentlevel!=null)
	// for (i = 0; i < currentlevel.length; i++) {
	// if (root.getAnnotations() == null ||
	// currentlevel[i].isAnnotationPresent(root.getAnnotations())) {
	// subLevel = currentlevel[i].getAssociativeMembers(o, parents);
	// if (parents || subLevel == null || subLevel.length == 0) {
	// retval.add(currentlevel[i]);
	// }
	// if (subLevel != null)
	// for (j = 0; j < subLevel.length; j++)
	// retval.add(subLevel[j]);
	// }
	// }
	// if (retval.size() == 0)
	// return null;
	// return retval.toArray(new MemberItem[retval.size()]);
	// }

	public MemberItem[] getMembers(Object o) {
		return null;
	}

	public boolean isAnnotationPresent(
			Class<? extends Annotation> annotationType[]) {
		if (annotationType == null) {
			return false;
		}
		int i;
		for (i = 0; i < annotationType.length; i++) {
			if (isAnnotationPresent(annotationType[i])) {
				return true;
			}
		}
		return false;
	}

	public void setDescription() {
		this.description = getDescription();
	}

	@Override
	public String toString() {
		return this.description;
	}

	public double getDoubleValue(Object _o) {
		double retval = 0D;
		Object v;
		v = getValue(_o);
		if (v.getClass() == java.lang.Double.class) {
			retval = ((Double) v).doubleValue();
		} else if (v.getClass() == java.lang.Integer.class) {
			retval = ((Integer) v).doubleValue();
		}
		return retval;
	}

	public MemberItemRoot getRoot() {
		return this.root;
	}

	public ObservableArray[] getArrays(Object o) {
		return getObservableArrays(getMembers(o));
	}

	public Observable[] getComponents(Object o) {
		return getObservables(getMembers(o));
	}

	public static Observable[] getObservables(MemberItem[] m) {
		return asObservables(filterObservables(m, false));
	}

	public static ObservableArray[] getObservableArrays(MemberItem[] m) {
		return asObservableArrays(filterObservables(m, true));
	}

	public static Operation[] getOperations(MemberItem[] m) {
		return asOperations(filterOperations(m));
	}

	private static ArrayList<MemberItem> filterObservables(MemberItem[] m,
			boolean _array) {
		ArrayList<MemberItem> l = null;
		if (m != null) {
			l = new ArrayList<MemberItem>(m.length);
			for (MemberItem it : m) {
				if (_array == it.isAggregate()) {
					l.add(it);
				}
			}
		}
		return l;
	}

	private static ArrayList<Operation> filterOperations(MemberItem[] m) {
		ArrayList<Operation> l = null;
		if (m != null) {
			l = new ArrayList<Operation>(m.length);
			for (MemberItem it : m) {
				if (it instanceof MemberItemMethod) {
					l.add((MemberItemMethod) it);
				}
			}
		}
		return l;
	}

	public static Observable[] asObservables(Collection<MemberItem> l) {
		Observable[] retVal = null;
		if (l != null && l.size() > 0) {
			retVal = l.toArray(new Observable[l.size()]);
		}
		return retVal;
	}

	public static ObservableArray[] asObservableArrays(Collection<MemberItem> l) {
		ObservableArray[] retVal = null;
		if (l != null && l.size() > 0) {
			retVal = l.toArray(new ObservableArray[l.size()]);
		}
		return retVal;
	}

	private static Operation[] asOperations(Collection<Operation> l) {
		Operation[] retVal = null;
		if (l != null && l.size() > 0) {
			retVal = l.toArray(new Operation[l.size()]);
		}
		return retVal;
	}

	public static boolean isAggregate(Class<?> c) {
		return !(c.equals(Integer.TYPE) || c.equals(Double.TYPE));
	}

	public boolean isAggregate() {
		return isAggregate(getValueType());
	}

	private static final Class<Annotation>[] PARAMETER_ANNOTATIONS = parameterAnnotations();

	@SuppressWarnings("unchecked")
	private static Class<Annotation>[] parameterAnnotations() {
		return new Class[] { Funcions.Parameter.class };
	}

	public static MemberItem[] getParameters(MemberItemRoot _root, Class<?> _c,
			String _operationName, MemberItemField memberOf) {
		MemberItem[] m = AbstractMemberItem.memberList(_root, _c,
				PARAMETER_ANNOTATIONS, memberOf);
		ArrayList<MemberItem> l = null;
		if (m != null) {
			l = new ArrayList<MemberItem>(m.length);
			for (MemberItem i : m) {
				String[] o = i.getParameterAnnotation();
				for (String s : o) {
					if (s.equals(_operationName)) {
						l.add(i);
						break;
					}
				}
			}
		}
		return l == null || l.size() == 0 ? null : l.toArray(new MemberItem[l
				.size()]);
	}

}
