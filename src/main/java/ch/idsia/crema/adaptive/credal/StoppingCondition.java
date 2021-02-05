package ch.idsia.crema.adaptive.credal;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    05.02.2021 15:31
 */
public interface StoppingCondition {

	/**
	 * @param model        model to work on
	 * @param skills       list of skill variables in the model
	 * @param observations evidence
	 * @return true if stop condition is reached, otherwise false
	 * @throws Exception if something goes wrong
	 */
	boolean stop(DAGModel<? extends GenericFactor> model, int[] skills, TIntIntMap observations) throws Exception;

}
