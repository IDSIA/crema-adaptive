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

	private BeliefPropagation<BayesianFactor> inference;

	@Override
	public double score(DAGModel<BayesianFactor> model, Question question, TIntIntMap observations) throws Exception {
		if (inference == null)
			inference = new BeliefPropagation<>(model);

		final BayesianFactor PQ = inference.query(question.skill, observations);

		// compute... something similar to a information gain
		double HSQ = 0;
		for (int a = 0; a < 2; a++) {
			TIntIntMap obs = new TIntIntHashMap(observations);
			obs.put(question.variable, a);

			final BayesianFactor bf = inference.query(question.skill, obs);
			final double Pqi = PQ.getValue(a);
			final double HSq = BayesianEntropy.H(bf);

			HSQ += HSq * Pqi;
		}

		return HSQ / 2;
	}
}
