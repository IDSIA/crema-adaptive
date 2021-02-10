package ch.idsia.crema.adaptive.experiments.persistence;

import ch.idsia.crema.adaptive.experiments.agents.Teacher;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.bp.BeliefPropagation;
import gnu.trove.list.TIntList;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    09.02.2021 16:37
 */
public class PersistBayesian implements Persist<BayesianFactor> {

	private BeliefPropagation<BayesianFactor> inference;

	@Override
	public OutputBayesian register(Teacher<BayesianFactor> teacher) {
		if (inference == null)
			inference = new BeliefPropagation<>(teacher.getModel());

		final OutputBayesian output = new OutputBayesian();

		final TIntList skills = teacher.getSkills();

		for (int s = 0; s < skills.size(); s++) {
			int skill = skills.get(s);
			final BayesianFactor query = inference.query(skill, teacher.getObservations());

			output.skills.add(skill);
			output.factors.add(query);
		}

		return output;
	}

}
