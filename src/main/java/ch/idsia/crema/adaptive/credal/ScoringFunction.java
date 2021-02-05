package ch.idsia.crema.adaptive.credal;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    05.02.2021 15:33
 */
public interface ScoringFunction {

	/**
	 * @param model        model to work on
	 * @param skill        skill variable in the model
	 * @param question     question variable in the model
	 * @param observations evidence
	 * @return the score associated with the given question
	 * @throws Exception if something goes wrong
	 */
	double score(DAGModel<? extends GenericFactor> model, int skill, int question, TIntIntMap observations) throws Exception;

}
