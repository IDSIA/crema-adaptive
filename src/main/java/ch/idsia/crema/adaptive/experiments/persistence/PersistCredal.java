package ch.idsia.crema.adaptive.experiments.persistence;

import ch.idsia.crema.adaptive.experiments.agents.Teacher;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.inference.approxlp2.ApproxLP2;
import gnu.trove.list.TIntList;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    09.02.2021 16:37
 */
public class PersistCredal implements Persist<IntervalFactor> {

	private final ApproxLP2 inference = new ApproxLP2();

	@Override
	public Output<IntervalFactor> register(Teacher<IntervalFactor> teacher) throws Exception {

		final OutputCredal output = new OutputCredal();

		final TIntList skills = teacher.getSkills();

		for (int s = 0; s < skills.size(); s++) {
			int skill = skills.get(s);

			final IntervalFactor query = inference.query(teacher.getModel(), skill, teacher.getObservations());

			output.skills.add(skill);
			output.factors.add(query);
		}

		return output;
	}

}
