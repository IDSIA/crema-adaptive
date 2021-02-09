package ch.idsia.crema.adaptive.experiments.stopping.precise;

import ch.idsia.crema.adaptive.experiments.Skill;
import ch.idsia.crema.adaptive.experiments.stopping.StoppingConditionModel;
import ch.idsia.crema.entropy.BayesianEntropy;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.bp.BeliefPropagation;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;

import java.util.List;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    05.02.2021 15:51
 */
public class StoppingConditionBayesianMeanEntropy implements StoppingConditionModel<BayesianFactor> {

	private BeliefPropagation<BayesianFactor> inference;

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
	public boolean stop(DAGModel<BayesianFactor> model, List<Skill> skills, TIntIntMap observations) throws Exception {
		if (inference == null)
			inference = new BeliefPropagation<>(model);

		double mean = 0;

		for (Skill skill : skills) {
			final int s = skill.variable;
			final BayesianFactor PS = inference.query(s, observations);

			// compute entropy of the current skill
			final double HS = BayesianEntropy.H(PS);

			mean += HS / model.getSize(s);
		}

		System.out.printf("Stopping condition: H=%.4f th=%.4f%n", mean, threshold);

		return mean < threshold;
	}
}
