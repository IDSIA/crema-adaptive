package ch.idsia.crema.adaptive.experiments.inference;

import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    05.03.2021 15:34
 */
public interface InferenceEngine {

	IntervalFactor query(DAGModel<IntervalFactor> model, TIntIntMap obs, int variable) throws InterruptedException;

}
