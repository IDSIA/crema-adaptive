package ch.idsia.crema.adaptive.experiments.stopping;

import ch.idsia.crema.adaptive.experiments.agents.AgentTeacher;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    05.02.2021 15:31
 */
public interface StoppingCondition<F extends AgentTeacher> {

	/**
	 * @param teacher who need to check for the stop
	 * @return true if stop condition is reached, otherwise false
	 * @throws Exception if something goes wrong
	 */
	boolean stop(F teacher) throws Exception;

}
