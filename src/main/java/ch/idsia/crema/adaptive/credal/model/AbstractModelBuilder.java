package ch.idsia.crema.adaptive.credal.model;

import ch.idsia.crema.adaptive.credal.Question;
import ch.idsia.crema.adaptive.credal.Skill;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.DAGModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    05.02.2021 13:44
 */
public abstract class AbstractModelBuilder<F extends GenericFactor> {

	DAGModel<F> model;
	public final List<Skill> skills = new ArrayList<>();
	public final List<Question> questions = new ArrayList<>();

	public DAGModel<F> getModel() {
		return model;
	}

}
