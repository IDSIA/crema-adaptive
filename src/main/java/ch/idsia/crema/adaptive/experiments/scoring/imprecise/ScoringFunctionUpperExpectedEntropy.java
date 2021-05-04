package ch.idsia.crema.adaptive.experiments.scoring.imprecise;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.Utils;
import ch.idsia.crema.adaptive.experiments.inference.InferenceApproxLP1;
import ch.idsia.crema.adaptive.experiments.inference.InferenceEngine;
import ch.idsia.crema.adaptive.experiments.scoring.ScoringFunction;
import ch.idsia.crema.entropy.AbellanEntropy;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.commons.math3.optim.linear.NoFeasibleSolutionException;


/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    05.02.2021 15:49
 */
public class ScoringFunctionUpperExpectedEntropy implements ScoringFunction<IntervalFactor> {

	private final AbellanEntropy entropy = new AbellanEntropy();

	private InferenceEngine inferenceEngine = new InferenceApproxLP1();

	public ScoringFunctionUpperExpectedEntropy setInferenceEngine(InferenceEngine inferenceEngine) {
		this.inferenceEngine = inferenceEngine;
		return this;
	}

	/**
	 * A {@link ScoringFunction} based on the expected mean upper entropy change from the answer.
	 *
	 * @param model        model to work on
	 * @param question     question to evaluate
	 * @param observations evidence
	 * @return a score between 0 and 1
	 * @throws Exception if something bad happens
	 */
	@Override
	public double score(DAGModel<IntervalFactor> model, Question question, TIntIntMap observations) throws Exception {
		final double[] HSQs = new double[2];

		for (int a = 0; a < 2; a++) {
			TIntIntMap obs = new TIntIntHashMap(observations);
			obs.put(question.variable, a);
			double HSq;

			try {
				final IntervalFactor query = inferenceEngine.query(model, obs, question.skill); // TODO: bring outside
				final double[] PSq = entropy.getMaxEntropy(query.getLower().clone(), query.getUpper().clone());
				HSq = Utils.H(PSq);
			} catch (NoFeasibleSolutionException e) {
				System.err.printf("No Feasible Solution for HSq: question=%d skill=%d state=%d obs=%s %n", question.variable, question.skill, a, obs);
				HSq = .0;
			}

			HSQs[a] = HSq;
		}

		try {
			final IntervalFactor PQ = inferenceEngine.query(model, observations, question.variable);
			final double[] lower = PQ.getLower();

			final double score0 = lower[0] * HSQs[0] + (1 - lower[0]) * HSQs[1];
			final double score1 = lower[1] * HSQs[1] + (1 - lower[1]) * HSQs[0];

			return -(Math.max(score0, score1));
		} catch (NoFeasibleSolutionException e) {
			System.err.printf("No Feasible Solution for PQ: question=%d obs=%s %n", question.variable, observations);
			return 0.0;
		}
	}
}
