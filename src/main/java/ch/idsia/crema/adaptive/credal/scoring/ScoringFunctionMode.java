package ch.idsia.crema.adaptive.credal.scoring;

import ch.idsia.crema.adaptive.credal.Question;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    08.02.2021 15:23
 */
public class ScoringFunctionMode implements ScoringFunction<IntervalFactor> {
	@Override
	public double score(DAGModel<IntervalFactor> model, Question question, TIntIntMap observations) throws Exception {
		// TODO
		return 0;
	}
}
