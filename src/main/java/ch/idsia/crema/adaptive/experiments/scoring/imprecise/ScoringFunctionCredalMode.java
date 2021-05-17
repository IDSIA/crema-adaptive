package ch.idsia.crema.adaptive.experiments.scoring.imprecise;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.inference.InferenceApproxLP1;
import ch.idsia.crema.adaptive.experiments.inference.InferenceEngine;
import ch.idsia.crema.adaptive.experiments.scoring.ScoringFunction;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    08.02.2021 15:23
 */
public class ScoringFunctionCredalMode implements ScoringFunction<IntervalFactor> {

	private boolean verbose = false;

	private InferenceEngine inferenceEngine = new InferenceApproxLP1();
	private GoalType goalType = GoalType.MINIMIZE;

	public ScoringFunctionCredalMode setInferenceEngine(InferenceEngine inferenceEngine) {
		this.inferenceEngine = inferenceEngine;
		return this;
	}

	public ScoringFunctionCredalMode setMinimize() {
		this.goalType = GoalType.MINIMIZE;
		return this;
	}

	public ScoringFunctionCredalMode setMaximize() {
		this.goalType = GoalType.MAXIMIZE;
		return this;
	}

	void print(LinearConstraint lc) {
		if (verbose)
			System.out.println(lc.getCoefficients() + " " + lc.getRelationship() + " " + lc.getValue());
	}

	@Override
	public double score(DAGModel<IntervalFactor> model, Question question, TIntIntMap observations) throws Exception {

		final IntervalFactor pS = inferenceEngine.query(model, observations, question.skill); // TODO: bring outside
		final int m = model.getSize(question.skill); // number of state of skill under analysis

		List<LinearConstraint> general_constraints = new ArrayList<>();

		// normalize
		if (verbose) System.out.println("normalize");
		final double[] c = new double[2 * m];
		Arrays.fill(c, 1.);
		general_constraints.add(new LinearConstraint(c, Relationship.EQ, 1.));
		print(general_constraints.get(general_constraints.size() - 1));

		// Pi
		if (verbose) System.out.println("Pi");
		for (int k = 0; k < m; k++) {
			final double[] C = new double[2 * m];
			C[k] = 1;       // vi
			C[m + k] = 1;   // wi
			general_constraints.add(new LinearConstraint(C, Relationship.GEQ, pS.getLower()[k])); // p_lower
			print(general_constraints.get(general_constraints.size() - 1));
			general_constraints.add(new LinearConstraint(C, Relationship.LEQ, pS.getUpper()[k])); // p_upper
			print(general_constraints.get(general_constraints.size() - 1));
		}

		// PI
		if (verbose) System.out.println("PIE");
		final IntervalFactor factor = model.getFactor(question.variable);
		final double[] lower = factor.getLower();
		final double[] upper = factor.getUpper();

		for (int k = 0; k < m; k++) {
			final double[] c1 = new double[2 * m];
			c1[k] = 1 - upper[k];   // 1 - PI_upper * vi
			c1[k + m] = -upper[k];  //   - PI_upper * wi
			general_constraints.add(new LinearConstraint(c1, Relationship.LEQ, 0));
			print(general_constraints.get(general_constraints.size() - 1));

			final double[] c2 = new double[2 * m];
			c2[k] = lower[k] - 1;   // PI_lower * vi -1
			c2[k + m] = lower[k];   // PI_lower * wi
			general_constraints.add(new LinearConstraint(c2, Relationship.LEQ, 0));
			print(general_constraints.get(general_constraints.size() - 1));
		}

		List<Double> solutions = new ArrayList<>();

		if (verbose) System.out.println("constraint opt.");
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < m; j++) {
				final double[] ci = new double[2 * m];
				ci[i] = 1;      // vi
				ci[j + m] = 1;  // wi

				final LinearObjectiveFunction f = new LinearObjectiveFunction(ci, 0);

				final List<LinearConstraint> constraints = new ArrayList<>();
				for (int k = 0; k < m; k++) {
					if (i != k) {
						double[] c0 = new double[2 * m];
						c0[i] = 1;  // vi
						c0[k] = -1; // -wi
						constraints.add(new LinearConstraint(c0, Relationship.GEQ, 0)); // <= 1
						print(constraints.get(constraints.size() - 1));
					}
					if (j != k) {
						double[] c0 = new double[2 * m];
						c0[m + j] = 1;  // vi
						c0[m + k] = -1; // -wi
						constraints.add(new LinearConstraint(c0, Relationship.GEQ, 0)); // <= 0
						print(constraints.get(constraints.size() - 1));
					}
				}

				constraints.addAll(general_constraints);

				try {
					final PointValuePair solution = new SimplexSolver().optimize(f, new LinearConstraintSet(constraints), goalType);
					solutions.add(solution.getValue());
				} catch (NoFeasibleSolutionException e) {
					System.err.println(e.getMessage());
				}
			}
		}

		if (verbose) System.out.println("solutions found: " + solutions.size());

		if (goalType == GoalType.MAXIMIZE) {
			final double HS = Arrays.stream(pS.getUpper()).max().orElse(0.0);
			return solutions.stream().max(Comparator.comparingDouble(x -> x)).orElseThrow(IllegalStateException::new) - HS;
		} else {
			final double HS = Arrays.stream(pS.getLower()).max().orElse(0.0);
			return solutions.stream().min(Comparator.comparingDouble(x -> x)).orElseThrow(IllegalStateException::new) - HS;
		}
	}
}
