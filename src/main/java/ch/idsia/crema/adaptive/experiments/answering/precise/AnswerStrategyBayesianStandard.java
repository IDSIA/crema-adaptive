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
 * Date:    09.02.2021 10:56
 */
public class AnswerStrategyBayesianStandard implements AnswerStrategy<BayesianFactor> {

	private final Random random;

	public AnswerStrategyBayesianStandard(long seed) {
		this.random = new Random(seed);
	}

	@Override
	public int answer(DAGModel<BayesianFactor> model, Question question, TIntIntMap skills) {
		final int s = skills.get(question.skill);

		// TODO: check
		final BayesianFactor PQ = model.getFactor(question.variable);
		final double Pq0 = PQ.getValueAt(s);

		final double x = random.nextDouble();

		if (x < Pq0)
			return 0;
		return 1;
	}
}
