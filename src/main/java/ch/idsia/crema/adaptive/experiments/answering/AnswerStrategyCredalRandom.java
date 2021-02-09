package ch.idsia.crema.adaptive.experiments.answering;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.model.graphical.DAGModel;

import java.util.Random;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    08.02.2021 18:09
 */
public class AnswerStrategyCredalRandom implements AnswerStrategy<IntervalFactor> {

	final Random random;

	public AnswerStrategyCredalRandom(long seed) {
		random = new Random(seed);
	}

	@Override
	public int answer(DAGModel<IntervalFactor> model, Question question) {
		final int x = random.nextInt(2);
		System.out.printf("AgentStudent:       question=%-3d answer=%d%n", question.id, x);
		return x;
	}
}
