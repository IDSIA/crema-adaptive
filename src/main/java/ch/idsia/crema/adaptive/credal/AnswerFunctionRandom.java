package ch.idsia.crema.adaptive.credal;

import java.util.Random;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    08.02.2021 13:45
 */
public class AnswerFunctionRandom implements AnswerFunction {

	final Random random;
	final int id;

	public AnswerFunctionRandom(int id) {
		this.random = new Random(id);
		this.id = id;
	}

	@Override
	public int answer(int ignored) {
		return random.nextInt(2);
	}
}
