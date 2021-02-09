package ch.idsia.crema.adaptive.experiments.stopping;

import ch.idsia.crema.adaptive.experiments.Skill;
import ch.idsia.crema.adaptive.experiments.Utils;
import ch.idsia.crema.entropy.AbellanEntropy;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.inference.approxlp2.ApproxLP2;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;

import java.util.List;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    05.02.2021 15:51
 */
public class StoppingConditionCredalMeanEntropy implements StoppingCondition<IntervalFactor> {

	private final AbellanEntropy entropy = new AbellanEntropy();
	private final ApproxLP2 approx = new ApproxLP2();

	private final double threshold;

	public StoppingConditionCredalMeanEntropy(double threshold) {
		this.threshold = threshold;
	}

	@Override
	public boolean stop(DAGModel<IntervalFactor> model, List<Skill> skills, TIntIntMap observations) throws Exception {
		double mean = 0;

		for (Skill skill : skills) {
			final int s = skill.variable;
			final IntervalFactor res = approx.query(model, s, observations);

			// compute entropy of the current skill
			final double[] PS = entropy.getMaxEntropy(res.getLower(), res.getUpper());
			final double HS = Utils.H(PS);

			mean += HS / model.getSize(s);
		}

		return mean < threshold;
	}
}
