package ch.idsia.crema.adaptive.experiments;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    05.02.2021 16:03
 */
public class Utils {

	public static final String separator = ",";
	protected static final NumberFormat nf = NumberFormat.getNumberInstance(Locale.ROOT);

	static {
		nf.setMaximumFractionDigits(4);
		nf.setMinimumFractionDigits(4);
	}

	public static NumberFormat numberFormat() {
		return nf;
	}

	public static double H(double[] d) {
		double h = 0.0;

		for (double v : d) {
			// log base 4
			double logXv = Math.log(v) / Math.log(d.length);
			h += v * logXv;
		}

		return -h;
	}

	public static int argmax(double[] values) {
		double v = values[0];
		int maxI = 0;

		for (int i = 1; i < values.length; i++) {
			if (values[i] > v) {
				v = values[i];
				maxI = i;
			}
		}

		return maxI;
	}
}
