package ch.idsia.crema.adaptive.experiments.answering.precise;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.answering.AnswerStrategyModel;
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
public class AnswerStrategyBayesianNoisy implements AnswerStrategyModel<BayesianFactor> {

	private BeliefPropagation<BayesianFactor> inference;

	private final Random random;
	private final double stdDev;
	private final double mean;

	public AnswerStrategyBayesianNoisy(long seed, double stdDev, double mean) {
		this.random = new Random(seed);
		this.stdDev = stdDev;
		this.mean = mean;
	}

	/**
	 * @param model    structure model
	 * @param question from a {@link ch.idsia.crema.adaptive.experiments.agents.Teacher}
	 * @param skills   evidence map on the skills wher the key is the skill variable in the model and the value is its
	 *                 real state
	 * @return an answer according to the {@link ch.idsia.crema.adaptive.experiments.agents.Student}'s bayesian model
	 * and its skills with some noise added.
	 */
	@Override
	public int answer(DAGModel<BayesianFactor> model, Question question, TIntIntMap skills) {
		if (inference == null)
			inference = new BeliefPropagation<>();

		// TODO: check
		final BayesianFactor PQ = inference.query(model, skills, question.variable);
		final double Pq0 = PQ.getValue(0);
		final double x = Math.max(0., Math.min(1., random.nextDouble() * stdDev + mean));

		if (x < Pq0)
			return 0;
		return 1;
	}

}
