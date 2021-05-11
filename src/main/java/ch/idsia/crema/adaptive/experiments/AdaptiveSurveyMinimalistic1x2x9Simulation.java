package ch.idsia.crema.adaptive.experiments;

import ch.idsia.crema.adaptive.experiments.agents.AgentTeacher;
import ch.idsia.crema.adaptive.experiments.agents.Student;
import ch.idsia.crema.adaptive.experiments.agents.Teacher;
import ch.idsia.crema.adaptive.experiments.model.imprecise.CredalMinimalistic1x2x9;
import ch.idsia.crema.adaptive.experiments.model.precise.BayesianMinimalistic1x2x9;
import ch.idsia.crema.adaptive.experiments.persistence.PersistBayesian;
import ch.idsia.crema.adaptive.experiments.persistence.PersistCredal;
import ch.idsia.crema.adaptive.experiments.scoring.imprecise.ScoringFunctionCredalMode;
import ch.idsia.crema.adaptive.experiments.scoring.imprecise.ScoringFunctionUpperExpectedEntropy;
import ch.idsia.crema.adaptive.experiments.scoring.imprecise.ScoringFunctionUpperLowerProbabilityOfRight;
import ch.idsia.crema.adaptive.experiments.scoring.precise.ScoringFunctionBayesianMode;
import ch.idsia.crema.adaptive.experiments.scoring.precise.ScoringFunctionExpectedEntropy;
import ch.idsia.crema.adaptive.experiments.scoring.precise.ScoringFunctionProbabilityOfRight;
import ch.idsia.crema.adaptive.experiments.scoring.precise.ScoringFunctionRandom;
import ch.idsia.crema.adaptive.experiments.stopping.imprecise.StoppingConditionCredalMeanEntropy;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.sampling.BayesianNetworkSampling;
import ch.idsia.crema.utility.RandomUtil;
import gnu.trove.map.TIntIntMap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
                .mapToObj(id -> new Student<BayesianFactor>(id, samples[id]))
                .collect(Collectors.toList());

        // We will use our lovely ExecutorService
        ExecutorService es = Executors.newFixedThreadPool(PARALLEL_COUNT);

        // Bayesian non adaptive survey
        final List<Callable<String[]>> bayesianMinimalistic1x2x9TasksNonAdaptive = students.stream()
                .map(student -> (Callable<String[]>) () -> {
                    // all these tasks are similar: check bayesian experiments for comments!
                    System.out.println("Bayesian Minimalistic1x2x9 non adaptive " + student.getId());

                    final AgentTeacher teacher = new Teacher<>(
                            new BayesianMinimalistic1x2x9(N_QUESTIONS),
                            new ScoringFunctionRandom(student.getId())
                    )
                            .setPersist(new PersistBayesian());

                    return new Experiment(teacher, student).run();
                })
                .collect(Collectors.toList());

        // Bayesian adaptive survey
        final List<Callable<String[]>> bayesianMinimalistic1x2x9TasksAdaptiveEntropy = students.stream()
                .map(student -> (Callable<String[]>) () -> {
                    // all these tasks are similar: check bayesian experiments for comments!
                    System.out.println("Bayesian Minimalistic1x2x9 adaptive + Entropy " + student.getId());

                    final AgentTeacher teacher = new Teacher<>(
                            new BayesianMinimalistic1x2x9(N_QUESTIONS),
                            new ScoringFunctionExpectedEntropy()
                    )
                            .setPersist(new PersistBayesian());

                    return new Experiment(teacher, student).run();
                })
                .collect(Collectors.toList());

        final List<Callable<String[]>> bayesianMinimalistic1x2x9TasksAdaptiveMode = students.stream()
                .map(student -> (Callable<String[]>) () -> {
                    // all these tasks are similar: check bayesian experiments for comments!
                    System.out.println("Bayesian Minimalistic1x2x9 adaptive + Mode " + student.getId());

                    final AgentTeacher teacher = new Teacher<>(
                            new BayesianMinimalistic1x2x9(N_QUESTIONS),
                            new ScoringFunctionBayesianMode()
                    )
                            .setPersist(new PersistBayesian());
                    return new Experiment(teacher, student).run();
                })
                .collect(Collectors.toList());

        final List<Callable<String[]>> bayesianMinimalistic1x2x9TasksAdaptivePRight = students.stream()
                .map(student -> (Callable<String[]>) () -> {
                    // all these tasks are similar: check bayesian experiments for comments!
                    System.out.println("Bayesian Minimalistic1x2x9 adaptive + PRight " + student.getId());

                    final AgentTeacher teacher = new Teacher<>(
                            new BayesianMinimalistic1x2x9(N_QUESTIONS),
                            new ScoringFunctionProbabilityOfRight()
                    )
                            .setPersist(new PersistBayesian());
                    return new Experiment(teacher, student).run();
                })
                .collect(Collectors.toList());

        // Credal adaptive survey
        final List<Callable<String[]>> credalMinimalistic1x2x9TasksAdaptiveEntropy = students.stream()
                .map(student -> (Callable<String[]>) () -> {
                    System.out.println("Credal Minimalistic1x2x9 adaptive + Entropy" + student.getId());

                    final AgentTeacher teacher = new Teacher<>(
                            new CredalMinimalistic1x2x9(N_QUESTIONS).setEps(.05),
                            new ScoringFunctionUpperExpectedEntropy(),
                            new StoppingConditionCredalMeanEntropy(.1)
                    )
                            .setPersist(new PersistCredal());

                    return new Experiment(teacher, student).run();
                })
                .collect(Collectors.toList());

        final List<Callable<String[]>> credalMinimalistic1x2x9TasksAdaptiveMode = students.stream()
                .map(student -> (Callable<String[]>) () -> {
                    System.out.println("Credal Minimalistic1x2x9 adaptive + Mode " + student.getId());

                    final AgentTeacher teacher = new Teacher<>(
                            new CredalMinimalistic1x2x9(N_QUESTIONS).setEps(.05),
                            new ScoringFunctionCredalMode(),
                            new StoppingConditionCredalMeanEntropy(.1)
                    )
                            .setPersist(new PersistCredal());
                    return new Experiment(teacher, student).run();
                })
                .collect(Collectors.toList());

        final List<Callable<String[]>> credalMinimalistic1x2x9TasksAdaptivePRight = students.stream()
                .map(student -> (Callable<String[]>) () -> {
                    System.out.println("Credal Minimalistic1x2x9 adaptive + PRight " + student.getId());

                    final AgentTeacher teacher = new Teacher<>(
                            new CredalMinimalistic1x2x9(N_QUESTIONS).setEps(.05),
                            new ScoringFunctionUpperLowerProbabilityOfRight(),
                            new StoppingConditionCredalMeanEntropy(.1)
                    )
                            .setPersist(new PersistCredal());
                    return new Experiment(teacher, student).run();
                })
                .collect(Collectors.toList());

        final List<Callable<String[]>> credalMinimalistic1x2x9TasksAdaptiveEntropyEps01 = students.stream()
                .map(student -> (Callable<String[]>) () -> {
                    System.out.println("Credal Minimalistic1x2x9 adaptive + Entropy" + student.getId());

                    final AgentTeacher teacher = new Teacher<>(
                            new CredalMinimalistic1x2x9(N_QUESTIONS).setEps(.01),
                            new ScoringFunctionUpperExpectedEntropy(),
                            new StoppingConditionCredalMeanEntropy(.1)
                    )
                            .setPersist(new PersistCredal());

                    return new Experiment(teacher, student).run();
                })
                .collect(Collectors.toList());

        final List<Callable<String[]>> credalMinimalistic1x2x9TasksAdaptiveModeEps01 = students.stream()
                .map(student -> (Callable<String[]>) () -> {
                    System.out.println("Credal Minimalistic1x2x9 adaptive + Mode " + student.getId());

                    final AgentTeacher teacher = new Teacher<>(
                            new CredalMinimalistic1x2x9(N_QUESTIONS).setEps(.01),
                            new ScoringFunctionCredalMode(),
                            new StoppingConditionCredalMeanEntropy(.1)
                    )
                            .setPersist(new PersistCredal());
                    return new Experiment(teacher, student).run();
                })
                .collect(Collectors.toList());

        final List<Callable<String[]>> credalMinimalistic1x2x9TasksAdaptivePRightEps01 = students.stream()
                .map(student -> (Callable<String[]>) () -> {
                    System.out.println("Credal Minimalistic1x2x9 adaptive + PRight " + student.getId());

                    final AgentTeacher teacher = new Teacher<>(
                            new CredalMinimalistic1x2x9(N_QUESTIONS).setEps(.01),
                            new ScoringFunctionUpperLowerProbabilityOfRight(),
                            new StoppingConditionCredalMeanEntropy(.1)
                    )
                            .setPersist(new PersistCredal());
                    return new Experiment(teacher, student).run();
                })
                .collect(Collectors.toList());


        // submit all the tasks to the ExecutionService

