package ch.idsia.crema.adaptive.experiments.scoring.precise;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.scoring.ScoringFunction;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.bp.BeliefPropagation;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    08.02.2021 15:23
 */
public class ScoringFunctionProbabilityOfRight implements ScoringFunction<BayesianFactor> {

	private BeliefPropagation<BayesianFactor> inference;

	@Override
	public double score(DAGModel<BayesianFactor> model, Question question, TIntIntMap observations) throws Exception {
		if (inference == null)
			inference = new BeliefPropagation<>(model);

		final BayesianFactor PQ = inference.query(question.skill, observations);

		return 1. - Math.abs(PQ.getValueAt(0) - 0.5);
	}
}
