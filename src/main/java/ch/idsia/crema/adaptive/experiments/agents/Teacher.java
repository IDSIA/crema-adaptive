package ch.idsia.crema.adaptive.experiments.agents;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.Skill;
import ch.idsia.crema.adaptive.experiments.model.AbstractModelBuilder;
import ch.idsia.crema.adaptive.experiments.scoring.ScoringFunction;
import ch.idsia.crema.adaptive.experiments.stopping.StoppingCondition;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.*;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    04.02.2021 17:25
 */
public class Teacher<F extends GenericFactor> implements AgentTeacher {

	/**
	 * Object that is used to generate the model and store information like the questions of the survey and the skills.
	 */
	private final AbstractModelBuilder<F> builder;
	/**
	 * Model used to perform inferences.
	 */
	private final DAGModel<F> model;

	/**
	 * Answers from a {@link Student}.
	 */
	private final TIntIntMap observations = new TIntIntHashMap();

	/**
	 * List of all the questions.
	 */
	private final List<Question> questions;
	/**
	 * List of all the question that have an answer.
	 */
	private final List<Question> questionsDone = new ArrayList<>();

	/**
	 * Function used to give a score in the {@link #next()} method.
	 */
	private final ScoringFunction<F> scoringFunction;
	/**
	 * List of conditions tested in the {@link #stop()} method.
	 */
	private final List<StoppingCondition<Teacher<F>>> stoppingConditions;

	/**
	 * @param builder            used to generate the model and store information
	 * @param scoringFunction    search strategy for the next question
	 * @param stoppingConditions multiple stopping criteria
	 */
	@SafeVarargs
	public Teacher(
			AbstractModelBuilder<F> builder,
			ScoringFunction<F> scoringFunction,
			StoppingCondition<Teacher<F>>... stoppingConditions
	) {
		this.builder = builder;
		model = builder.getModel();

		this.scoringFunction = scoringFunction;
		this.stoppingConditions = Arrays.asList(stoppingConditions);

		this.questions = builder.questions;
	}

	@Override
	public int getNumberQuestionsDone() {
		return questionsDone.size();
	}

	public DAGModel<F> getModel() {
		return model;
	}

	public TIntIntMap getObservations() {
		return observations;
	}

	public List<Skill> getSkills() {
		return builder.skills;
	}

	/**
	 * @param question should be inside (5, 104)
	 * @param answer   should be 0 or 1
	 */
	@Override
	public void check(Question question, int answer) {
		observations.put(question.variable, answer);
		questionsDone.add(question);
	}

	/**
	 * @return return true if we need to stop, otherwise false
	 * @throws Exception if inference goes wrong
	 */
	@Override
	public boolean stop() throws Exception {
		for (StoppingCondition<Teacher<F>> condition : stoppingConditions) {
			if (condition.stop(this))
				return true;
		}
		return false;
	}

	/**
	 * @return the next question based on the defined {@link #scoringFunction}, null if nothing is found
	 * @throws Exception if inference goes wrong
	 */
	@Override
	public Question next() throws Exception {
		double maxIG = 0.;
		Question nextQuestion = null;

		Set<Integer> templates = new HashSet<>();

		// for each question
		for (Question question : questions) {
			if (questionsDone.contains(question))
				continue; // question already done, skip

			if (question.template > 0 && templates.contains(question.template))
				continue; // template already checked, skip

			templates.add(question.template);

			final double score = scoringFunction.score(model, question, observations);

			if (score > maxIG) {
				maxIG = score;
				nextQuestion = question;
			}
		}

		if (nextQuestion == null)
			System.out.printf("AgentTeacher:       no question found%n");
		else
			System.out.printf("AgentTeacher:       next question=%-3d%n", nextQuestion.id);

		return nextQuestion;
	}

}
