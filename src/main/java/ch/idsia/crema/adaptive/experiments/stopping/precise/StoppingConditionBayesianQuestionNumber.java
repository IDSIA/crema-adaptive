package ch.idsia.crema.adaptive.experiments.stopping.precise;

import ch.idsia.crema.adaptive.experiments.Skill;
import ch.idsia.crema.adaptive.experiments.stopping.StoppingCondition;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;

import java.util.List;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    09.02.2021 09:57
 */
public class StoppingConditionBayesianQuestionNumber implements StoppingCondition<BayesianFactor> {

	private final int number;

	/**
	 * @param number maximum amount of question to pose (inclusive)
	 */
	public StoppingConditionBayesianQuestionNumber(int number) {
		this.number = number;
	}

	@Override
	public boolean stop(DAGModel<BayesianFactor> model, List<Skill> skills, TIntIntMap observations) throws Exception {
		return observations.size() >= number;
	}
}
