package ch.idsia.crema.adaptive.experiments.agents;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.answering.AnswerStrategy;
import ch.idsia.crema.adaptive.experiments.model.AbstractModelBuilder;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.DAGModel;

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

	// TODO: observations on skills as a sample

	public Student(int id, AbstractModelBuilder<F> builder, AnswerStrategy<F> strategy) {
		this.id = id;
		this.builder = builder;
		this.model = builder.getModel();
		this.answerStrategy = strategy;

		// TODO: generate list of all answers
	}

	/**
	 * Generates an answer for the given question.
	 *
	 * @param question the question to answer to
	 * @return 0 or 1 based on the question
	 */
	@Override
	public int answer(Question question) {
		// TODO: add noisy version of answerStrategy
		return answerStrategy.answer(model, question);
	}

}
