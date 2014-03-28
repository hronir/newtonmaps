package newtonpath.member;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import newtonpath.kepler.Funcions;
import newtonpath.statemanager.Parameter;


public class MemberItemField extends AbstractMemberItem {

	private java.lang.reflect.Member field;
	private AbstractMemberItem subProp;
	private int[] index;

	@Override
	void copyTo(AbstractMemberItem c) {
		super.copyTo(c);
		c.description = this.description;
		((MemberItemField) c).field = this.field;
		if (this.subProp == null) {
			((MemberItemField) c).subProp = null;
		} else {
			((MemberItemField) c).subProp = (AbstractMemberItem) this.subProp
					.getCopy();
		}
		if (this.index == null) {
			((MemberItemField) c).index = null;
		} else {
			((MemberItemField) c).index = this.index.clone();
		}
	}

	public MemberItem getCopy() {
		MemberItemField c;
		c = new MemberItemField(getRoot());
		copyTo(c);
		return c;
	}

	public MemberItemField(MemberItemRoot _root) {
		super(_root);
		this.field = null;
		// v = null;
		this.index = null;
		this.subProp = null;
		this.description = "";
	}

	public MemberItemField(MemberItemRoot _root, java.lang.reflect.Member _f) {
		this(_root, null, _f, -1);
	}

	public MemberItemField(MemberItemRoot _root, MemberItemField _p,
			java.lang.reflect.Member _subf) {
		this(_root, _p, _subf, -1);
	}

	public MemberItemField(MemberItemRoot _root, MemberItemField _p, int _index) {
		this(_root, _p, null, _index);
	}

	public MemberItemField(MemberItemRoot _root, MemberItemField _p,
			java.lang.reflect.Member _subf, int _index) {
		super(_root);
		if (_p == null) {
			this.field = _subf;
			// v = null;
			if (_index < 0) {
				this.index = null;
			} else {
				this.index = new int[1];
				this.index[0] = _index;
			}
			this.subProp = null;
		} else {
			this.field = _p.field;
			// v = _p.v;
			if (_index < 0 || _p.subProp != null) {
				if (_p.index == null) {
					this.index = null;
				} else {
					this.index = _p.index.clone();
				}
			} else {
				int l;
				if (_p.index == null) {
					l = 0;
				} else {
					l = _p.index.length;
				}
				this.index = new int[l + 1];
				this.index[l] = _index;
				while (l > 0) {
					l--;
					this.index[l] = _p.index[l];
				}
			}
			if (_p.subProp == null) {
				this.subProp = null;
				if (_subf != null) {
					this.subProp = new MemberItemField(_root, _subf);
				}
			} else {
				if (_subf != null || _index >= 0) {
					this.subProp = new MemberItemField(_root,
							(MemberItemField) _p.subProp, _subf, _index);
				} else {
					this.subProp = (AbstractMemberItem) _p.subProp.getCopy();
				}
			}
		}
		setDescription();
	}

	public static MemberItemField extendMethod(MemberItemRoot _root,
			MemberItemField _p, java.lang.reflect.Method _meth) {
		MemberItemField retVal = (MemberItemField) _p.getCopy();
		MemberItemField c;
		c = retVal;
		while (c.subProp != null) {
			c = (MemberItemField) c.subProp;
		}
		c.subProp = new MemberItemField(_root, _meth);
		retVal.setDescription();
		return retVal;
	}

	// public void setDescription() {
	// description = getDescription();
	// }

	public String getDescription() {
		int i;
		String result;
		if (this.field != null) {
			result = this.field.getName();
			if (this.index != null) {
				for (i = 0; i < this.index.length; i++) {
					result = result + "[" + Integer.toString(this.index[i])
							+ "]";
				}
			}
			if (this.subProp != null) {
				result = result + "." + this.subProp.getDescription();
			}
		} else {
			result = "(null)";
		}
		return result;
	}

	public String getStringValue(Object _o) {
		Object o;
		String retval;
		o = getValue(_o);
		if (o == null) {
			retval = "null";
		} else if (o.getClass() == Integer.class
				|| o.getClass() == Double.class) {
			retval = o.toString();
		} else {
			retval = getTypeDescription(o);
		}
		return retval;
	}

