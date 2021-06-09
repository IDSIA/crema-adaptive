package ch.idsia.crema.adaptive.experiments;

import ch.idsia.crema.adaptive.experiments.agents.Student;
import ch.idsia.crema.adaptive.experiments.agents.Teacher;
import ch.idsia.crema.adaptive.experiments.model.imprecise.CredalMinimalistic1x2x9;
import ch.idsia.crema.adaptive.experiments.model.precise.BayesianMinimalistic1x2x9;
import ch.idsia.crema.adaptive.experiments.persistence.PersistBayesian;
import ch.idsia.crema.adaptive.experiments.persistence.PersistCredal;
import ch.idsia.crema.adaptive.experiments.scoring.imprecise.ScoringFunctionCredalMode;
import ch.idsia.crema.adaptive.experiments.scoring.imprecise.ScoringFunctionUpperExpectedEntropy;
import ch.idsia.crema.adaptive.experiments.scoring.precise.ScoringFunctionBayesianMode;
import ch.idsia.crema.adaptive.experiments.scoring.precise.ScoringFunctionExpectedEntropy;
import ch.idsia.crema.adaptive.experiments.scoring.precise.ScoringFunctionRandom;
import ch.idsia.crema.adaptive.experiments.stopping.imprecise.StoppingConditionCredalMeanEntropy;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.sampling.BayesianNetworkSampling;
import ch.idsia.crema.utility.RandomUtil;
import gnu.trove.map.TIntIntMap;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Authors:  	Giorgia Adorni
 * Project: 	crema-adaptive
 * Date:    	19.02.2021 10:30
 */
public class AdaptiveSurveyMinimalistic1x2x9Simulation {
	// we will generate samples up to 256 students
	static final int N_STUDENTS = 256;
	// we are going to use a model with 20 questions: each template has 5 questions
	static final int N_QUESTIONS = 2;
	// since we are using an ExecutorService, we will run 16 tests in parallel
	static final int PARALLEL_COUNT = 9;

	public static void main(String[] args) throws Exception {

		// General model for sampling the students. Note that teacher and
		// students model need only to have the same questions with the same
		// variable index (this automatically force the need of the same number
		// of skills)

		final BayesianMinimalistic1x2x9 minimalistic1x2x9 = new BayesianMinimalistic1x2x9(N_QUESTIONS);
		// Note on sampling:
		// The random (seed) management is done by crema. For repeatable
		// experiments, save the sampling to a file or sed a seed in
		// RandomUtil#setRandom
		final Random random = new Random(42);
		RandomUtil.setRandom(random);

		final BayesianNetworkSampling bns = new BayesianNetworkSampling();
		final TIntIntMap[] samples = bns.samples(minimalistic1x2x9.getModel(), N_STUDENTS);

		// Creates students based on samples: note that we are not using an
		// AnswerStrategy object since we don't need it

		final List<Student<BayesianFactor>> students = IntStream.range(0, N_STUDENTS)
				.mapToObj(id -> new Student<BayesianFactor>(id, samples[id], minimalistic1x2x9.skills))
				.collect(Collectors.toList());

		final String path = "output/Minimalistic1x2x9/";


		final ExperimentSuite[] experiments = {
				new ExperimentSuite(
						path, "Minimalistic1x2x9", "non-adaptive", students,
						() -> new Teacher<>(new BayesianMinimalistic1x2x9(N_QUESTIONS), new ScoringFunctionRandom(0)).setPersist(new PersistBayesian()),
						2, 3
				),
				new ExperimentSuite(
						path, "Minimalistic1x2x9", "bayesian-adaptive-entropy", students,
						() -> new Teacher<>(new BayesianMinimalistic1x2x9(N_QUESTIONS), new ScoringFunctionExpectedEntropy()).setPersist(new PersistBayesian()),
						0, 1, 3
				),
				new ExperimentSuite(
						path, "Minimalistic1x2x9", "bayesian-adaptive-mode", students,
						() -> new Teacher<>(new BayesianMinimalistic1x2x9(N_QUESTIONS), new ScoringFunctionBayesianMode()).setPersist(new PersistBayesian()),
						0, 1, 3
				),
				new ExperimentSuite(
						path, "Minimalistic1x2x9", "credal-eps05-adaptive-entropy", students,
						() -> new Teacher<>(new CredalMinimalistic1x2x9(N_QUESTIONS).setEps(.05), new ScoringFunctionUpperExpectedEntropy(), new StoppingConditionCredalMeanEntropy(.1)).setPersist(new PersistCredal()),
						0, 1, 3
				),
				new ExperimentSuite(
						path, "Minimalistic1x2x9", "credal-eps05-adaptive-mode", students,
						() -> new Teacher<>(new CredalMinimalistic1x2x9(N_QUESTIONS).setEps(.05), new ScoringFunctionCredalMode(), new StoppingConditionCredalMeanEntropy(.1)).setPersist(new PersistCredal()),
						0, 1, 3
				),
				new ExperimentSuite(
						path, "Minimalistic1x2x9", "credal-eps01-adaptive-entropy", students,
						() -> new Teacher<>(new CredalMinimalistic1x2x9(N_QUESTIONS).setEps(.01), new ScoringFunctionUpperExpectedEntropy(), new StoppingConditionCredalMeanEntropy(.1)).setPersist(new PersistCredal()),
						0, 1, 3
				),
				new ExperimentSuite(
						path, "Minimalistic1x2x9", "credal-eps01-adaptive-mode", students,
						() -> new Teacher<>(new CredalMinimalistic1x2x9(N_QUESTIONS).setEps(.01), new ScoringFunctionCredalMode(), new StoppingConditionCredalMeanEntropy(.1)).setPersist(new PersistCredal()),
						0, 1, 3
				),
		};

		experiments[0].run(PARALLEL_COUNT); // Bayesian4x2x4 non-adaptive
		experiments[1].run(PARALLEL_COUNT); // Bayesian4x2x4 adaptive+entropy
		experiments[2].run(PARALLEL_COUNT); // Bayesian4x2x4 adaptive+mode
		experiments[3].run(PARALLEL_COUNT); // Credal4x2x4 adaptive+entropy 0.05
		experiments[4].run(PARALLEL_COUNT); // Credal4x2x4 adaptive+mode 0.05
		experiments[5].run(PARALLEL_COUNT); // Credal4x2x4 adaptive+entropy 0.01
		experiments[6].run(PARALLEL_COUNT); // Credal4x2x4 adaptive+mode 0.01

	}

}
