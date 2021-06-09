package ch.idsia.crema.adaptive.experiments.inference;

import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.inference.approxlp2.ApproxLP2;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    05.03.2021 15:11
 */
public class InferenceApproxLP2 implements InferenceEngine {

	private final ApproxLP2<IntervalFactor> approx = new ApproxLP2<>();

	@Override
	public IntervalFactor query(DAGModel<IntervalFactor> model, TIntIntMap obs, int variable) throws InterruptedException {
		return approx.query(model, obs, variable);
	}

}
