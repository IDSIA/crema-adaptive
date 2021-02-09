package ch.idsia.crema.adaptive.experiments.agents;

import ch.idsia.crema.adaptive.experiments.Question;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    09.02.2021 08:55
 */
public interface AgentTeacher {

	int getNumberQuestionsDone();

	void check(Question question, int answer);

	boolean stop() throws Exception;

	Question next() throws Exception;

}
