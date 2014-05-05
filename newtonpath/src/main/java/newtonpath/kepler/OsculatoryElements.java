/**
 * 
 */
package newtonpath.kepler;

import newtonpath.kepler.Funcions.Read;

public class OsculatoryElements {
	@Read
	public double excentricity;
	@Read
	public double major;
	@Read
	public double minor;
	@Read
	public double center[];
	@Read
	public double majorVec[];
	@Read
	public double minorVec[];
	@Read
	public double mu;
	@Read
	public double angularMom;
	@Read
	public double angularMomVec[];
	@Read
	public double reducedMass;

	public OsculatoryElements() {
		this.angularMomVec = new double[3];
		this.center = new double[3];
		this.majorVec = new double[3];
		this.minorVec = new double[3];
	}
}