package ch.idsia.crema.adaptive.experiments;

import ch.idsia.crema.adaptive.experiments.agents.AgentTeacher;
import ch.idsia.crema.adaptive.experiments.agents.Student;
import ch.idsia.crema.factor.bayesian.BayesianFactor;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    12.05.2021 15:41
 */
public class ExperimentSuite {

	private final String path;
	private final String modelName;
	private final String expName;
	private final List<Student<BayesianFactor>> students;
	private final Supplier<AgentTeacher> teacher;
	private final int[] outIndex;

	public ExperimentSuite(String path, String modelName, String expName, List<Student<BayesianFactor>> students, Supplier<AgentTeacher> teacher, int... outIndex) {
		this.path = path;
		this.modelName = modelName;
		this.expName = expName;
		this.students = students;
		this.teacher = teacher;
		this.outIndex = outIndex;
	}

	public void run(int PARALLEL_COUNT) throws Exception {
		ExecutorService es = Executors.newFixedThreadPool(PARALLEL_COUNT);

		final List<Callable<String[]>> tasks = students.stream()
				.map(student -> (Callable<String[]>) () -> {
					// all these tasks are similar: check bayesian experiments for comments!
					System.out.printf("%s %s %d%n", modelName, expName, student.getId());
					return new Experiment(teacher.get(), student).run();
				})
				.collect(Collectors.toList());

		final long start = System.currentTimeMillis();
		final List<Future<String[]>> content = es.invokeAll(tasks);
		final long end = System.currentTimeMillis();

		es.shutdown();

		final long seconds = end - start / 1000;
		System.out.printf("%s %s elapsed time : %d (avg:%.4f)%n", modelName, expName, seconds, 1.0 * seconds / students.size());

		for (int i : outIndex) {
			writeToFile(content, i);
		}
	}

	private String iName(int i) {
		switch (i) {
			case 0:
				return "posteriors";
			case 1:
				return "answers";
			case 2:
				return "profiles";
			case 3:
				return "progress";
			default:
				return "";
		}
	}

	private void writeToFile(List<Future<String[]>> content, int idx) throws Exception {
		final List<String> lines = content.stream()
				.map(x -> {
					try {
						// wait for the task to finish (should already be completed) and get the returned row
						return x.get()[idx];
					} catch (Exception e) {
						// if something bad happens, erase the line
						e.printStackTrace();
						return "";
					}
				})
				// ignore empty lines
				.filter(x -> !x.isEmpty())
				.collect(Collectors.toList());

		// just dump everything to a file
		if (!new File(path).mkdirs())
			System.err.println("Could not create output folder: " + path);

		Files.write(Paths.get(String.format("%s%s.%s.%s.csv", path, modelName, iName(idx), expName)), lines);
	}
}
