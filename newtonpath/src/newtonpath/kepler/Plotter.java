/*
 * Plotter.java
 *
 * Created on 24 de septiembre de 2007, 23:09
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package newtonpath.kepler;

/**
 * 
 * @author oriol
 */
public abstract interface Plotter {
	void plotState(RK78 _o);
}
