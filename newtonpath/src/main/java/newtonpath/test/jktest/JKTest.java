package newtonpath.test.jktest;

import newtonpath.kepler.BodySystemRef;
import newtonpath.kepler.Funcions;
import newtonpath.kepler.Newton;
import newtonpath.ui.AproxObs;

public class JKTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		AproxObs i;
		// AproxObs.setEphLoader(new EphLoaderFile());
		double v[] = { 145.78010519032551, 0.010000000000000103,
				1.5707766802024616, 2.563692114465387, 0.0058851481300749384,
				-1.5707761418842034 };
		try {
			i = AproxObs.getInstance(BodySystemRef.getSolarSystemSpacecraft());
			i.loadEpoch(Newton.EPOCA_INI);
			i.getIntegrator().setInitStep();
			i.section.setAllBases(i.section.poincareStartBody);
			i.section.poincareStartVector = v.clone();

			i.section.poincareMap();
			Funcions.printVector(i.section.poincareEndVector);

			i.aproxMaxIter = 500;
			i.aproxStepPar = 5.0e-4D;
			i.aproxEndBodyDistance = 0.005;
			i.distanceApproximation();
			Funcions.printVector(i.section.poincareStartVector);

			i.section.poincareEndBody = 8;
			i.section.poincareMaxTime = 4000.0D;
			i.section.poincareMinTime = 500.0D;
			i.aproxMinStepPar = 1.0e-10D;
			i.aproxStepPar = 1.0e-5D;
			i.aproxEndBodyDistance = 0.0050;
			i.aproxMaxIter = 10;

			i.distanceApproximation();

			Funcions.printVector(i.section.poincareStartVector);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
