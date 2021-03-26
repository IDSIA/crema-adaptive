package ch.idsia.crema.adaptive.experiments.persistence;

import ch.idsia.crema.adaptive.experiments.agents.Teacher;
import ch.idsia.crema.adaptive.experiments.inference.InferenceApproxLP1;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import gnu.trove.list.TIntList;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    09.02.2021 16:37
 */
public class PersistCredal implements Persist<IntervalFactor> {

	private final InferenceApproxLP1 inference = new InferenceApproxLP1();

	@Override
	public Output<IntervalFactor> register(Teacher<IntervalFactor> teacher) throws Exception {

		final OutputCredal output = new OutputCredal();

		final TIntList skills = teacher.getSkills();

		for (int s = 0; s < skills.size(); s++) {
			int skill = skills.get(s);

			final IntervalFactor query = inference.query(teacher.getModel(), teacher.getObservations(), skill);

			output.skills.add(skill);
			output.factors.add(query);
		}

		return output;
	}

}
