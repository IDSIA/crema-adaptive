package ch.idsia.crema.adaptive.experiments;

import ch.idsia.crema.adaptive.experiments.agents.AgentTeacher;
import ch.idsia.crema.adaptive.experiments.agents.Student;
import ch.idsia.crema.adaptive.experiments.agents.Teacher;
import ch.idsia.crema.adaptive.experiments.model.imprecise.Credal4x4x4;
import ch.idsia.crema.adaptive.experiments.model.precise.Bayesian4x4x4;
import ch.idsia.crema.adaptive.experiments.persistence.PersistBayesian;
import ch.idsia.crema.adaptive.experiments.persistence.PersistCredal;
import ch.idsia.crema.adaptive.experiments.scoring.imprecise.ScoringFunctionCredalMode;
import ch.idsia.crema.adaptive.experiments.scoring.imprecise.ScoringFunctionUpperExpectedEntropy;
import ch.idsia.crema.adaptive.experiments.scoring.imprecise.ScoringFunctionUpperLowerProbabilityOfRight;
import ch.idsia.crema.adaptive.experiments.scoring.precise.ScoringFunctionBayesianMode;
import ch.idsia.crema.adaptive.experiments.scoring.precise.ScoringFunctionExpectedEntropy;
import ch.idsia.crema.adaptive.experiments.scoring.precise.ScoringFunctionProbabilityOfRight;
import ch.idsia.crema.adaptive.experiments.scoring.precise.ScoringFunctionRandom;
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
public class AdaptiveSurvey4x4x4Simulation {

	// we will generate samples up to 256 students
	static final int N_STUDENTS = 256;
	// we are going to use a model with 20 questions: each template has 5 questions
	static final int N_QUESTIONS = 10;
	// since we are using an ExecutorService, we will run 16 tests in parallel
	static final int PARALLEL_COUNT = 16;

