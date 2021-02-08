package ch.idsia.crema.adaptive.credal;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.DAGModel;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    05.02.2021 13:44
 */
public abstract class AbstractModelBuilder<F extends GenericFactor> {

	DAGModel<F> model;

	final int[] skills;
	final int[][] questions;

	public AbstractModelBuilder(int nSkills, int nQuestions) {
		this.skills = new int[nSkills];
		this.questions = new int[nSkills][nQuestions];
	}

	public DAGModel<F> getModel() {
		return model;
	}

}