//        Instant start1 = Instant.now();
        final List<Future<String[]>> resultsBayesianNonAdaptive = es.invokeAll(bayesianMinimalistic1x2x9TasksNonAdaptive);
//        Instant end1 = Instant.now();
//        String timeBayesianNonAdaptive = String.valueOf(Duration.between(start1, end1).getSeconds());

//        Instant start2 = Instant.now();
        final List<Future<String[]>> resultsBayesianAdaptiveEntropy = es.invokeAll(bayesianMinimalistic1x2x9TasksAdaptiveEntropy);
//        Instant end2 = Instant.now();
//        String timeBayesianAdaptiveEntropy = String.valueOf(Duration.between(start2, end2).getSeconds());

//        Instant start3 = Instant.now();
        final List<Future<String[]>> resultsBayesianAdaptiveMode = es.invokeAll(bayesianMinimalistic1x2x9TasksAdaptiveMode);
//        Instant end3 = Instant.now();
//        String timeBayesianAdaptiveMode = String.valueOf(Duration.between(start3, end3).getSeconds());

//        Instant start4 = Instant.now();
        final List<Future<String[]>> resultsBayesianAdaptivePRight = es.invokeAll(bayesianMinimalistic1x2x9TasksAdaptivePRight);
//        Instant end4 = Instant.now();
//        String timeBayesianAdaptivePRight = String.valueOf(Duration.between(start4, end4).getSeconds());

