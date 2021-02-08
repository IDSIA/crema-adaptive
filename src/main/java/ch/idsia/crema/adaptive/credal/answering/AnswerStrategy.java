package ch.idsia.crema.adaptive.credal.answering;

import ch.idsia.crema.adaptive.credal.Question;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.DAGModel;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    08.02.2021 18:08
 */
public interface AnswerStrategy<F extends GenericFactor> {

	int answer(DAGModel<F> model, Question question);

}
