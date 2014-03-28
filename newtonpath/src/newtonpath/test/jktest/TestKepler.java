package newtonpath.test.jktest;

import newtonpath.kepler.BodySystemRef;
import newtonpath.kepler.Newton;
import newtonpath.ui.AproxObs;

public class TestKepler {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			AproxObs n = AproxObs.getInstance(BodySystemRef
					.getSolarSystemSpacecraft());
			n.loadEpoch(Newton.EPOCA_INI);
			n.section.poincareMinTime = 510D;
			n.getIntegrator().paramMaxStep = 100D;
			n.initialPlanetAprox();
			n.section.poincareMap();
			System.err.println(n.section.poincareStartVector[0]);
			System.err.println(n.section.poincareEndVector[0]);
			System.err.println(n.section.poincareEndPoint[0]);

			n.section.poincareInvertMap();
			n.section.poincareMap();
			System.err.println(n.section.poincareStartVector[0]);
			System.err.println(n.section.poincareEndVector[0]);
			System.err.println(n.section.poincareStartPoint[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
