package ch.idsia.crema.adaptive.experiments.answering;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.agents.Student;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    09.02.2021 12:53
 */
public interface AnswerStrategyModel<F extends GenericFactor> extends AnswerStrategy<Student<F>> {

	/**
	 * @param model    structure model
	 * @param question from a {@link ch.idsia.crema.adaptive.experiments.agents.Teacher}
	 * @param skills   evidence map on the skills wher the key is the skill variable in the model and the value is its
	 *                 real state
	 * @return the answer found
	 */
	int answer(DAGModel<F> model, Question question, TIntIntMap skills);

	/**
	 * The default implementation is a wrapper of the {@link #answer(DAGModel, Question, TIntIntMap)} method.
	 *
	 * @param student  who need to give the answer
	 * @param question chosen by a {@link ch.idsia.crema.adaptive.experiments.agents.Teacher}
	 * @return the answer found
	 */
	@Override
	default int answer(Student<F> student, Question question) {
		return answer(student.getModel(), question, student.getSkills());
	}
}
