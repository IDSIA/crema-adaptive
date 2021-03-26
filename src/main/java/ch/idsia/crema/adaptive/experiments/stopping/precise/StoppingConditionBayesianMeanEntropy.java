package ch.idsia.crema.adaptive.experiments.stopping.precise;

import ch.idsia.crema.adaptive.experiments.stopping.StoppingConditionModel;
import ch.idsia.crema.entropy.BayesianEntropy;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.bp.BeliefPropagation;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.list.TIntList;
import gnu.trove.map.TIntIntMap;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    05.02.2021 15:51
 */
public class StoppingConditionBayesianMeanEntropy implements StoppingConditionModel<BayesianFactor> {

	private final BeliefPropagation<BayesianFactor> inference = new BeliefPropagation<>();

	private final double threshold;

	public StoppingConditionBayesianMeanEntropy(double threshold) {
		this.threshold = threshold;
	}

	/**
	 * Check if the mean entropy value of the skills is below the given threshold or not.
	 *
	 * @param model        model to work on
	 * @param skills       list of skill variables in the model
	 * @param observations evidence
	 * @return true if stop condition is reached, otherwise false
	 * @throws Exception if inference goes wrong
	 */
	@Override
	public boolean stop(DAGModel<BayesianFactor> model, TIntList skills, TIntIntMap observations) throws Exception {
		double mean = 0;

		for (int s = 0; s < skills.size(); s++) {
			int skill = skills.get(s);
			final BayesianFactor PS = inference.query(model, observations, skill);

			// compute entropy of the current skill
			final double HS = BayesianEntropy.H(PS);

			mean += HS / model.getSize(skill);
		}

//		System.out.printf("Stopping condition: H=%.4f th=%.4f%n", mean, threshold);

		return mean < threshold;
	}
}
