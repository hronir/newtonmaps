/*
 * JKPanelList.java
 *
 * Created on 18 octobre 2007, 13:43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package newtonpath.ui;

import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JMenuItem;

import newtonpath.kepler.events.Event;


/**
 *
 */
public class JKViewList<E> implements JKView<E> {
	private E integ;
	private ArrayList<JKView<E>> panels;

	/** Creates a new instance of JKPanelList */
	public JKViewList() {
		this.integ = null;
		this.panels = new ArrayList<JKView<E>>(8);
	}

	public void addView(JKView<E> _newpanel) {
		if (!this.panels.contains(_newpanel)) {
			this.panels.add(_newpanel);
			if (this.integ != null) {
				_newpanel.resetView(this.integ);
			}
		}
	}

	public void removeView(JKView<E> _panel) {
		this.panels.remove(_panel);
	}

	public void updateView(E integ) {
		int i;
		for (i = 0; i < this.panels.size(); i++) {
			this.panels.get(i).updateView(integ);
		}
	}

	public void resetView(E integrador) {
		int i;
		this.integ = integrador;
		for (i = 0; i < this.panels.size(); i++) {
			this.panels.get(i).resetView(integrador);
		}
	}

	public E getInteg() {
		return this.integ;
	}

	public void prepareContextMenu(Vector<JMenuItem> mnu, Event dynEvt) {
	}

	public void finishContextMenu() {

	}
	
	public void setParent(JKViewList<E> parent) {
	}
}