//        Instant start5 = Instant.now();
        final List<Future<String[]>> resultsCredalAdaptiveEntropy = es.invokeAll(credalMinimalistic1x2x9TasksAdaptiveEntropy);
//        Instant end5 = Instant.now();
//        String timeCredalAdaptiveEntropy = String.valueOf(Duration.between(start5, end5).getSeconds());

//        Instant start6 = Instant.now();
        final List<Future<String[]>> resultsCredalAdaptiveMode = es.invokeAll(credalMinimalistic1x2x9TasksAdaptiveMode);
//        Instant end6 = Instant.now();
//        String timeCredalAdaptiveMode = String.valueOf(Duration.between(start6, end6).getSeconds());

//        Instant start7 = Instant.now();
        final List<Future<String[]>> resultsCredalAdaptivePRight = es.invokeAll(credalMinimalistic1x2x9TasksAdaptivePRight);
//        Instant end7 = Instant.now();
//        String timeCredalAdaptivePRight = String.valueOf(Duration.between(start7, end7).getSeconds());

        final List<Future<String[]>> resultsCredalAdaptiveEntropyEps01 = es.invokeAll(credalMinimalistic1x2x9TasksAdaptiveEntropyEps01);
        final List<Future<String[]>> resultsCredalAdaptiveModeEps01 = es.invokeAll(credalMinimalistic1x2x9TasksAdaptiveModeEps01);
        final List<Future<String[]>> resultsCredalAdaptivePRightEps01 = es.invokeAll(credalMinimalistic1x2x9TasksAdaptivePRightEps01);

        // wait until the end, then shutdown and proceed with the code
        es.shutdown();

        // write the output to file
		String path = "output/Minimalistic1x2x9/";

//        String[] lines = {"bayesian-non-adaptive, " + timeBayesianNonAdaptive + "sec",
//                           "bayesian-adaptive-entropy, " + timeBayesianAdaptiveEntropy + "sec",
//                            "bayesian-adaptive-mode, " + timeBayesianAdaptiveMode + "sec",
//                            "bayesian-adaptive-pright, " + timeBayesianAdaptivePRight + "sec",
//                            "credal-adaptive-entropy, " + timeCredalAdaptiveEntropy + "sec",
//                            "credal-adaptive-mode, " + timeCredalAdaptiveMode + "sec",
//                            "credal-adaptive-pright, " + timeCredalAdaptivePRight + "sec"};

