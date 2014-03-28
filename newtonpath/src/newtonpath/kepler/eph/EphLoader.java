/*
 * EphLoader.java
 *
 * Created on 13 novembre 2007, 13:57
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package newtonpath.kepler.eph;

/**
 * 
 * @author OAL
 */
public interface EphLoader {
	void ephClose() throws Exception;

	double[] ephGet() throws Exception;

	void ephOpen(double jday) throws Exception;

}
