package ch.idsia.crema.adaptive;

import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: Credo3
 * Date:    21.02.2017 16:23
 */
public class AnswerSet {

	/**
	 * answers foreach student
	 */
	private int[][] answers;
	private int[] questionToIndex = null;

	public AnswerSet() {
	}

	public AnswerSet load(String filename) {
		List<String> records = new ArrayList<>();

		try (Scanner s = new Scanner(new File(filename))) {
			String line = s.nextLine();
			String[] tokens = line.split(",");
			questionToIndex = new int[tokens.length];

			for (int i = 0; i < tokens.length; i++) {
				questionToIndex[i] = Integer.parseInt(tokens[i].substring(1));
			}

			while (s.hasNext()) {
				line = s.nextLine();
				records.add(line);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// -1 is to avoid header
		answers = new int[records.size()][];

		for (int i = 0; i < records.size(); i++) {
//			String[] tokens = records.get(i).split(",");
			String[] tokens = records.get(i).split(" *,");
			int answerNumber = tokens.length;

			// -1 is to avoid id
			answers[i] = new int[answerNumber];
			for (int j = 0; j < answerNumber; j++) {
				String token = tokens[j];
				answers[i][j] = Integer.parseInt(token);
			}
		}

		return this;
	}

	public int getAnswer(int student, int question) {
		int idx = ArrayUtils.indexOf(questionToIndex, question);
		return answers[student][idx];
	}

}
