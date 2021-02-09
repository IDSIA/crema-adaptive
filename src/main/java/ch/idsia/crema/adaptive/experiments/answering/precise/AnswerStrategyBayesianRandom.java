package ch.idsia.crema.adaptive.experiments.answering.precise;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.answering.AnswerStrategy;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;

import java.util.Random;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    08.02.2021 18:09
 */
public class AnswerStrategyBayesianRandom implements AnswerStrategy<BayesianFactor> {

	final Random random;

	public AnswerStrategyBayesianRandom(long seed) {
		random = new Random(seed);
	}

	@Override
	public int answer(DAGModel<BayesianFactor> model, Question question, TIntIntMap skills) {
		final int x = random.nextInt(2);
		System.out.printf("AgentStudent:       question=%-3d answer=%d%n", question.id, x);
		return x;
	}
}
