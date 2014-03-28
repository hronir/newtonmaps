/*
 * MemberItemMethod.java
 *
 * Created on 10 de noviembre de 2007, 12:37
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package newtonpath.member;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

import newtonpath.kepler.Funcions;
import newtonpath.statemanager.ObservableArray;
import newtonpath.statemanager.Operation;
import newtonpath.statemanager.Parameter;


/**
 * 
 * @author oriol
 */
public class MemberItemMethod extends AbstractMemberItem implements Operation {

	private java.lang.reflect.Method method;

	/** Creates a new instance of MemberItemMethod */
	MemberItemMethod(MemberItemRoot _root) {
		super(_root);
		this.method = null;
	}

	public MemberItemMethod(MemberItemRoot _root, java.lang.reflect.Method _m) {
		super(_root);
		this.method = _m;
		setDescription();
	}

	@Override
	void copyTo(AbstractMemberItem c) {
		super.copyTo(c);
		((MemberItemMethod) c).method = this.method;
	}

	
	public MemberItem getCopy() {
		MemberItemMethod c;
		c = new MemberItemMethod(getRoot());
		copyTo(c);
		return c;
	}

	
	public String getDescription() {
		String result = getOperationName();
		if (result == null) {
			result = this.method.getName().concat("()");
		}
		return result;
	}

	
	public boolean execute(Object _o) throws Exception {
		try {
			this.method.invoke(_o, (Object[]) null);
			return true;
		} catch (InvocationTargetException ex) {
			if (ex.getCause() != null && (ex.getCause() instanceof Exception)) {
				throw (Exception) ex.getCause();
			}
			ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	
	public String getSaveString() {
		StringBuffer sbRet;
		sbRet = new StringBuffer();
		sbRet.append('M');
		sbRet.append(';');
		sbRet.append(this.method.getName().toString());
		sbRet.append(';');
		return sbRet.toString();
	}

	
	public Object getValue(Object _o) {
		Object retval = null;
		try {
			retval = this.method.invoke(_o, (Object[]) null);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			retval = null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			retval = null;
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			retval = null;
		}
		return retval;
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

	
	public Class<?> getValueType() {
		return this.method.getReturnType();
	}

	
	public boolean isAnnotationPresent(
			Class<? extends Annotation> annotationClass) {
		return this.method.isAnnotationPresent(annotationClass);
	}

	
	public Parameter getParameter() {
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (super.equals(obj)) {
			return true;
		}
		if (!obj.getClass().equals(this.getClass())) {
			return false;
		}
		return (this.method.equals(((MemberItemMethod) obj).method));
	}

	@Override
	public int hashCode() {
		return this.method.hashCode();
	}

	public String getOperationName() {
		Funcions.Operation p = this.method
				.getAnnotation(Funcions.Operation.class);
		return p == null ? null : p.value();
	}

	
	public String[] getParameterAnnotation() {
		return null;
	}

	
	public ObservableArray getParameters(Object _o) {
		return new MemberItemRoot(getRoot().getValueType(), getOperationName());
	}
}
