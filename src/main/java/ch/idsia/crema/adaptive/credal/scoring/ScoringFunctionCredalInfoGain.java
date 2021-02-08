package ch.idsia.crema.adaptive.credal.scoring;

import ch.idsia.crema.adaptive.credal.Question;
import ch.idsia.crema.adaptive.credal.Utils;
import ch.idsia.crema.entropy.AbellanEntropy;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.inference.approxlp2.ApproxLP2;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;


/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    05.02.2021 15:49
 */
public class ScoringFunctionCredalInfoGain implements ScoringFunction<IntervalFactor> {

	private final AbellanEntropy entropy = new AbellanEntropy();
	private final ApproxLP2 approx = new ApproxLP2();

	@Override
	public double score(DAGModel<IntervalFactor> model, Question question, TIntIntMap observations) throws Exception {
		// compute... something similar to a information gain
		final double[] HSQs = new double[2];
		for (int a = 0; a < 2; a++) {
			TIntIntMap obs = new TIntIntHashMap(observations);
			obs.put(question.variable, a);

			final IntervalFactor query = approx.query(model, question.skill, obs);
			final double[] PSq = entropy.getMaxEntropy(query.getLower(), query.getUpper());
			final double HSq = Utils.H(PSq);

			HSQs[a] = HSq;
		}

		final IntervalFactor pQ = approx.query(model, question.variable, observations);
		final double[] lower = pQ.getLower();

		final double score0 = lower[0] * HSQs[0] + (1 - lower[0]) * HSQs[1];
		final double score1 = lower[1] * HSQs[1] + (1 - lower[1]) * HSQs[0];

		return Math.max(score0, score1);
	}
}
