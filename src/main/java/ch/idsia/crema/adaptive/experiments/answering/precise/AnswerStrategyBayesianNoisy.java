package ch.idsia.crema.adaptive.experiments.answering.precise;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.answering.AnswerStrategy;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.bp.BeliefPropagation;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;

import java.util.Random;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    09.02.2021 10:56
 */
public class AnswerStrategyBayesianNoisy implements AnswerStrategy<BayesianFactor> {

	private BeliefPropagation<BayesianFactor> inference;

	private final Random random;
	private final double stdDev;
	private final double mean;

	public AnswerStrategyBayesianNoisy(long seed, double stdDev, double mean) {
		this.random = new Random(seed);
		this.stdDev = stdDev;
		this.mean = mean;
	}

	@Override
	public int answer(DAGModel<BayesianFactor> model, Question question, TIntIntMap skills) {
		if (inference == null)
			inference = new BeliefPropagation<>(model);

		final BayesianFactor PQ = inference.query(question.variable, skills);
		final double Pq0 = PQ.getValue(0);
		final double x = random.nextDouble() * stdDev + mean;

		if (x < Pq0)
			return 0;
		return 1;
	}

}
