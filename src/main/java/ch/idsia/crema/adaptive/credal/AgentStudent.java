package ch.idsia.crema.adaptive.credal;

import ch.idsia.crema.adaptive.credal.model.AbstractModelBuilder;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.DAGModel;

import java.util.Random;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    08.02.2021 15:58
 */
public class AgentStudent<F extends GenericFactor> {

	final Random random;
	final int id;

	final AbstractModelBuilder<F> builder;
	final DAGModel<F> model;

	public AgentStudent(int id, AbstractModelBuilder<F> builder) {
		this.random = new Random(id);
		this.id = id;
		this.builder = builder;
		this.model = builder.getModel();
	}

	/**
	 * Generates an answer for the given question.
	 *
	 * @param question the question to answer to
	 * @return 0 or 1 based on the question
	 */
	int answer(Question question) {
		final int x = random.nextInt(2);
		System.out.printf("AgentStudent:       question=%-3d answer=%d%n", question.id, x);
		return x;
	}

}
