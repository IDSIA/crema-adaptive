package ch.idsia.crema.adaptive.experiments.agents;

import ch.idsia.crema.adaptive.experiments.Question;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    09.02.2021 08:55
 */
public interface AgentStudent {

	/**
	 * @return id of the student
	 */
	int getId();

	/**
	 * @param question a {@link Teacher}'s question
	 * @return a value that represent the state of the answer
	 */
	int answer(Question question);

}
