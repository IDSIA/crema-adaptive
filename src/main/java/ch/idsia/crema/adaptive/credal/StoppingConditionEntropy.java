package ch.idsia.crema.adaptive.credal;

import ch.idsia.crema.entropy.AbellanEntropy;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.inference.approxlp2.ApproxLP2;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    05.02.2021 15:51
 */
public class StoppingConditionEntropy implements StoppingCondition {

	private final AbellanEntropy entropy = new AbellanEntropy();
	private final ApproxLP2 approx = new ApproxLP2();

	@Override
	public boolean stop(DAGModel<? extends GenericFactor> model, int[] skills, TIntIntMap observations) throws Exception {
		double mean = 0;

		for (int skill : skills) {
			final IntervalFactor res = approx.query(model, skill, observations);

			// compute entropy of the current skill
			final double[] PS = entropy.getMaxEntropy(res.getLower(), res.getUpper());
			final double HS = Utils.H(PS);

			mean += HS / 4;
		}

		return mean < 0.2;
	}
}
