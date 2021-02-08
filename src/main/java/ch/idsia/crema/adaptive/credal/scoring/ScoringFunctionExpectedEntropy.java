package ch.idsia.crema.adaptive.credal.scoring;

import ch.idsia.crema.adaptive.credal.Question;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    08.02.2021 15:23
 */
public class ScoringFunctionExpectedEntropy implements ScoringFunction<BayesianFactor> {
	@Override
	public double score(DAGModel<BayesianFactor> model, Question question, TIntIntMap observations) throws Exception {
		// TODO
		return 0;
	}
}
