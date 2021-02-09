package ch.idsia.crema.adaptive.experiments.scoring.precise;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.scoring.ScoringFunction;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.bp.BeliefPropagation;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    08.02.2021 15:23
 */
public class ScoringFunctionMode implements ScoringFunction<BayesianFactor> {

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

			final double[] data = bf.getData();
			final double mode = argmax(data);

			// TODO: track when mode have the same index, then raise warning

			HSQ += mode * Pqi;
		}

		return HSQ / 2;
	}

	// TODO: move to utils, add mode()
	int argmax(double[] data) {
		double v = data[0];
		int maxI = 0;

		for (int i = 1; i < data.length; i++) {
			if (data[i] > v) {
				v = data[i];
				maxI = i;
			}
		}

		return maxI;
	}
}
