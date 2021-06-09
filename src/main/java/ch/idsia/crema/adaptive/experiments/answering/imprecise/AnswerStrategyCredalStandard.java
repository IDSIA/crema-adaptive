package ch.idsia.crema.adaptive.experiments.answering.imprecise;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.answering.AnswerStrategyModel;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;

import java.util.Random;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    09.02.2021 10:56
 */
public class AnswerStrategyCredalStandard implements AnswerStrategyModel<IntervalFactor> {

	private final Random random;

	public AnswerStrategyCredalStandard(long seed) {
		this.random = new Random(seed);
	}

	// TODO: add noisy version of this

	/**
	 * @param model    structure model
	 * @param question from a {@link ch.idsia.crema.adaptive.experiments.agents.Teacher}
	 * @param skills   evidence map on the skills wher the key is the skill variable in the model and the value is its
	 *                 real state
	 * @return an answer according to the {@link ch.idsia.crema.adaptive.experiments.agents.Student}'s credal model
	 * and its skills.
	 */
	@Override
	public int answer(DAGModel<IntervalFactor> model, Question question, TIntIntMap skills) {
		final int s = skills.get(question.skill);

		// TODO: check
		final double[] lower = model.getFactor(question.variable).getLowerAt(s);
		final double[] upper = model.getFactor(question.variable).getUpperAt(s);

		final double l = Math.max(lower[0], 1 - upper[1]);
		final double u = Math.min(upper[0], 1 - lower[1]);

		final double p = l + random.nextDouble() / (u - l);
		final double x = random.nextDouble();

		if (x < p)
			return 0;
		return 1;
	}
}