	public static void main(String[] args) throws Exception {

		// General model for sampling the students. Note that teacher and
		// students model need only to have the same questions with the same
		// variable index (this automatically force the need of the same number
		// of skills)

		final Bayesian4x4x4 bayesian4x4x4 = new Bayesian4x4x4(N_QUESTIONS);

		// Note on sampling:
		// The random (seed) management is done by crema. For repeatable
		// experiments, save the sampling to a file or sed a seed in
		// RandomUtil#setRandom
		final Random random = new Random(42);
		RandomUtil.setRandom(random);

		final BayesianNetworkSampling bns = new BayesianNetworkSampling();
		final TIntIntMap[] samples = bns.samples(bayesian4x4x4.getModel(), N_STUDENTS);

		// Creates students based on samples: note that we are not using an
		// AnswerStrategy object since we don't need it
		final List<Student<BayesianFactor>> students = IntStream.range(0, N_STUDENTS)
				.mapToObj(id -> new Student<BayesianFactor>(id, samples[id]))
				.collect(Collectors.toList());

		// We will use our lovely ExecutorService
		ExecutorService es = Executors.newFixedThreadPool(PARALLEL_COUNT);

		// Bayesian non adaptive survey
		final List<Callable<String[]>> bayesian4x4x4TasksNonAdaptive = students.stream()
				.map(student -> (Callable<String[]>) () -> {
					// all these tasks are similar: check bayesian experiments for comments!
					try {
						System.out.println("Bayesian4x4x4 non adaptive " + student.getId());

						final AgentTeacher teacher = new Teacher<>(
								new Bayesian4x4x4(N_QUESTIONS),
								new ScoringFunctionRandom(student.getId())
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

		// Bayesian adaptive survey
		final List<Callable<String[]>> bayesian4x4x4TasksAdaptiveEntropy = students.stream()
				.map(student -> (Callable<String[]>) () -> {
					try {
						System.out.println("Bayesian4x4x4 adaptive + Entropy " + student.getId());

						/*
							build a teacher for each student since we are in a concurrent environment and the teacher
							will save the output results of a single student
						 */
						final AgentTeacher teacher = new Teacher<>(
								// model to use for the question choice
								new Bayesian4x4x4(N_QUESTIONS),
								// scoring function used to select the next question
								new ScoringFunctionExpectedEntropy()
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

		final List<Callable<String[]>> bayesian4x4x4TasksAdaptiveMode = students.stream()
				.map(student -> (Callable<String[]>) () -> {
					try {
						System.out.println("Bayesian4x4x4 adaptive + Mode " + student.getId());

						/*
							build a teacher for each student since we are in a concurrent environment and the teacher
							will save the output results of a single student
						 */
						final AgentTeacher teacher = new Teacher<>(
								// model to use for the question choice
								new Bayesian4x4x4(N_QUESTIONS),
								// scoring function used to select the next question
								new ScoringFunctionBayesianMode()
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

		final List<Callable<String[]>> bayesian4x4x4TasksAdaptivePRight = students.stream()
				.map(student -> (Callable<String[]>) () -> {
					try {
						System.out.println("Bayesian4x4x4 adaptive + PRight " + student.getId());

						/*
							build a teacher for each student since we are in a concurrent environment and the teacher
							will save the output results of a single student
						 */
						final AgentTeacher teacher = new Teacher<>(
								// model to use for the question choice
								new Bayesian4x4x4(N_QUESTIONS),
								// scoring function used to select the next question
								new ScoringFunctionProbabilityOfRight()
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

		// Credal adaptive survey
		final List<Callable<String[]>> credal4x4x4TasksAdaptiveEntropy = students.stream()
				.map(student -> (Callable<String[]>) () -> {
					try {
						System.out.println("Credal4x4x4 adaptive + Entropy " + student.getId());

						final AgentTeacher teacher = new Teacher<>(
								new Credal4x4x4(N_QUESTIONS),
								new ScoringFunctionUpperExpectedEntropy()
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

		final List<Callable<String[]>> credal4x4x4TasksAdaptiveMode = students.stream()
				.map(student -> (Callable<String[]>) () -> {
					try {
						System.out.println("Credal4x4x4 adaptive + Mode " + student.getId());

						final AgentTeacher teacher = new Teacher<>(
								new Credal4x4x4(N_QUESTIONS),
								new ScoringFunctionCredalMode()
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

		final List<Callable<String[]>> credal4x4x4TasksAdaptivePRight = students.stream()
				.map(student -> (Callable<String[]>) () -> {
					try {
						System.out.println("Credal4x4x4 adaptive + PRight " + student.getId());

						final AgentTeacher teacher = new Teacher<>(
								new Credal4x4x4(N_QUESTIONS),
								new ScoringFunctionUpperLowerProbabilityOfRight()
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
//		final List<Future<String[]>> resultsBayesianNonAdaptive = es.invokeAll(bayesian4x4x4TasksNonAdaptive);
//		final List<Future<String[]>> resultsBayesianAdaptiveEntropy = es.invokeAll(bayesian4x4x4TasksAdaptiveEntropy);
//		final List<Future<String[]>> resultsBayesianAdaptiveMode = es.invokeAll(bayesian4x4x4TasksAdaptiveMode);
//		final List<Future<String[]>> resultsBayesianAdaptivePRight = es.invokeAll(bayesian4x4x4TasksAdaptivePRight);

//		final List<Future<String[]>> resultsCredalAdaptiveEntropy = es.invokeAll(credal4x4x4TasksAdaptiveEntropy);
//		final List<Future<String[]>> resultsCredalAdaptiveMode = es.invokeAll(credal4x4x4TasksAdaptiveMode);
		final List<Future<String[]>> resultsCredalAdaptivePRight = es.invokeAll(credal4x4x4TasksAdaptivePRight);

		// wait until the end, then shutdown and proceed with the code
		es.shutdown();

		// write the output to file
		String Bayesian4x4Path = "output/Bayesian4x4x4/";
//		writeToFile(Bayesian4x4Path, "Bayesian4x4x4.profiles", resultsBayesianNonAdaptive, 2);

		// Bayesian
//		writeToFile(Bayesian4x4Path, "Bayesian4x4x4.posteriors.bayesian-non-adaptive", resultsBayesianNonAdaptive, 0);
//		writeToFile(Bayesian4x4Path, "Bayesian4x4x4.answers.bayesian-non-adaptive", resultsBayesianNonAdaptive, 1);

//		writeToFile(Bayesian4x4Path, "Bayesian4x4x4.posteriors.bayesian-adaptive-entropy", resultsBayesianAdaptiveEntropy,	0);
//		writeToFile(Bayesian4x4Path, "Bayesian4x4x4.answers.bayesian-adaptive-entropy", resultsBayesianAdaptiveEntropy, 1);

//		writeToFile(Bayesian4x4Path, "Bayesian4x4x4.posteriors.bayesian-adaptive-mode", resultsBayesianAdaptiveMode,	0);
//		writeToFile(Bayesian4x4Path, "Bayesian4x4x4.answers.bayesian-adaptive-mode", resultsBayesianAdaptiveMode, 1);
//
//		writeToFile(Bayesian4x4Path, "Bayesian4x4x4.posteriors.bayesian-adaptive-pright", resultsBayesianAdaptivePRight, 0);
//		writeToFile(Bayesian4x4Path, "Bayesian4x4x4.answers.bayesian-adaptive-pright", resultsBayesianAdaptivePRight, 1);
//
//		// Credal
//		writeToFile(Bayesian4x4Path, "Bayesian4x4x4.posteriors.credal-adaptive-entropy", resultsCredalAdaptiveEntropy, 0);
//		writeToFile(Bayesian4x4Path, "Bayesian4x4x4.answers.credal-adaptive-entropy", resultsCredalAdaptiveEntropy, 1);
//
//		writeToFile(Bayesian4x4Path, "Bayesian4x4x4.posteriors.credal-adaptive-mode", resultsCredalAdaptiveMode, 0);
//		writeToFile(Bayesian4x4Path, "Bayesian4x4x4.answers.credal-adaptive-mode", resultsCredalAdaptiveMode, 1);

		writeToFile(Bayesian4x4Path, "Bayesian4x4x4.posteriors.credal-adaptive-pright", resultsCredalAdaptivePRight, 0);
		writeToFile(Bayesian4x4Path, "Bayesian4x4x4.answers.credal-adaptive-pright", resultsCredalAdaptivePRight, 1);
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
