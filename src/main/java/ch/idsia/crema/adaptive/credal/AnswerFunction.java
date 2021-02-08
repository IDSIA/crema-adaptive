package ch.idsia.crema.adaptive.credal;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    08.02.2021 13:44
 */
public interface AnswerFunction {

	/**
	 * Generates an answer for the given question.
	 *
	 * @param question the question to answer to
	 * @return 0 or 1 based on the question
	 */
	int answer(int question);

}
