package newtonpath.ui;

import java.awt.Component;
import java.io.ObjectStreamException;

import newtonpath.application.JKApplication;


public interface ComponentConfiguration {

	public abstract Component getComponent(JKApplication context)
			throws ObjectStreamException;

}