package ch.idsia.crema.adaptive.experiments.stopping;

import ch.idsia.crema.adaptive.experiments.Skill;
import ch.idsia.crema.adaptive.experiments.agents.Teacher;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;

import java.util.List;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    05.02.2021 15:31
 */
public interface StoppingConditionModel<F extends GenericFactor> extends StoppingCondition<Teacher<F>> {

	/**
	 * @param model        model to work on
	 * @param skills       list of skill variables in the model
	 * @param observations evidence
	 * @return true if stop condition is reached, otherwise false
	 * @throws Exception if something goes wrong
	 */
	boolean stop(DAGModel<F> model, List<Skill> skills, TIntIntMap observations) throws Exception;

	/**
	 * The default implementation is a wrapper of the {@link #stop(DAGModel, List, TIntIntMap)} method;
	 *
	 * @param teacher who need to check for the stop
	 * @return true if stop condition is reached, otherwise false
	 * @throws Exception if something goes wrong
	 */
	@Override
	default boolean stop(Teacher<F> teacher) throws Exception {
		return stop(teacher.getModel(), teacher.getSkills(), teacher.getObservations());
	}
}
