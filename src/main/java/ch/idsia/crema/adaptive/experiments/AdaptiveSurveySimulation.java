package ch.idsia.crema.adaptive.experiments;

import ch.idsia.crema.adaptive.experiments.agents.AgentTeacher;
import ch.idsia.crema.adaptive.experiments.agents.Student;
import ch.idsia.crema.adaptive.experiments.agents.Teacher;
import ch.idsia.crema.adaptive.experiments.model.imprecise.Credal4Skill4Difficulty5Question;
import ch.idsia.crema.adaptive.experiments.model.imprecise.CredalMinimalistic;
import ch.idsia.crema.adaptive.experiments.model.precise.Bayesian4Skill4Difficulty;
import ch.idsia.crema.adaptive.experiments.model.precise.BayesianMinimalistic;
import ch.idsia.crema.adaptive.experiments.persistence.PersistBayesian;
import ch.idsia.crema.adaptive.experiments.persistence.PersistCredal;
import ch.idsia.crema.adaptive.experiments.scoring.imprecise.ScoringFunctionUpperExpectedEntropy;
import ch.idsia.crema.adaptive.experiments.scoring.precise.ScoringFunctionExpectedEntropy;
import ch.idsia.crema.adaptive.experiments.scoring.precise.ScoringFunctionRandom;
import ch.idsia.crema.adaptive.experiments.stopping.StoppingConditionQuestionNumber;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.sampling.BayesianNetworkSampling;
import ch.idsia.crema.utility.RandomUtil;
import gnu.trove.map.TIntIntMap;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * Authors:  	Claudio "Dna" Bonesana, Giorgia Adorni
 * Project: 	crema-adaptive
 * Date:    	V1.0: 04.02.2021 16:09
 * 				V2.0: 11.02.2021 16:00
 */
public class AdaptiveSurveySimulation {

	// we will generate samples up to 256 students
	static final int N_STUDENTS = 256;
	// we are going to use a model with 20 questions: each template has 5 questions
	static final int N_QUESTIONS = 5;
	// since we are using an ExecutorService, we will run 16 tests in parallel
	static final int PARALLEL_COUNT = 3;