//        writeTimeToFile(path, lines);

        // Non-adaptive
        writeToFile(path, "Minimalistic1x2x9.profiles", resultsBayesianNonAdaptive, 2);
        writeToFile(path, "Minimalistic1x2x9.progress", resultsBayesianNonAdaptive, 3);

        // Bayesian
        writeToFile(path, "Minimalistic1x2x9.posteriors.bayesian-non-adaptive", resultsBayesianNonAdaptive, 0);
        writeToFile(path, "Minimalistic1x2x9.answers.bayesian-non-adaptive", resultsBayesianNonAdaptive, 1);
        writeToFile(path, "Minimalistic1x2x9.progress.bayesian-non-adaptive", resultsBayesianNonAdaptive, 3);

        writeToFile(path, "Minimalistic1x2x9.posteriors.bayesian-adaptive-entropy", resultsBayesianAdaptiveEntropy, 0);
        writeToFile(path, "Minimalistic1x2x9.answers.bayesian-adaptive-entropy", resultsBayesianAdaptiveEntropy, 1);
        writeToFile(path, "Minimalistic1x2x9.progress.bayesian-adaptive-entropy", resultsBayesianAdaptiveEntropy, 3);

        writeToFile(path, "Minimalistic1x2x9.posteriors.bayesian-adaptive-mode", resultsBayesianAdaptiveMode, 0);
        writeToFile(path, "Minimalistic1x2x9.answers.bayesian-adaptive-mode", resultsBayesianAdaptiveMode, 1);
        writeToFile(path, "Minimalistic1x2x9.progress.bayesian-adaptive-mode", resultsBayesianAdaptiveMode, 3);

        writeToFile(path, "Minimalistic1x2x9.posteriors.bayesian-adaptive-pright", resultsBayesianAdaptivePRight, 0);
        writeToFile(path, "Minimalistic1x2x9.answers.bayesian-adaptive-pright", resultsBayesianAdaptivePRight, 1);
        writeToFile(path, "Minimalistic1x2x9.progress.bayesian-adaptive-pright", resultsBayesianAdaptivePRight, 3);

        // Credal
        writeToFile(path, "Minimalistic1x2x9.posteriors.credal-adaptive-entropy", resultsCredalAdaptiveEntropy, 0);
        writeToFile(path, "Minimalistic1x2x9.answers.credal-adaptive-entropy", resultsCredalAdaptiveEntropy, 1);
        writeToFile(path, "Minimalistic1x2x9.progress.credal-adaptive-entropy", resultsCredalAdaptiveEntropy, 3);

        writeToFile(path, "Minimalistic1x2x9.posteriors.credal-adaptive-mode", resultsCredalAdaptiveMode, 0);
        writeToFile(path, "Minimalistic1x2x9.answers.credal-adaptive-mode", resultsCredalAdaptiveMode, 1);
        writeToFile(path, "Minimalistic1x2x9.progress.credal-adaptive-mode", resultsCredalAdaptiveMode, 3);

        writeToFile(path, "Minimalistic1x2x9.posteriors.credal-adaptive-pright", resultsCredalAdaptivePRight, 0);
        writeToFile(path, "Minimalistic1x2x9.answers.credal-adaptive-pright", resultsCredalAdaptivePRight, 1);
        writeToFile(path, "Minimalistic1x2x9.progress.credal-adaptive-pright", resultsCredalAdaptivePRight, 3);

        writeToFile(path, "Minimalistic1x2x9.posteriors.credal-adaptive-entropy.eps01", resultsCredalAdaptiveEntropyEps01, 0);
        writeToFile(path, "Minimalistic1x2x9.answers.credal-adaptive-entropy.eps01", resultsCredalAdaptiveEntropyEps01, 1);
        writeToFile(path, "Minimalistic1x2x9.progress.credal-adaptive-entropy.eps01", resultsCredalAdaptiveEntropyEps01, 3);

        writeToFile(path, "Minimalistic1x2x9.posteriors.credal-adaptive-mode.eps01", resultsCredalAdaptiveModeEps01, 0);
        writeToFile(path, "Minimalistic1x2x9.answers.credal-adaptive-mode.eps01", resultsCredalAdaptiveModeEps01, 1);
        writeToFile(path, "Minimalistic1x2x9.progress.credal-adaptive-mode.eps01", resultsCredalAdaptiveModeEps01, 3);

        writeToFile(path, "Minimalistic1x2x9.posteriors.credal-adaptive-pright.eps01", resultsCredalAdaptivePRightEps01, 0);
        writeToFile(path, "Minimalistic1x2x9.answers.credal-adaptive-pright.eps01", resultsCredalAdaptivePRightEps01, 1);
        writeToFile(path, "Minimalistic1x2x9.progress.credal-adaptive-pright.eps01", resultsCredalAdaptivePRightEps01, 3);
    }

    static void writeToFile(String path, String filename, List<Future<String[]>> content, int idx) throws Exception {
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
        new File(path).mkdirs();
        Files.write(Paths.get(path + filename + ".csv"), lines);
    }

    static void writeTimeToFile(String path, String[] lines) throws IOException {
        // just dump everything to a file
        new File(path).mkdirs();
        Files.write(Paths.get(path + "Times" + ".csv"), Arrays.asList(lines));
    }
}
