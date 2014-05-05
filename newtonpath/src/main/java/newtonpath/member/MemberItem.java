package newtonpath.member;

import java.lang.annotation.Annotation;

import newtonpath.statemanager.Observable;
import newtonpath.statemanager.ObservableArray;


public interface MemberItem extends Observable, ObservableArray {

	// public MemberItem[] getAssociativeMembers(Object o,boolean parents);

	public MemberItem getCopy();

	public MemberItem[] getMembers(Object o);

	public boolean isAnnotationPresent(
			Class<? extends Annotation> annotationType);

	public boolean isAnnotationPresent(
			Class<? extends Annotation> annotationType[]);

	public boolean isAggregate();

	public String[] getParameterAnnotation();

}