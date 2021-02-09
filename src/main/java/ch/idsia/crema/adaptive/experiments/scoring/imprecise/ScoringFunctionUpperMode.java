package ch.idsia.crema.adaptive.experiments.scoring.imprecise;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.scoring.ScoringFunction;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;
import org.apache.commons.lang3.NotImplementedException;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    08.02.2021 15:23
 */
public class ScoringFunctionUpperMode implements ScoringFunction<IntervalFactor> {

	@Override
	public double score(DAGModel<IntervalFactor> model, Question question, TIntIntMap observations) throws Exception {
		// TODO
		throw new NotImplementedException("Not yet implemented.");
	}
}
