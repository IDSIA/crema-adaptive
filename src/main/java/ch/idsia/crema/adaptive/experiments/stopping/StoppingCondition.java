package ch.idsia.crema.adaptive.experiments.stopping;

import ch.idsia.crema.adaptive.experiments.Skill;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;

import java.util.List;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    05.02.2021 15:31
 */
public interface StoppingCondition<F extends GenericFactor> {

	/**
	 * @param model        model to work on
	 * @param skills       list of skill variables in the model
	 * @param observations evidence
	 * @return true if stop condition is reached, otherwise false
	 * @throws Exception if something goes wrong
	 */
	boolean stop(DAGModel<F> model, List<Skill> skills, TIntIntMap observations) throws Exception;

}
