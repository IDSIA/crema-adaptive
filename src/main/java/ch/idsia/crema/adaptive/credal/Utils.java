package ch.idsia.crema.adaptive.credal;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    05.02.2021 16:03
 */
public class Utils {

	public static double H(double[] d) {
		double h = 0.0;

		for (double v : d) {
			// log base 4
			double logXv = Math.log(v) / Math.log(d.length);
			h += v * logXv;
		}

		return -h;
	}

}
