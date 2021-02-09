package ch.idsia.crema.adaptive.experiments;

import ch.idsia.crema.adaptive.experiments.model.AbstractModelBuilder;
import ch.idsia.crema.adaptive.experiments.scoring.ScoringFunction;
import ch.idsia.crema.adaptive.experiments.stopping.StoppingCondition;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    04.02.2021 17:25
 */
public class AgentTeacher<F extends GenericFactor> {

	private final AbstractModelBuilder<F> builder;
	private final DAGModel<F> model;

	private final TIntIntMap observations = new TIntIntHashMap();

	private final List<Question> questions;
	private final List<Question> questionsDone = new ArrayList<>();

	private final ScoringFunction<F> scoringFunction;
	private final StoppingCondition<F> stoppingCondition;

	public AgentTeacher(
			AbstractModelBuilder<F> builder,
			ScoringFunction<F> scoringFunction,
			StoppingCondition<F> stoppingCondition
	) {
		this.builder = builder;

		this.scoringFunction = scoringFunction;
		this.stoppingCondition = stoppingCondition;

		// model is defined there
		model = builder.getModel();
		this.questions = builder.questions;
	}

	public int getNumberQuestionsDone() {
		return questionsDone.size();
	}

	/**
	 * @param question should be inside (5, 104)
	 * @param answer   should be 0 or 1
	 */
	public void answer(Question question, int answer) {
		observations.put(question.variable, answer);
		questionsDone.add(question);
	}

	/**
	 * @return return true if we need to stop, otherwise false
	 * @throws Exception if inference goes wrong
	 */
	public boolean stop() throws Exception {
		return stoppingCondition.stop(model, builder.skills, observations);
	}

	/**
	 * @return the next question based on an entropy analysis, -1 if nothing is found
	 * @throws Exception if inference goes wrong
	 */
	public Question next() throws Exception {
		double maxIG = 0.;
		Question nextQuestion = null;

		Set<Integer> templates = new HashSet<>();

		// for each question
		for (Question question : questions) {
			if (questionsDone.contains(question))
				continue; // question already done, skip

			if (question.template > 0 && templates.contains(question.template))
				continue; // template already done, skip

			templates.add(question.template);

			final double score = scoringFunction.score(model, question, observations);

			if (score > maxIG) {
				maxIG = score;
				nextQuestion = question;
			}
		}

		if (nextQuestion == null)
			System.out.println("AgentTeacher:       no question found");
		else
			System.out.printf("AgentTeacher:       next question=%-3d%n", nextQuestion.id);

		return nextQuestion;
	}

}
