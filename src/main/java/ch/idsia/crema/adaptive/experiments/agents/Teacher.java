package ch.idsia.crema.adaptive.experiments.agents;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.model.AbstractModelBuilder;
import ch.idsia.crema.adaptive.experiments.persistence.Output;
import ch.idsia.crema.adaptive.experiments.persistence.Persist;
import ch.idsia.crema.adaptive.experiments.persistence.Progress;
import ch.idsia.crema.adaptive.experiments.scoring.ScoringFunction;
import ch.idsia.crema.adaptive.experiments.stopping.StoppingCondition;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.list.TIntList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.*;
import java.util.stream.Collectors;

import static ch.idsia.crema.adaptive.experiments.Utils.separator;

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

	private Persist<F> persist;

	private final List<Progress<F>> progress = new ArrayList<>();

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
		this.model = builder.getModel();

		this.scoringFunction = scoringFunction;
		this.stoppingConditions = Arrays.asList(stoppingConditions);

		this.questions = builder.questions;

		this.progress.add(new Progress<>(-1, new Question(-1, -1, -1))); // this is just the staring point
	}

	public Teacher<F> setPersist(Persist<F> persist) {
		this.persist = persist;
		return this;
	}

	@Override
	public int getNumberQuestionsDone() {
		return questionsDone.size();
	}

	@Override
	public int getTotalNumberQuestions() {
		return questions.size();
	}

	public DAGModel<F> getModel() {
		return model;
	}

	public TIntIntMap getObservations() {
		return observations;
	}

	public TIntList getSkills() {
		return builder.skills;
	}

	/**
	 * @param question should be inside (5, 104)
	 * @param answer   should be 0 or 1
	 */
	@Override
	public void check(Question question, int answer) throws Exception {
		observations.put(question.variable, answer);
		questionsDone.add(question);

		progress.get(this.progress.size() - 1).setAnswer(answer);
	}

	/**
	 * @return return true if we need to stop, otherwise false
	 * @throws Exception if inference goes wrong
	 */
	@Override
	public boolean stop() throws Exception {
		if (persist != null)
			progress.get(progress.size() - 1).setOutput(persist.register(this));

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
		double maxIG = Double.NEGATIVE_INFINITY;
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

//			System.out.printf("%3d %3d %3d %.4f %n", question.skill, question.template, question.variable, score);

			if (score > maxIG) {
				maxIG = score;
				nextQuestion = question;
			}
		}

		progress.add(new Progress<>(maxIG, nextQuestion));

		return nextQuestion;
	}

	@Override
	public String getResults() {
		return progress.stream()
				.map(Progress::getOutput)
				.filter(Objects::nonNull)
				.map(Output::serialize)
				.collect(Collectors.joining(separator));
	}

	@Override
	public List<String> getProgress(int id) {
		return progress.stream()
				.filter(x -> x.getOutput() != null)
				.map(x -> x.serialize(id))
				.collect(Collectors.toList());
	}
}
