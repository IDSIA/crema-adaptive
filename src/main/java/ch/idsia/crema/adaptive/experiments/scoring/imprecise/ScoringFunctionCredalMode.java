package ch.idsia.crema.adaptive.experiments.scoring.imprecise;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.scoring.ScoringFunction;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.inference.approxlp2.ApproxLP2;
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

	private final ApproxLP2 approx = new ApproxLP2();

	private GoalType goalType = GoalType.MINIMIZE;

	public ScoringFunctionCredalMode setMinimize() {
		this.goalType = GoalType.MINIMIZE;
		return this;
	}

	public ScoringFunctionCredalMode setMaximize() {
		this.goalType = GoalType.MAXIMIZE;
		return this;
	}

	@Override
	public double score(DAGModel<IntervalFactor> model, Question question, TIntIntMap observations) throws Exception {

		final IntervalFactor pS = approx.query(model, question.skill, observations); // TODO: bring outside
		final int m = model.getSize(question.skill); // number of state of skill under analysis

		List<LinearConstraint> general_constraints = new ArrayList<>();

		// normalize
		final double[] c = new double[2 * m];
		Arrays.fill(c, 1.);
		general_constraints.add(new LinearConstraint(c, Relationship.EQ, 1));

		// Pi
		for (int k = 0; k < m; k++) {
			final double[] C = new double[2 * m];
			C[k] = 1;
			C[m + k] = 1;
			general_constraints.add(new LinearConstraint(C, Relationship.GEQ, pS.getLower()[k]));
			general_constraints.add(new LinearConstraint(C, Relationship.LEQ, pS.getUpper()[k]));
		}

		// PI
		final IntervalFactor factor = model.getFactor(question.variable);
		final double[] lower = factor.getLower();
		final double[] upper = factor.getUpper();

		for (int k = 0; k < m; k++) {
			final double[] c1 = new double[2 * m];
			c1[k] = 1 - upper[k];
			c1[k + m] = -upper[k];
			general_constraints.add(new LinearConstraint(c1, Relationship.LEQ, 0));

			final double[] c2 = new double[2 * m];
			c2[k] = 1 - lower[k];
			c2[k + m] = -lower[k];
			general_constraints.add(new LinearConstraint(c2, Relationship.GEQ, 0));
		}

		List<Double> solutions = new ArrayList<>();

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < m; j++) {
				final double[] ci = new double[2 * m];
				ci[i] = 1;
				ci[j + m] = 1;

				LinearObjectiveFunction f = new LinearObjectiveFunction(ci, 0);

				List<LinearConstraint> constraints = new ArrayList<>();
				for (int k = 0; k < m; k++) {
					if (i != k) {
						double[] c0 = new double[2 * m];
						c0[i] = 1;
						c0[k] = -1;
						constraints.add(new LinearConstraint(c0, Relationship.GEQ, 0));
					}
					if (j != k) {
						double[] c0 = new double[2 * m];
						c0[m + j] = 1;
						c0[m + k] = -1;
						constraints.add(new LinearConstraint(c0, Relationship.GEQ, 0));
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

		if (goalType == GoalType.MAXIMIZE) {
			return solutions.stream().max(Comparator.comparingDouble(x -> x)).orElseThrow(IllegalStateException::new);
		} else {
			return solutions.stream().min(Comparator.comparingDouble(x -> x)).orElseThrow(IllegalStateException::new);
		}
	}
}