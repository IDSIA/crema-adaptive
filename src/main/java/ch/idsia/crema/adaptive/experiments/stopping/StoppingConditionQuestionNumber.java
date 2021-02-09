package ch.idsia.crema.adaptive.experiments.stopping;

import ch.idsia.crema.adaptive.experiments.agents.AgentTeacher;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    09.02.2021 09:57
 */
public class StoppingConditionQuestionNumber<T extends AgentTeacher> implements StoppingCondition<T> {

	private final int number;

	/**
	 * @param number maximum amount of question to pose (inclusive)
	 */
	public StoppingConditionQuestionNumber(int number) {
		this.number = number;
	}

	@Override
	public boolean stop(T teacher) throws Exception {
		return teacher.getNumberQuestionsDone() >= number;
	}
}
