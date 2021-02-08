package ch.idsia.crema.adaptive.credal.scoring;

import ch.idsia.crema.adaptive.credal.Question;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    05.02.2021 15:33
 */
public interface ScoringFunction<F extends GenericFactor> {

	/**
	 * @param model        model to work on
	 * @param question     question to evaluate
	 * @param observations evidence
	 * @return the score associated with the given question
	 * @throws Exception if something goes wrong
	 */
	double score(DAGModel<F> model, Question question, TIntIntMap observations) throws Exception;

}
