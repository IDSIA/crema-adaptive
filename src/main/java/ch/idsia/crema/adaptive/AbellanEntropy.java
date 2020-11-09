package ch.idsia.crema.adaptive;

import java.util.Arrays;
import java.util.stream.DoubleStream;

public class AbellanEntropy {

	public static void main(String[] args) {
		AbellanEntropy ae = new AbellanEntropy();

		double[] l;
		double[] u;
		double[] e;

		l = new double[]{0.3, 0.4, 0.0, 0.0, 0.0};
		u = new double[]{1.0, 1.0, 0.04, 1.0, 1.0};

		e = ae.getMaxEntropy(l, u);
		System.out.println(Arrays.toString(e));

		l = new double[]{0.08771929824561402, 0.21052631578947367, 0.3508771929824561, 0.3508771929824561};
		u = new double[]{0.08771929824561402, 0.21052631578947367, 0.3508771929824561, 0.3508771929824561};

		e = ae.getMaxEntropy(l, u);
		System.out.println(Arrays.toString(e));

//		l = new double[]{0.3164556962025316, 0.3164556962025316, 0.05063291139240505, 0.3164556962025316};
//		u = new double[]{0.31645569620253167, 0.31645569620253167, 0.05063291139240508, 0.31645569620253167};
//
//		e = ae.getMaxEntropy(l, u);
//		System.out.println(Arrays.toString(e));
	}

	/**
	 * Return the index of the minimum value of arr among the values such that the corresponding elements of b are true.
	 *
	 * @param arr input array
	 * @param b   filter array
	 * @return the index of the minimum value
	 */
	private int minLS(double[] arr, boolean[] b) {
		int index = 0;
		boolean[] b2 = b.clone();
		double myMin;

		if (!allFalse(b2)) {
			for (int i = 0; i < arr.length; i++) {
				if (b2[i]) {
					index = i;
					break;
				}
			}
			myMin = arr[index];
			for (int i = 0; i < b2.length; i++) {
				if (b2[i]) {
					if (arr[i] < myMin) {
						myMin = arr[i];
						index = i;
					}
				}
			}
		} else {
			index = -1;
		}

		return index;
	}

	/**
	 * Return the number of occurrences of the minimum of arr only over the values of arr such that the corresponding
	 * element of b is true.
	 *
	 * @param arr input array
	 * @param b   filter array
	 * @return the number of element around the minimum
	 */
	private int nMinLS(double[] arr, boolean[] b) {
		double myMin = arr[minLS(arr, b)];
		int q = 0;
		for (int i = 0; i < b.length; i++) {
			if (b[i]) {
				if (Math.abs(arr[i] - myMin) < 1E-10) {
					q++;
				}
			}
		}
		return q;
	}

	/**
	 * Find the index of the second smallest element of arr among the values corresponding to the true values of b
	 *
	 * @param arr input array
	 * @param b   filter array
	 * @return the second smallest element
	 */
	private int secondMinLS(double[] arr, boolean[] b) {
		boolean[] b2 = b.clone();
		int index = minLS(arr, b2);
		double min1 = arr[index];
		for (int i = 0; i < arr.length; i++)
			if (arr[i] == min1)
				b2[i] = false;
//		int index2 = -1;
//		if (index != -1) {
//			b[index] = false;
//			index2 = minLS(arr, b2);
//		}
//		return index2;
		return minLS(arr, b2);
	}

	/**
	 * Boolean function to check whether or not all the elements of arr are false.
	 *
	 * @param arr input array
	 * @return true if all elements are false, otherwise true
	 */
	private boolean allFalse(boolean[] arr) {
		for (boolean b : arr)
			if (b)
				return false;
		return true;
	}

	public double[] getMaxEntropy(double[] l, double[] u) {
		// ALGORITHM
		double ss;
		int r, f, m;
		boolean[] S = new boolean[l.length];
		for (int i = 0; i < l.length; i++) {
			S[i] = true;
		}
		// S initialisation
		while (DoubleStream.of(l).sum() < 1.0) {
			for (int i = 0; i < l.length; i++) {
				if (u[i] == l[i]) {
					S[i] = false;
				}
			}
			ss = DoubleStream.of(l).sum();
			r = minLS(l, S);
			f = secondMinLS(l, S);
			m = nMinLS(l, S);
			for (int i = 0; i < l.length; i++) {
				if (l[i] == l[minLS(l, S)]) {
					if (f == -1) {
						l[i] += Math.min(u[i] - l[i], Math.min((1 - ss) / m, 1));
					} else {
						l[i] += Math.min(u[i] - l[i], Math.min(l[f] - l[r], (1 - ss) / m));
					}
				}
			}
		}
		return l;
	}
}