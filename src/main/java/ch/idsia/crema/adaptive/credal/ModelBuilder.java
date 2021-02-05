package ch.idsia.crema.adaptive.credal;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.DAGModel;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    05.02.2021 13:44
 */
public abstract class ModelBuilder<F extends GenericFactor> {

	DAGModel<F> model;

	final int[] varSkills;
	final int[][] varQuestions;

	public ModelBuilder(int skills, int questions) {
		varSkills = new int[skills];
		varQuestions = new int[skills][questions];
	}

	public DAGModel<F> getModel() {
		return model;
	}

}
