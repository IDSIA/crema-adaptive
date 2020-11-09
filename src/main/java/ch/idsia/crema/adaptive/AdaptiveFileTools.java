package ch.idsia.crema.adaptive;

import ch.idsia.crema.factor.credal.vertex.generator.CNGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Pattern;

public class AdaptiveFileTools {

	/**
	 * Check if a string is a number.
	 *
	 * @param str input string
	 * @return true if it is a number, otherwise false
	 */
	public static boolean isNumeric(String str) {
		try {
			Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	/**
	 * Read a credal network specification and write a consistent Bayesian network specification
	 *
	 * @param nameFile input filename
	 */
	public static void writeBNFile(String nameFile) {
		String[] parts = nameFile.split(Pattern.quote("."));
		String newNameFile = parts[0] + "Bayes." + parts[1];

		double[][] credalValues = readMyFile(nameFile);
		double[][] bayesianValues = credalValues.clone();
		double[][] bounds = new double[2][4];

		CNGenerator myGen = new CNGenerator();

		for (int i = 0; i <= 8; i = i + 2) {
			bounds[0] = credalValues[i];
			bounds[1] = credalValues[i + 1];
			bayesianValues[i] = myGen.fromIntervalsToCoM(bounds);
			bayesianValues[i + 1] = bayesianValues[i];
		}

		for (int i = 0; i < credalValues.length; i++) {
			if (i > 9) {
				bayesianValues[i][0] = (credalValues[i][0] + credalValues[i][1]) / 2;
				bayesianValues[i][1] = bayesianValues[i][0];
			}
		}

		try (PrintStream output = new PrintStream(new File(newNameFile))) {
			output.println("// Sample Credal network specification");
			for (double[] bayesianValue : bayesianValues) {
				for (double value : bayesianValue) {
					// display ss to make sure program works correctly
					output.printf(Locale.ROOT, "%2.10f ", value);
				}
				output.print("\n");
			}
		} catch (FileNotFoundException e) {
			System.out.println("Cannot write file!");
		}
	}

	/**
	 * Red a file containing the parameters of a credal network.
	 *
	 * @param nameFile input file
	 * @return a bi-dimensional array
	 */
	public static double[][] readMyFile(String nameFile) {
		double[][] output = new double[26][4];
		for (int i = 0; i < output.length; i++)
			for (int j = 0; j < output[0].length; j++)
				output[i][j] = Double.NaN;

		try (Scanner scan = new Scanner(new File(nameFile))) {
			int rowNumber = 0;
			int col;

			while (scan.hasNext()) {
				String line = scan.nextLine();
				if (!(line.startsWith("//"))) { // Ignore comments
					col = 0;
					for (String element : line.split(" ")) {
						if (isNumeric(element)) {
							output[rowNumber][col] = Double.parseDouble(element);
							col++;
						}
					}
					rowNumber++;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return output;
	}
}