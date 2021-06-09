package ch.idsia.crema.adaptive.experiments.scoring.precise;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.scoring.ScoringFunction;
import ch.idsia.crema.entropy.BayesianEntropy;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.bp.BeliefPropagation;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;


/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    05.02.2021 15:49
 */
public class ScoringFunctionExpectedEntropy implements ScoringFunction<BayesianFactor> {

	private final BeliefPropagation<BayesianFactor> inference = new BeliefPropagation<>();

	/**
	 * A {@link ScoringFunction} based on the expected mean entropy change from the answer.
	 *
	 * @param model        model to work on
	 * @param question     question to evaluate
	 * @param observations evidence
	 * @return a score between 0 and 1
	 * @throws Exception if something bad happens
	 */
	@Override
	public double score(DAGModel<BayesianFactor> model, Question question, TIntIntMap observations) throws Exception {
		final BayesianFactor PQ = inference.query(model, observations, question.variable);
		final BayesianFactor PS = inference.query(model, observations, question.skill);
		final double HS = BayesianEntropy.H(PS);

		double HSQ = 0;
		for (int a = 0; a < 2; a++) {
			TIntIntMap obs = new TIntIntHashMap(observations);
			obs.put(question.variable, a);

			final BayesianFactor bf = inference.query(model, obs, question.skill);
			final double Pqi = PQ.getValue(a);
			final double HSq = BayesianEntropy.H(bf);

			HSQ += HSq * Pqi;
		}

		return HS - HSQ;
	}
}
