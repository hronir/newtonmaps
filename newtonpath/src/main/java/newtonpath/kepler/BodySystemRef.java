/**
 * 
 */
package newtonpath.kepler;

import java.io.IOException;

import newtonpath.kepler.bodies.Body;
import newtonpath.kepler.bodies.Planet;
import newtonpath.kepler.bodies.Spacecraft;
import newtonpath.kepler.eph.EphLoader;
import newtonpath.kepler.eph.EphLoaderArray;


public class BodySystemRef {
	private final static double solSysMass[] = { 1D, 1D / 6023600.,
			1D / 408523.5, 1D / 332946.038 + 1D / 27068708.75, 1D / 3098710.,
			1D / 1047.355, 1D / 3498.5, 1D / 2286.9, 1D / 19314.0,
			1D / 3000000.0 };

	public static final int spaceCraftBodies[] = { -1, 0, 1, 2, 3, 4, 5, 6, 7,
			8, 9 };
	// public static final int spaceCraftBodies[]={-1,0,3,5,6,7,8};

	public static final int solarSystemBodies[] = { 0, 1, 2, 3, 4, 5, 6, 7, 8,
			9 };

	public static final Body solarSystemDescriptions[] = { Planet.SUN,
			Planet.MERCURY, Planet.VENUS, Planet.EARTH, Planet.MARS,
			Planet.JUPITER, Planet.SATURN, Planet.URANUS, Planet.NEPTUNE,
			Planet.PLUTO };

	public static final Body defaultSpacecraftDescriptor = new Spacecraft();

	public final int planetPos[] = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 };
	public final int planetId[];
	public final Body descriptions[];

	private void resetPlanetPos() {
		int i;
		for (i = 0; i < this.planetPos.length; i++) {
			this.planetPos[i] = -1;
		}
		if (this.planetId != null) {
			for (i = 0; i < this.planetId.length; i++) {
				if (this.planetId[i] >= 0) {
					this.planetPos[this.planetId[i]] = i;
					this.descriptions[i] = solarSystemDescriptions[this.planetId[i]];
				} else {
					this.descriptions[i] = defaultSpacecraftDescriptor;
				}
			}
		}
	}

	public BodySystemRef(int[] _planets) {
		this.descriptions = new Body[_planets.length];
		this.planetId = _planets.clone();
		resetPlanetPos();
	}

	public static BodySystemRef getSolarSystemSpacecraft() {
		return new BodySystemRef(spaceCraftBodies);
	}

	public double[] getMasses() {
		return systemMasses(this.planetId);
	}

	public static double[] systemMasses(int _planets[]) {
		double _ret[] = new double[_planets.length];
		int i;
		for (i = 0; i < _planets.length; i++) {
			if (_planets[i] >= 0) {
				_ret[i] = solSysMass[_planets[i]];
			}
		}
		return _ret;
	}

	public int getCentralBody() {
		return this.planetPos[0];
	}

	public int sun() {
		return this.planetPos[0];
	}

	public int mer() {
		return this.planetPos[1];
	}

	public int ven() {
		return this.planetPos[2];
	}

	public int ear() {
		return this.planetPos[3];
	}

	public int mar() {
		return this.planetPos[4];
	}

	public int jup() {
		return this.planetPos[5];
	}

	public int sat() {
		return this.planetPos[6];
	}

	public int ura() {
		return this.planetPos[7];
	}

	public int nep() {
		return this.planetPos[8];
	}

	public int plu() {
		return this.planetPos[9];
	}

	public double loadEpoch(double jd, double[] _epochPosition)
			throws Exception {
		double data_epoca, data;
		int i, j, pla;
		int carregat[] = new int[(this.planetPos.length + 1)];
		double coord[] = new double[6];

		double planetPosition[] = new double[(this.planetPos.length + 1) * 3];
		double planetSpeed[] = new double[(this.planetPos.length + 1) * 3];

		int firstSpeedPos = this.planetId.length * 3;
		this.ephLoader.ephOpen(jd);

		data = data_epoca = 0.;
		for (i = 0; i < this.planetPos.length; i++) {
			carregat[i] = 0;
		}

		for (i = 0; i < 3; i++) {
			// SUN
			planetPosition[3 * 1 + i] = 0.;
			planetSpeed[3 * 1 + i] = 0.;
		}
		double cols[];
		while ((cols = this.ephLoader.ephGet()) != null) {
			pla = (int) cols[0];
			data = cols[1];
			for (i = 0; i < 6; i++) {
				coord[i] = cols[2 + i];
			}
			if (pla >= 0) {
				if ((data < 1.) || ((data < jd) && (data > data_epoca + 1.))) {
					for (i = 0; i < this.planetId.length; i++) {
						carregat[i] = 0;
					}
					data_epoca = data;
				}
				if (Math.abs(data - data_epoca) < 1.) {
					carregat[pla] = 1;
					for (i = 0; i < 3; i++) {
						planetPosition[pla * 3 + i] = coord[i];
						planetSpeed[pla * 3 + i] = coord[3 + i];
					}
				}
			}
		}
		this.ephLoader.ephClose();

		for (i = 2; i < this.planetId.length; i++) {
			if (carregat[i] == 0) {
				throw new IOException("Invalid file format");
			}
		}

		planetPosition[0] = -planetPosition[3 * 6 + 0];
		planetPosition[1] = -planetPosition[3 * 6 + 1];
		planetPosition[2] = -planetPosition[3 * 6 + 2];
		planetSpeed[0] = -planetSpeed[3 * 6 + 0];
		planetSpeed[1] = -planetSpeed[3 * 6 + 1];
		planetSpeed[2] = -planetSpeed[3 * 6 + 2];

		for (i = 0; i < this.planetId.length; i++) {
			for (j = 0; j < 3; j++) {
				_epochPosition[i * 3 + j] = planetPosition[(this.planetId[i] + 1)
						* 3 + j];
				_epochPosition[firstSpeedPos + i * 3 + j] = planetSpeed[(this.planetId[i] + 1)
						* 3 + j];
			}
		}
		return data_epoca;
	}

	private EphLoader ephLoader = new EphLoaderArray();

	public void setEphLoader(EphLoader newLoader) {
		this.ephLoader = newLoader;
	}

	public int getPlanetIndex(Body b) {
		int i = -1;
		if (b instanceof Planet) {
			int j = 0;
			while (j < this.descriptions.length && i < 0) {
				if (this.descriptions[j].equals(b)) {
					i = j;
				}
				j++;
			}
		}
		return i;
	}
}