	public boolean execSetValue(Object _o, Object _value) throws Exception {
		boolean bOk;
		if (this.subProp != null) {
			if (this.subProp instanceof MemberItemField) {
				bOk = ((MemberItemField) this.subProp)
						.execSetValue(getMemberArrayValue(this.field, _o,
								this.index), _value);
			} else {
				throw new IllegalArgumentException();
			}
		} else {
			bOk = false;
			int k = 0;
			if (getMemberType().isArray()) {
				Class<?> arr = getMemberType();
				Object o = getMemberValue(_o);
				while (arr.getComponentType().isArray()) {
					o = (((Object[]) o)[this.index[k]]);
					arr = arr.getComponentType();
					k = k + 1;
				}
				try {
					if (arr.getComponentType() == java.lang.Double.TYPE) {
						Array.setDouble(o, this.index[k], ((Double) _value)
								.doubleValue());
					} else if (arr.getComponentType() == java.lang.Integer.TYPE) {
						Array.setInt(o, this.index[k], ((Integer) _value)
								.intValue());
					}
					bOk = true;
				} catch (Exception e) {
					e.printStackTrace();
					bOk = false;
				}
			} else {
				try {
					if (this.field != null) {
						setMemberValue(_o, _value);
						bOk = true;
					}
				} catch (IllegalArgumentException ex) {
					ex.printStackTrace();
					bOk = false;
				} catch (IllegalAccessException ex) {
					ex.printStackTrace();
					bOk = false;
				}
			}
		}
		return bOk;
	}

	private void setMemberValue(Object _o, Object _value)
			throws IllegalAccessException {
		if (this.field instanceof Field) {
			((Field) this.field).set(_o, _value);
		}
	}

	private Object getMemberValue(Object _o) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		if (this.field instanceof Field) {
			return ((Field) this.field).get(_o);
		}
		return ((Method) this.field).invoke(_o);
	}

	public Object getValue(Object _o) {
		Object o;
		o = getMemberArrayValue(this.field, _o, this.index);
		if (o != null && this.subProp != null) {
			o = this.subProp.getValue(o);
		}
		return o;
	}

	public MemberItem[] getFieldArrayRef(Object o) {
		MemberItem[] result;
		int i, n, l, newind[];
		// Object o;
		result = null;
		// o=getValue(_o);
		if (o != null && o.getClass().isArray()) {
			n = ((this.index == null) ? 0 : this.index.length) + 1;
			newind = new int[n];
			for (i = 0; i < n - 1; i++) {
				newind[i] = this.index[i];
			}
			if (o.getClass() == double[].class) {
				l = ((double[]) o).length;
			} else if (o.getClass() == int[].class) {
				l = ((int[]) o).length;
			} else {
				l = ((Object[]) o).length;
			}
			result = new MemberItem[l];
			for (i = 0; i < l; i++) {
				newind[n - 1] = i;
				result[i] = new MemberItemField(getRoot(), this, i);
			}
		}
		return result;
	}

	public Class<?> getValueType() {
		if (this.subProp != null) {
			return this.subProp.getValueType();
		}
		Class<?> c;
		int i = 0;
		c = getMemberType();
		if (this.index != null) {
			i = this.index.length;
			while (i > 0 && c.isArray()) {
				c = c.getComponentType();
				i--;
			}
		}
		return c;
	}

	private Class<?> getMemberType() {
		if (this.field instanceof Field) {
			return ((Field) this.field).getType();
		}
		return ((Method) this.field).getReturnType();
	}

	@Override
	public MemberItem[] getMembers(Object _o) {
		MemberItem[] sub = null;
		if (getValueType().isArray()) {
			Object o = getValue(_o);
			if (o != null) {
				sub = getFieldArrayRef(o);
			}
		} else {
			sub = getRoot().memberList(getValueType(), this);
		}
		if (sub != null && sub.length == 0) {
			sub = null;
		}
		return sub;
	}

	public boolean isAnnotationPresent(
			Class<? extends Annotation> annotationClass) {
		return isMemberAnotationPresent(annotationClass);
	}

	private boolean isMemberAnotationPresent(
			Class<? extends Annotation> annotationClass) {
		if (this.field instanceof Field) {
			return ((Field) this.field).isAnnotationPresent(annotationClass);
		}
		return ((Method) this.field).isAnnotationPresent(annotationClass);
	}

	public Parameter getParameter() {
		return new MemberItemFieldWrite(this);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (super.equals(obj)) {
			return true;
		}
		if (!getClass().equals(obj.getClass())) {
			return false;
		}
		final MemberItemField f = (MemberItemField) obj;
		if (!this.field.equals(f.field)) {
			return false;
		}
		if (this.subProp == null) {
			if (f.subProp != null) {
				return false;
			}
		} else {
			if (!this.subProp.equals(f.subProp)) {
				return false;
			}
		}
		if (this.index == null) {
			if (f.index != null) {
				return false;
			}
		} else {
			if (f.index == null || Arrays.equals(this.index, f.index)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		int retVal = Arrays.hashCode(this.index) + this.field.hashCode();
		if (this.subProp != null) {
			retVal += this.subProp.hashCode();
		}
		return retVal;
	}

	public String[] getParameterAnnotation() {
		Funcions.Parameter p;
		if (this.field instanceof Field) {
			p = ((Field) this.field).getAnnotation(Funcions.Parameter.class);
		} else {
			p = ((Method) this.field).getAnnotation(Funcions.Parameter.class);
		}
		return p == null ? null : p.value();
	}

}
