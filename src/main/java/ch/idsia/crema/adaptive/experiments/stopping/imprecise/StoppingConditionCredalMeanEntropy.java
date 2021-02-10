package ch.idsia.crema.adaptive.experiments.stopping.imprecise;

import ch.idsia.crema.adaptive.experiments.Utils;
import ch.idsia.crema.adaptive.experiments.stopping.StoppingConditionModel;
import ch.idsia.crema.entropy.AbellanEntropy;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.inference.approxlp2.ApproxLP2;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.list.TIntList;
import gnu.trove.map.TIntIntMap;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    05.02.2021 15:51
 */
public class StoppingConditionCredalMeanEntropy implements StoppingConditionModel<IntervalFactor> {

	private final AbellanEntropy entropy = new AbellanEntropy();
	private final ApproxLP2 approx = new ApproxLP2();

	private final double threshold;

	public StoppingConditionCredalMeanEntropy(double threshold) {
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
	public boolean stop(DAGModel<IntervalFactor> model, TIntList skills, TIntIntMap observations) throws Exception {
		double mean = 0;

		for (int s = 0; s < skills.size(); s++) {
			int skill = skills.get(s);
			final IntervalFactor res = approx.query(model, skill, observations);

			// compute entropy of the current skill
			final double[] PS = entropy.getMaxEntropy(res.getLower(), res.getUpper());
			final double HS = Utils.H(PS);

			mean += HS / model.getSize(skill);
		}

		return mean < threshold;
	}
}
