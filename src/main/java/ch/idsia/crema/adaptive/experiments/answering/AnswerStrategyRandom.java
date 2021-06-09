package ch.idsia.crema.adaptive.experiments.answering;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.agents.AgentStudent;

import java.util.Random;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    08.02.2021 18:09
 */
public class AnswerStrategyRandom<F extends AgentStudent> implements AnswerStrategy<F> {

	private final Random random;

	public AnswerStrategyRandom(long seed) {
		random = new Random(seed);
	}

	/**
	 * @param student  who need to give the answer
	 * @param question chosen by a {@link ch.idsia.crema.adaptive.experiments.agents.Teacher}
	 * @return generated from a uniform distribution
	 */
	@Override
	public int answer(F student, Question question) {
		final int x = random.nextInt(2);
		System.out.printf("AgentStudent:       question=%-3d answer=%d%n", question.variable, x);
		return x;
	}
}
