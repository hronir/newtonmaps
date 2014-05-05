/**
 * 
 */
package newtonpath.kepler.bodies;

public class Spacecraft implements Body {
	static private long idcounter = 0;
	final private long id;

	public Spacecraft() {
		// super("Spacecraft");
		this.id = ++idcounter;
	}

	public boolean equals(Spacecraft o) {
		return o != null && o.id == this.id;
	}

	@Override
	public String toString() {
		return "Spacecraft";
	}
}