	public static void main(String[] args) throws Exception {

		// General model for sampling the students. Note that teacher and
		// students model need only to have the same questions with the same
		// variable index (this automatically force the need of the same number
		// of skills)

		final Bayesian4Skill4Difficulty bayesian4x4x4 = new Bayesian4Skill4Difficulty(N_QUESTIONS);
		final BayesianMinimalistic minimalistic = new BayesianMinimalistic(N_QUESTIONS, .4, .6);

		// Note on sampling:
		// The random (seed) management is done by crema. For repeatable
		// experiments, save the sampling to a file or sed a seed in
		// RandomUtil#setRandom
		final Random random = new Random(42);
		RandomUtil.setRandom(random);

		final BayesianNetworkSampling bns = new BayesianNetworkSampling();
		final TIntIntMap[] bayesianSamples = bns.samples(bayesian4x4x4.getModel(), N_STUDENTS);
		final TIntIntMap[] minimalisticSamples = bns.samples(minimalistic.getModel(), N_STUDENTS);

		// Creates students based on samples: note that we are not using an
		// AnswerStrategy object since we don't need it
		final List<Student<BayesianFactor>> bayesian4x4x4Students = IntStream.range(0, N_STUDENTS)
				.mapToObj(id -> new Student<BayesianFactor>(id, bayesianSamples[id]))
				.collect(Collectors.toList());
		final List<Student<BayesianFactor>> minimalisticStudents = IntStream.range(0, N_STUDENTS)
				.mapToObj(id -> new Student<BayesianFactor>(id, minimalisticSamples[id]))
				.collect(Collectors.toList());

		// We will use our lovely ExecutorService
		ExecutorService es = Executors.newFixedThreadPool(PARALLEL_COUNT);

		// All the tasks for the non adaptive survey
		final List<Callable<String[]>> bayesian4x4x4TasksNonAdaptive = bayesian4x4x4Students.stream()
				.map(student -> (Callable<String[]>) () -> {
					// all these tasks are similar: check bayesian experiments for comments!
					try {
						System.out.println("Bayesian4x4 non adaptive " + student.getId());

						final AgentTeacher teacher = new Teacher<>(
								new Bayesian4Skill4Difficulty(N_QUESTIONS),
								new ScoringFunctionRandom(student.getId()),
								new StoppingConditionQuestionNumber<>(20)
						)
								.setPersist(new PersistBayesian());

						new Experiment(teacher, student).run();

						String posteriors = student.getId() + "," + teacher.getResults();
						String answers = student.getAnswers(teacher.getTotalNumberQuestions());
						String profiles = student.getProfiles(teacher.getTotalNumberQuestions());

						return new String[]{posteriors, answers, profiles};
					} catch (Exception e) {
						e.printStackTrace();
						return ArrayUtils.EMPTY_STRING_ARRAY;
					}
				})
				.collect(Collectors.toList());

		final List<Callable<String[]>> bayesianMinimalisticTasksNonAdaptive = minimalisticStudents.stream()
				.map(student -> (Callable<String[]>) () -> {
					// all these tasks are similar: check bayesian experiments for comments!
					try {
						System.out.println("Bayesian minimalistic non adaptive " + student.getId());

						final AgentTeacher teacher = new Teacher<>(
								new BayesianMinimalistic(N_QUESTIONS, .4, .6),
								new ScoringFunctionRandom(student.getId()),
								new StoppingConditionQuestionNumber<>(20)
						)
								.setPersist(new PersistBayesian());

						new Experiment(teacher, student).run();

						String posteriors = student.getId() + "," + teacher.getResults();
						String answers = student.getAnswers(teacher.getTotalNumberQuestions());
						String profiles = student.getProfiles(teacher.getTotalNumberQuestions());

						return new String[]{posteriors, answers, profiles};
					} catch (Exception e) {
						e.printStackTrace();
						return ArrayUtils.EMPTY_STRING_ARRAY;
					}
				})
				.collect(Collectors.toList());

		// all the tasks for the adaptive bayesian survey
		final List<Callable<String[]>> bayesian4x4x4TasksAdaptive = bayesian4x4x4Students.stream()
				.map(student -> (Callable<String[]>) () -> {
					try {
						System.out.println("Bayesian4x4 adaptive " + student.getId());

						/*
							build a teacher for each student since we are in a concurrent environment and the teacher
							will save the output results of a single student
						 */
						final AgentTeacher teacher = new Teacher<>(
								// model to use for the question choice
								new Bayesian4Skill4Difficulty(N_QUESTIONS),
								// scoring function used to select the next question
								new ScoringFunctionExpectedEntropy(),
								// first stopping criteria: check for mean bayesian entropy of skills
//								new StoppingConditionBayesianMeanEntropy(.3),
								// second stopping criteria: stop after 10 questions
								new StoppingConditionQuestionNumber<>(20)
						)
								// we want to save the results and they are of bayesian type
								.setPersist(new PersistBayesian());

						// run new configured experiment
						new Experiment(teacher, student).run();

						// return a row for the CSV file
						String posteriors = student.getId() + "," + teacher.getResults();
						String answers = student.getAnswers(teacher.getTotalNumberQuestions());
						String profiles = student.getProfiles(teacher.getTotalNumberQuestions());

						return new String[]{posteriors, answers, profiles};
					} catch (Exception e) {
						// if something goes wrong, return an empty row that will be filtered out
						e.printStackTrace();
						return ArrayUtils.EMPTY_STRING_ARRAY;
					}
				})
				.collect(Collectors.toList());

		final List<Callable<String[]>> bayesianMinimalisticTasksAdaptive = minimalisticStudents.stream()
				.map(student -> (Callable<String[]>) () -> {
					try {
						System.out.println("Bayesian minimalistic adaptive " + student.getId());

						/*
							build a teacher for each student since we are in a concurrent environment and the teacher
							will save the output results of a single student
						 */
						final AgentTeacher teacher = new Teacher<>(
								// model to use for the question choice
								new BayesianMinimalistic(N_QUESTIONS, .4, .6),
								// scoring function used to select the next question
								new ScoringFunctionExpectedEntropy(),
								// first stopping criteria: check for mean bayesian entropy of skills
//								new StoppingConditionBayesianMeanEntropy(.3),
								// second stopping criteria: stop after 10 questions
								new StoppingConditionQuestionNumber<>(20)
						)
								// we want to save the results and they are of bayesian type
								.setPersist(new PersistBayesian());

						// run new configured experiment
						new Experiment(teacher, student).run();

						// return a row for the CSV file
						String posteriors = student.getId() + "," + teacher.getResults();
						String answers = student.getAnswers(teacher.getTotalNumberQuestions());
						String profiles = student.getProfiles(teacher.getTotalNumberQuestions());

						return new String[]{posteriors, answers, profiles};
					} catch (Exception e) {
						// if something goes wrong, return an empty row that will be filtered out
						e.printStackTrace();
						return ArrayUtils.EMPTY_STRING_ARRAY;
					}
				})
				.collect(Collectors.toList());

		// all the tasks for the adaptive credal survey
		final List<Callable<String[]>> credal4x4TasksAdaptive = bayesian4x4x4Students.stream()
				.map(student -> (Callable<String[]>) () -> {
					try {
						System.out.println("Credal4x4" + student.getId());

						final AgentTeacher teacher = new Teacher<>(
								new Credal4Skill4Difficulty5Question(N_QUESTIONS),
								new ScoringFunctionUpperExpectedEntropy(),
//								new StoppingConditionCredalMeanEntropy(.5),
								new StoppingConditionQuestionNumber<>(20)
						)
								.setPersist(new PersistCredal());

						new Experiment(teacher, student).run();

						String posteriors = student.getId() + "," + teacher.getResults();
						String answers = student.getAnswers(teacher.getTotalNumberQuestions());
						String profiles = student.getProfiles(teacher.getTotalNumberQuestions());

						return new String[]{posteriors, answers, profiles};
					} catch (Exception e) {
						e.printStackTrace();
						return ArrayUtils.EMPTY_STRING_ARRAY;
					}
				})
				.collect(Collectors.toList());

		final List<Callable<String[]>> credalMinimalisticTasksAdaptive = minimalisticStudents.stream()
				.map(student -> (Callable<String[]>) () -> {
					try {
						System.out.println("Credal4x4 minimalistic" + student.getId());

						final AgentTeacher teacher = new Teacher<>(
								new CredalMinimalistic(N_QUESTIONS, .4, .4, .6, .6),
								new ScoringFunctionUpperExpectedEntropy(),
//								new StoppingConditionCredalMeanEntropy(.5),
								new StoppingConditionQuestionNumber<>(20)
						)
								.setPersist(new PersistCredal());

						new Experiment(teacher, student).run();

						String posteriors = student.getId() + "," + teacher.getResults();
						String answers = student.getAnswers(teacher.getTotalNumberQuestions());
						String profiles = student.getProfiles(teacher.getTotalNumberQuestions());

						return new String[]{posteriors, answers, profiles};
					} catch (Exception e) {
						e.printStackTrace();
						return ArrayUtils.EMPTY_STRING_ARRAY;
					}
				})
				.collect(Collectors.toList());


		// submit all the tasks to the ExecutionService
		final List<Future<String[]>> results4x4NonAdaptiveBayesian = es.invokeAll(bayesian4x4x4TasksNonAdaptive);
		final List<Future<String[]>> results4x4AdaptiveBayesian = es.invokeAll(bayesian4x4x4TasksAdaptive);
//		final List<Future<String[]>> results4x4AdaptiveCredal = es.invokeAll(credal4x4TasksAdaptive);

		final List<Future<String[]>> resultsMinimalisticNonAdaptiveBayesian = es.invokeAll(bayesianMinimalisticTasksNonAdaptive);
		final List<Future<String[]>> resultsMinimalisticAdaptiveBayesian = es.invokeAll(bayesianMinimalisticTasksAdaptive);
//		final List<Future<String[]>> resultsMinimalisticAdaptiveCredal = es.invokeAll(credalMinimalisticTasksAdaptive);

		// wait until the end, then shutdown and proceed with the code
		es.shutdown();

		// write the output to file
		String path = "output/";
		writeToFile(path, "bayesProfiles", results4x4NonAdaptiveBayesian, 2);
		writeToFile(path, "minimalisticProfiles", resultsMinimalisticNonAdaptiveBayesian, 2);

		String Bayesian4x4Path = path + "Bayesian4x4/";
		writeToFile(Bayesian4x4Path, "posteriors.non-adaptive", results4x4NonAdaptiveBayesian, 0);
		writeToFile(Bayesian4x4Path, "answers.non-adaptive", results4x4NonAdaptiveBayesian, 1);

		writeToFile(Bayesian4x4Path, "posteriors.bayesian-adaptive", results4x4AdaptiveBayesian, 0);
		writeToFile(Bayesian4x4Path, "answers.bayesian-adaptive", results4x4AdaptiveBayesian, 1);

//		writeToFile(Bayesian4x4Path, "posteriors.credal-adaptive", results4x4AdaptiveCredal, 0);
//		writeToFile(Bayesian4x4Path, "answers.credal-adaptive", results4x4AdaptiveCredal, 1);


		String Minimalistic = path + "Minimalistic/";

		writeToFile(Minimalistic, "posteriors.non-adaptive", resultsMinimalisticNonAdaptiveBayesian, 0);
		writeToFile(Minimalistic, "answers.non-adaptive", resultsMinimalisticNonAdaptiveBayesian, 1);

		writeToFile(Minimalistic, "posteriors.bayesian-adaptive", resultsMinimalisticAdaptiveBayesian, 0);
		writeToFile(Minimalistic, "answers.bayesian-adaptive", resultsMinimalisticAdaptiveBayesian, 1);

//		writeToFile(Minimalistic, "posteriors.credal-adaptive", resultsMinimalisticAdaptiveCredal, 0);
//		writeToFile(Minimalistic, "answers.credal-adaptive", resultsMinimalisticAdaptiveCredal, 1);
	}

	static void writeToFile(String path, String filename, List<Future<String[]>> content, int idx) throws Exception {
		final List<String> lines = content.stream()
				.map(x -> {
					try {
						// wait for the task to finish (should already be completed) and get the returned row
						return x.get()[idx];
					} catch (InterruptedException | ExecutionException e) {
						// if something bad happens, erase the line
						e.printStackTrace();
						return "";
					}
				})
				// ignore empty lines
				.filter(x -> !x.isEmpty())
				.collect(Collectors.toList());

		// just dump everything to a file
		new File(path).mkdirs();
		Files.write(Paths.get(path + filename + ".csv"), lines);
	}
}
