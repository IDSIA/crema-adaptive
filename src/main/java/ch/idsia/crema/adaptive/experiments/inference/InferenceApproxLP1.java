package ch.idsia.crema.adaptive.experiments.inference;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.inference.approxlp.ApproxLP1;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.model.graphical.MixedModel;
import ch.idsia.crema.preprocess.BinarizeEvidence;
import ch.idsia.crema.preprocess.RemoveBarren;
import gnu.trove.map.TIntIntMap;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    05.03.2021 15:11
 */
public class InferenceApproxLP1 implements InferenceEngine {

	@Override
	public IntervalFactor query(DAGModel<IntervalFactor> original, TIntIntMap obs, int variable) throws InterruptedException {
		DAGModel<IntervalFactor> model = original.copy();

		if (obs.isEmpty()) {
			// remove barren nodes
			final RemoveBarren<IntervalFactor> rb = new RemoveBarren<>();
			rb.executeInPlace(model, obs, variable);

			// ApproxLP1
			final ApproxLP1<IntervalFactor> approx = new ApproxLP1<>();
			return approx.query(model, variable);
		} else {
			// remove barren nodes
			final RemoveBarren<IntervalFactor> rb = new RemoveBarren<>();
			rb.executeInPlace(model, obs, variable);

			final CutObserved co = new CutObserved();
			co.executeInPlace(model, obs);

			final MergeObserved mo = new MergeObserved();
			model = mo.execute(model, obs);

			// binarize evidence
			final BinarizeEvidence<IntervalFactor> be = new BinarizeEvidence<>(obs.size());
			final MixedModel mixedModel = be.execute(model, obs);
			final int evidence = be.getEvidenceNode();

			// ApproxLP1
			final ApproxLP1<GenericFactor> approx = new ApproxLP1<>();
			approx.setEvidenceNode(evidence);
			return approx.query(mixedModel, variable);
		}
	}

}
