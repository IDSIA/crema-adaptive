package ch.idsia.crema.adaptive.experiments.agents;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.Skill;
import ch.idsia.crema.adaptive.experiments.answering.AnswerStrategy;
import ch.idsia.crema.adaptive.experiments.model.AbstractModelBuilder;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    08.02.2021 15:58
 */
public class Student<F extends GenericFactor> implements AgentStudent {

	final int id;

	final AbstractModelBuilder<F> builder;
	final DAGModel<F> model;

	final AnswerStrategy<F> answerStrategy;

	final TIntIntMap states = new TIntIntHashMap();
	final TIntIntMap answers = new TIntIntHashMap();

	// TODO: observations on skills as a sample

	public Student(int id, AbstractModelBuilder<F> builder, AnswerStrategy<F> strategy) {
		this.id = id;
		this.builder = builder;
		this.model = builder.getModel();
		this.answerStrategy = strategy;
	}

	/**
	 * Set a precise state for the given {@link Skill}. If the {@link AnswerStrategy} supports it, all the answers will
	 * be based ont these set of skills. Set the skill before calling the {@link #generateAnswers(int, int)} method to
	 * obtain a valid answer list.
	 *
	 * @param skill for this skill
	 * @param state set this state
	 */
	public Student<F> setSkill(Skill skill, int state) {
		states.put(skill.variable, state);
		return this;
	}

	/**
	 * Generate a list of answers to all questions using the stored {@link #model}, {@link #answerStrategy}, and
	 * {@link #states}.
	 */
	public void generateAnswers(int skill, int state) {
		for (Question question : builder.questions) {
			answers.put(question.id, answerStrategy.answer(model, question, states));
		}
	}

	/**
	 * Generates an answer for the given question.
	 *
	 * @param question the question to answer to
	 * @return 0 or 1 based on the question
	 */
	@Override
	public int answer(Question question) {
		if (answers.containsKey(question.id))
			return answers.get(question.id);

		return answerStrategy.answer(model, question, states);
	}

}
