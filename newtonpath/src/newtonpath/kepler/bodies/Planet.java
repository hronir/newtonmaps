/**
 * 
 */
package newtonpath.kepler.bodies;

public enum Planet implements Body {
	SUN(0), MERCURY(1), VENUS(2), EARTH(3), MARS(4), JUPITER(5), SATURN(6), URANUS(
			7), NEPTUNE(8), PLUTO(9), ;

	final private int planet;
	final private static String[] names = { "Sun", "Mercury", "Venus", "Earth",
			"Mars", "Jupiter", "Saturn", "Uranus", "Neptune", "Pluto" };

	Planet(int _pl) {
		this.planet = _pl;
	}

	public int getIndex() {
		return this.planet;
	}

	@Override
	public String toString() {
		return names[this.planet];
	}
}