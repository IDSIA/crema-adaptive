package ch.idsia.crema.adaptive.experiments.agents;

import ch.idsia.crema.adaptive.experiments.Question;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    09.02.2021 08:55
 */
public interface AgentTeacher {

	int getNumberQuestionsDone();
	int getTotalNumberQuestions();
	/**
	 * Checks the answer to the given question.
	 *
	 * @param question {@link Teacher}'s question
	 * @param answer   {@link Student}'s answer
	 */
	void check(Question question, int answer);

	/**
	 * @return true if one the {@link ch.idsia.crema.adaptive.experiments.stopping.StoppingCondition} is meet, otherwise false.
	 * @throws Exception it something really bad happens...
	 */
	boolean stop() throws Exception;

	/**
	 * @return the next {@link Question} found using the given {@link ch.idsia.crema.adaptive.experiments.scoring.ScoringFunction}.
	 * @throws Exception is something not so good happens...
	 */
	Question next() throws Exception;

	/**
	 * @return a comma separated row of all the posterior probability; these probabilities are collected each time a
	 * {@link ch.idsia.crema.adaptive.experiments.stopping.StoppingCondition} is called (so we have nQuestion+1 outputs)
	 */
	String getResults();
}
