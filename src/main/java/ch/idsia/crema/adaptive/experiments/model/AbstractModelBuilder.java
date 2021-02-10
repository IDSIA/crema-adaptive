package ch.idsia.crema.adaptive.experiments.model;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.List;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    05.02.2021 13:44
 */
public abstract class AbstractModelBuilder<F extends GenericFactor> {

	/**
	 * Model builded.
	 */
	protected DAGModel<F> model;
	/**
	 * A list of all the skill variables of the model. Each skill is identified by it's variable index in the model.
	 */
	public final TIntList skills = new TIntArrayList();
	/**
	 * A list of all the {@link Question}s of the model. A question has an unique id, a variable index for the
	 * associated skill, a variable index in the model, and a template index.
	 */
	public final List<Question> questions = new ArrayList<>();

	public DAGModel<F> getModel() {
		return model;
	}

}
