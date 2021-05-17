package ch.idsia.crema.adaptive.experiments;

import ch.idsia.crema.adaptive.experiments.agents.Student;
import ch.idsia.crema.adaptive.experiments.agents.Teacher;
import ch.idsia.crema.adaptive.experiments.model.imprecise.CredalMinimalistic1x2x1;
import ch.idsia.crema.adaptive.experiments.model.precise.BayesianMinimalistic;
import ch.idsia.crema.adaptive.experiments.persistence.PersistBayesian;
import ch.idsia.crema.adaptive.experiments.persistence.PersistCredal;
import ch.idsia.crema.adaptive.experiments.scoring.imprecise.ScoringFunctionCredalMode;
import ch.idsia.crema.adaptive.experiments.scoring.precise.ScoringFunctionExpectedEntropy;
import ch.idsia.crema.adaptive.experiments.scoring.precise.ScoringFunctionRandom;
import ch.idsia.crema.adaptive.experiments.stopping.StoppingConditionQuestionNumber;
import ch.idsia.crema.adaptive.experiments.stopping.imprecise.StoppingConditionCredalMeanEntropy;
import ch.idsia.crema.adaptive.experiments.stopping.precise.StoppingConditionBayesianMeanEntropy;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.sampling.BayesianNetworkSampling;
import gnu.trove.map.TIntIntMap;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    04.02.2021 16:09
 */
public class AdaptiveSurvey {

	// we will generate samples up to 20 students
	static final int N_STUDENTS = 20;
	// we are going to use a minimalistic (1xSkill NxQuestion) model with 20 questions
	static final int N_QUESTIONS = 5;
	// since we are using an ExecutorService, we will run 4 tests in parallel
	static final int PARALLEL_COUNT = 4;

	public static void main(String[] args) throws Exception {
		/*
		    general model for sampling the students. Note that teacher and students model need only to have the same
		    questions with the same variable index (this automatically force the need of the same number of skills)
		 */
		final BayesianMinimalistic minimalistic = new BayesianMinimalistic(N_QUESTIONS, .4, .6);
		/*
			note on sampling:
		    the random (seed) management is done by crema
		    for repeatable experiments, save the sampling to a file or sed a seed in RandomUtil#setRandom
		 */
		final BayesianNetworkSampling bns = new BayesianNetworkSampling();
		final TIntIntMap[] samples = bns.samples(minimalistic.getModel(), N_STUDENTS);

		// creates students based on samples: note that we are not using an AnswerStrategy object since we don't need it
		final List<Student<BayesianFactor>> students = IntStream.range(0, N_STUDENTS)
				.mapToObj(id -> new Student<BayesianFactor>(id, samples[id], minimalistic.skills))
				.collect(Collectors.toList());

		final String path = "output/minimalistic/";

		final ExperimentSuite[] experiments = {
				new ExperimentSuite(
						path, "minimalistic", "non-adaptive", students,
						() -> new Teacher<>(
								new BayesianMinimalistic(N_QUESTIONS, .4, .6),
								new ScoringFunctionRandom(0),
								new StoppingConditionQuestionNumber<>(10)
						).setPersist(new PersistBayesian()),
						2, 3
				),
				new ExperimentSuite(
						path, "minimalistic", "bayesian-adaptive-entropy", students,
						() -> new Teacher<>(
								// model to use for the question choice
								new BayesianMinimalistic(N_QUESTIONS, .4, .6),
								// scoring function used to select the next question
								new ScoringFunctionExpectedEntropy(),
								// first stopping criteria: check for mean bayesian entropy of skills
								new StoppingConditionBayesianMeanEntropy(.3),
								// second stopping criteria: stop after 10 questions
								new StoppingConditionQuestionNumber<>(10)
						).setPersist(new PersistBayesian()),
						0, 1, 3
				),
				new ExperimentSuite(
						path, "minimalistic", "credal-adaptive-entropy", students,
						() -> new Teacher<>(
								new CredalMinimalistic1x2x1(N_QUESTIONS, .4, .4, .6, .6),
								new ScoringFunctionCredalMode(),
								new StoppingConditionCredalMeanEntropy(.5),
								new StoppingConditionQuestionNumber<>(10)
						).setPersist(new PersistCredal()),
						0, 1, 3
				),
		};

		experiments[0].run(PARALLEL_COUNT); // Bayesian4x2x4 non-adaptive
		experiments[1].run(PARALLEL_COUNT); // Bayesian4x2x4 adaptive+entropy
		experiments[2].run(PARALLEL_COUNT); // Credal4x2x4 adaptive+entropy
	}

}
