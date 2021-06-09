package ch.idsia.crema.adaptive.experiments.scoring.imprecise;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.inference.InferenceApproxLP1;
import ch.idsia.crema.adaptive.experiments.inference.InferenceEngine;
import ch.idsia.crema.adaptive.experiments.scoring.ScoringFunction;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    08.02.2021 15:23
 */
public class ScoringFunctionUpperLowerProbabilityOfRight implements ScoringFunction<IntervalFactor> {

	private InferenceEngine inferenceEngine = new InferenceApproxLP1();

	private boolean useUpper = true;

	public ScoringFunctionUpperLowerProbabilityOfRight setInferenceEngine(InferenceEngine inferenceEngine) {
		this.inferenceEngine = inferenceEngine;
		return this;
	}

	public ScoringFunctionUpperLowerProbabilityOfRight useUpper() {
		useUpper = true;
		return this;
	}

	public ScoringFunctionUpperLowerProbabilityOfRight useLower() {
		useUpper = false;
		return this;
	}

	/**
	 * A {@link ScoringFunction} based on the upper probability of answer correctly.
	 *
	 * @param model        model to work on
	 * @param question     question to evaluate
	 * @param observations evidence
	 * @return a score between 0 and 1
	 * @throws Exception if something bad happens
	 */
	@Override
	public double score(DAGModel<IntervalFactor> model, Question question, TIntIntMap observations) throws Exception {
		final IntervalFactor pQ = inferenceEngine.query(model, observations, question.variable);

		double[] values;

		if (useUpper) {
			values = pQ.getUpper();
		} else {
			values = pQ.getLower();
		}

		return 1. - Math.abs(values[0] - 0.5);
	}
}
