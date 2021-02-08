package ch.idsia.crema.adaptive.credal;

import ch.idsia.crema.adaptive.credal.answering.AnswerStrategy;
import ch.idsia.crema.adaptive.credal.model.AbstractModelBuilder;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.DAGModel;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    08.02.2021 15:58
 */
public class AgentStudent<F extends GenericFactor> {

	final int id;

	final AbstractModelBuilder<F> builder;
	final DAGModel<F> model;

	final AnswerStrategy<F> answerStrategy;

	public AgentStudent(int id, AbstractModelBuilder<F> builder, AnswerStrategy<F> strategy) {
		this.id = id;
		this.builder = builder;
		this.model = builder.getModel();
		this.answerStrategy = strategy;
	}

	/**
	 * Generates an answer for the given question.
	 *
	 * @param question the question to answer to
	 * @return 0 or 1 based on the question
	 */
	int answer(Question question) {
		return answerStrategy.answer(model, question);
	}

}
