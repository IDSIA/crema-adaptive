package ch.idsia.crema.adaptive.experiments.agents;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.answering.AnswerStrategy;
import ch.idsia.crema.adaptive.experiments.model.AbstractModelBuilder;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    08.02.2021 15:58
 */
public class Student<F extends GenericFactor> implements AgentStudent {

	/**
	 * Unique id of the student
	 */
	final int id;

	/**
	 * Object that is used to generate the model and store information like the questions of the survey and the skills.
	 */
	final AbstractModelBuilder<F> builder;
	/**
	 * Model used to perform inferences.
	 */
	final DAGModel<F> model;

	/**
	 * Which kind of strategy will be used to answer. This can be set to null if the method
	 * {@link #setAnswers(TIntIntMap)} is used.
	 */
	final AnswerStrategy<Student<F>> answerStrategy;

	/**
	 * Internal states of the skills relative to this student.
	 */
	final TIntIntMap skills = new TIntIntHashMap();
	/**
	 * Internal map of already available answers.
	 */
	final TIntIntMap answers = new TIntIntHashMap();

	// TODO: generator of students

	/**
	 * @param id       unique identifier of this student
	 * @param builder  used to generate the model and store model related information
	 * @param strategy which kind of strategy will be used to answer
	 */
	public Student(int id, AbstractModelBuilder<F> builder, AnswerStrategy<Student<F>> strategy) {
		this.id = id;
		this.builder = builder;
		this.model = builder.getModel();
		this.answerStrategy = strategy;
	}

	/**
	 * @param id      unique identifier of this student
	 * @param answers map of already available answers where the key is the unique id of a {@link Question}, value is
	 *                the answer
	 */
	public Student(int id, TIntIntMap answers) {
		this.id = id;
		this.builder = null;
		this.model = null;
		this.answerStrategy = null;
		setAnswers(answers);
	}

	@Override
	public int getId() {
		return id;
	}

	public DAGModel<F> getModel() {
		return model;
	}

	public TIntIntMap getSkills() {
		return skills;
	}

	/**
	 * Set a precise state for the given skill. If the {@link AnswerStrategy} supports it, all the answers will
	 * be based ont these set of skills. Set the skill before calling the {@link #generateAnswers()} method to
	 * obtain a valid answer list.
	 *
	 * @param skill variable index for the skill
	 * @param state set this state
	 */
	public Student<F> setSkill(int skill, int state) {
		skills.put(skill, state);
		return this;
	}

	/**
	 * An alternative method to {@link #generateAnswers()} that use a pre-loaded {@link TIntIntMap} to fill the internal
	 * answer collection.
	 *
	 * @param answers a map where the key is the id of a {@link Question} and the value is the state of the answer.
	 */
	public Student<F> setAnswers(TIntIntMap answers) {
		this.answers.putAll(answers);
		return this;
	}

	/**
	 * Generate a list of answers to all questions using the stored {@link #model}, {@link #answerStrategy}, and
	 * {@link #skills}. Remember to fill the skills with {@link #setSkill(int, int)} before use this method.
	 * <p>
	 * As an alternative, it is possible to use the {@link #setAnswers(TIntIntMap)} method.
	 */
	public Student<F> generateAnswers() {
		if (answerStrategy == null)
			throw new IllegalStateException("No answer strategy defined.");

		for (Question question : builder.questions) {
			answers.put(question.variable, answerStrategy.answer(this, question));
		}
		return this;
	}

	/**
	 * This method will return the saved answer if the field {@link #answers} already contains an answer for the given
	 * {@link Question}, otherwise it will use the {@link #answerStrategy} object to generate a new one. In order to
	 * pre-load all the possible answers, use the {@link #generateAnswers()} method.
	 *
	 * @param question the question to answer to
	 * @return 0 or 1 based on the question
	 */
	@Override
	public int answer(Question question) {
		if (answers.containsKey(question.variable))
			return answers.get(question.variable);

		return answerStrategy.answer(this, question);
	}

	@Override
	public String getAnswers(int numQuestions) {
		int [] answersList = Arrays.copyOfRange(Arrays.stream(answers.values()).toArray(), 0, numQuestions);
		return StringUtils.join(answersList, ',');
	}

	@Override
	public String getProfiles(int numQuestions) {
		int [] profilesList = Arrays.copyOfRange(Arrays.stream(answers.values()).toArray(), numQuestions, answers.values().length);
		return StringUtils.join(profilesList, ',');
	}
}
