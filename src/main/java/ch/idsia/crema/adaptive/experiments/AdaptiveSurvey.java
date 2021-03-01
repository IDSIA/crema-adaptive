package ch.idsia.crema.adaptive.experiments;

import ch.idsia.crema.adaptive.experiments.agents.AgentTeacher;
import ch.idsia.crema.adaptive.experiments.agents.Student;
import ch.idsia.crema.adaptive.experiments.agents.Teacher;
import ch.idsia.crema.adaptive.experiments.model.imprecise.CredalMinimalistic;
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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.*;
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
	static final int N_QUESTIONS = 20;
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
				.mapToObj(id -> new Student<BayesianFactor>(id, samples[id]))
				.collect(Collectors.toList());

		// we will use our lovely ExecutorService
		ExecutorService es = Executors.newFixedThreadPool(PARALLEL_COUNT);

		// all the tasks for the non adaptive survey
		final List<Callable<String>> tasksNonAdaptive = students.stream()
				.map(student -> (Callable<String>) () -> {
					// all these tasks are similar: check bayesian experiments for comments!
					try {
						System.out.println("Student non adaptive " + student.getId());

						final AgentTeacher teacher = new Teacher<>(
								new BayesianMinimalistic(N_QUESTIONS, .4, .6),
								new ScoringFunctionRandom(student.getId()),
								new StoppingConditionQuestionNumber<>(10)
						)
								.setPersist(new PersistBayesian());

						new Experiment(teacher, student).run();

						return student.getId() + "," + teacher.getNumberQuestionsDone() + "," + teacher.getResults();
					} catch (Exception e) {
						e.printStackTrace();
						return "";
					}
				})
				.collect(Collectors.toList());

		// all the tasks for the adaptive bayesian survey
		final List<Callable<String>> tasksAdaptiveBayesian = students.stream()
				.map(student -> (Callable<String>) () -> {
					try {
						System.out.println("Student Bayesian " + student.getId());

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
								new StoppingConditionBayesianMeanEntropy(.3),
								// second stopping criteria: stop after 10 questions
								new StoppingConditionQuestionNumber<>(10)
						)
								// we want to save the results and they are of bayesian type
								.setPersist(new PersistBayesian());

						// run new configured experiment
						new Experiment(teacher, student).run();

						// return a row for the CSV file
						return student.getId() + "," + teacher.getNumberQuestionsDone() + "," + teacher.getResults();
					} catch (Exception e) {
						// if something goes wrong, return an empty row that will be filtered out
						e.printStackTrace();
						return "";
					}
				})
				.collect(Collectors.toList());

		// all the tasks for the adaptive credal survey
		final List<Callable<String>> tasksAdaptiveCredal = students.stream()
				.map(student -> (Callable<String>) () -> {
					try {
						System.out.println("Student Credal " + student.getId());

						final AgentTeacher teacher = new Teacher<>(
								new CredalMinimalistic(N_QUESTIONS, .4, .4, .6, .6),
								new ScoringFunctionCredalMode(),
								new StoppingConditionCredalMeanEntropy(.5),
								new StoppingConditionQuestionNumber<>(10)
						)
								.setPersist(new PersistCredal());

						new Experiment(teacher, student).run();

						return student.getId() + "," + teacher.getNumberQuestionsDone() + "," + teacher.getResults();
					} catch (Exception e) {
						e.printStackTrace();
						return "";
					}
				})
				.collect(Collectors.toList());


		// submit all the tasks to the ExecutionService
		final List<Future<String>> resultsNonAdaptive = es.invokeAll(tasksNonAdaptive);
		final List<Future<String>> resultsAdaptiveBayesian = es.invokeAll(tasksAdaptiveBayesian);
		final List<Future<String>> resultsAdaptiveCredal = es.invokeAll(tasksAdaptiveCredal);

		// wait until the end, then shutdown and proceed with the code
		es.shutdown();

		// write the output to file
		writeToFile("output.non-adaptive.csv", resultsNonAdaptive);
		writeToFile("output.bayesian-adaptive.csv", resultsAdaptiveBayesian);
		writeToFile("output.credal-adaptive.csv", resultsAdaptiveCredal);
	}

	static void writeToFile(String filename, List<Future<String>> content) throws Exception {
		final List<String> lines = content.stream()
				.map(x -> {
					try {
						// wait for the task to finish (should already be completed) and get the returned row
						return x.get();
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
		Files.write(Paths.get(filename), lines);
	}

}
