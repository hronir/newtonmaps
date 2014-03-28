/**
 * 
 */
package newtonpath.kepler.eph;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class EphLoaderFile implements EphLoader {
	private BufferedReader f;

	// private boolean debug=false;
	public void ephOpen(double jday) throws Exception {
		this.f = new BufferedReader(new InputStreamReader(EphLoaderFile.class
				.getResourceAsStream("eph.dat")));
	}

	private String ephGetLine() throws Exception {
		return this.f.readLine();
	}

	public double[] ephGet() throws Exception {
		int i;
		String linia = ephGetLine();
		double[] result;
		if (linia == null) {
			result = null;
		} else {
			String columnes[];
			columnes = linia.split(" +");
			result = new double[columnes.length];
			if (columnes.length == 8) {
				/*
				 * if (columnes[1].trim().startsWith("2454301")){
				 * System.out.print("{"); System.out.print(columnes[0]); for (i
				 * = 1; i < columnes.length; i++){ System.out.print(",");
				 * System.out.print(columnes[i]); } System.out.println("},"); }
				 */
				try {
					for (i = 0; i < columnes.length; i++) {
						result[i] = Double.parseDouble(columnes[i]);
					}
				} catch (Exception e) {
					result = null;
				}
			}
		}
		return result;
	}

	public void ephClose() throws Exception {
		this.f.close();
		this.f = null;
	}
}