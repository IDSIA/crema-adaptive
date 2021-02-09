package ch.idsia.crema.adaptive.experiments.answering;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.agents.AgentStudent;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    08.02.2021 18:08
 */
public interface AnswerStrategy<F extends AgentStudent> {

	/**
	 * @param student  who need to give the answer
	 * @param question chosen by a {@link ch.idsia.crema.adaptive.experiments.agents.Teacher}
	 * @return the answer found
	 */
	int answer(F student, Question question);

}
