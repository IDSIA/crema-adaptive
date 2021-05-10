package ch.idsia.crema.adaptive.experiments;

import ch.idsia.crema.adaptive.experiments.agents.AgentTeacher;
import ch.idsia.crema.adaptive.experiments.agents.Student;
import ch.idsia.crema.adaptive.experiments.agents.Teacher;
import ch.idsia.crema.adaptive.experiments.model.imprecise.CredalMinimalistic1x2x1;
import ch.idsia.crema.adaptive.experiments.model.precise.BayesianMinimalistic;
import ch.idsia.crema.adaptive.experiments.persistence.PersistBayesian;
import ch.idsia.crema.adaptive.experiments.persistence.PersistCredal;
import ch.idsia.crema.adaptive.experiments.scoring.imprecise.ScoringFunctionCredalMode;
import ch.idsia.crema.adaptive.experiments.scoring.imprecise.ScoringFunctionUpperExpectedEntropy;
import ch.idsia.crema.adaptive.experiments.scoring.precise.ScoringFunctionBayesianMode;
import ch.idsia.crema.adaptive.experiments.scoring.precise.ScoringFunctionExpectedEntropy;
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

import static ch.idsia.crema.adaptive.experiments.Utils.separator;

/**
 * Authors:  	Giorgia Adorni
 * Project: 	crema-adaptive
 * Date:    	19.02.2021 11:00
 */
public class AdaptiveSurveyMinimalistic1x2x1Simulation {
    // we will generate samples up to 256 students
    static final int N_STUDENTS = 256;
    // we are going to use a model with 20 questions: each template has 5 questions
    static final int N_QUESTIONS = 20;
    // since we are using an ExecutorService, we will run 16 tests in parallel
    static final int PARALLEL_COUNT = 16;

    public static void main(String[] args) throws Exception {

        // General model for sampling the students. Note that teacher and
        // students model need only to have the same questions with the same
        // variable index (this automatically force the need of the same number
        // of skills)

        final BayesianMinimalistic minimalistic1x2x1 = new BayesianMinimalistic(N_QUESTIONS, .4, .6);
        // Note on sampling:
        // The random (seed) management is done by crema. For repeatable
        // experiments, save the sampling to a file or sed a seed in
        // RandomUtil#setRandom
        final Random random = new Random(42);
        RandomUtil.setRandom(random);

        final BayesianNetworkSampling bns = new BayesianNetworkSampling();
        final TIntIntMap[] samples = bns.samples(minimalistic1x2x1.getModel(), N_STUDENTS);

        // Creates students based on samples: note that we are not using an
        // AnswerStrategy object since we don't need it

        final List<Student<BayesianFactor>> students = IntStream.range(0, N_STUDENTS)
                .mapToObj(id -> new Student<BayesianFactor>(id, samples[id]))
                .collect(Collectors.toList());

        // We will use our lovely ExecutorService
        ExecutorService es = Executors.newFixedThreadPool(PARALLEL_COUNT);

        // Bayesian non adaptive survey
        final List<Callable<String[]>> bayesianMinimalistic1x2x1TasksNonAdaptive = students.stream()
                .map(student -> (Callable<String[]>) () -> {
                    // all these tasks are similar: check bayesian experiments for comments!
                    try {
                        System.out.println("Bayesian Minimalistic1x2x1 non adaptive " + student.getId());

                        final AgentTeacher teacher = new Teacher<>(
                                new BayesianMinimalistic(N_QUESTIONS, .4, .6),
                                new ScoringFunctionRandom(student.getId())
                        )
                                .setPersist(new PersistBayesian());

                        new Experiment(teacher, student).run();

                        String posteriors = student.getId() + separator + teacher.getResults();
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
        final List<Callable<String[]>> bayesianMinimalistic1x2x1TasksAdaptiveEntropy = students.stream()
                .map(student -> (Callable<String[]>) () -> {
                    // all these tasks are similar: check bayesian experiments for comments!
                    try {
                        System.out.println("Bayesian Minimalistic1x2x1 adaptive + ExpectedEntropy" + student.getId());

                        final AgentTeacher teacher = new Teacher<>(
                                new BayesianMinimalistic(N_QUESTIONS, .4, .6),
                                new ScoringFunctionExpectedEntropy()
                        )
                                .setPersist(new PersistBayesian());

                        new Experiment(teacher, student).run();

                        String posteriors = student.getId() + separator + teacher.getResults();
                        String answers = student.getAnswers(teacher.getTotalNumberQuestions());
                        String profiles = student.getProfiles(teacher.getTotalNumberQuestions());

                        return new String[]{posteriors, answers, profiles};
                    } catch (Exception e) {
                        e.printStackTrace();
                        return ArrayUtils.EMPTY_STRING_ARRAY;
                    }
                })
                .collect(Collectors.toList());

        final List<Callable<String[]>> bayesianMinimalistic1x2x1TasksAdaptiveMode = students.stream()
                .map(student -> (Callable<String[]>) () -> {
                    // all these tasks are similar: check bayesian experiments for comments!
                    try {
                        System.out.println("Bayesian Minimalistic1x2x1 adaptive + Mode" + student.getId());

                        final AgentTeacher teacher = new Teacher<>(
                                new BayesianMinimalistic(N_QUESTIONS, .4, .6),
                                new ScoringFunctionBayesianMode()
                        )
                                .setPersist(new PersistBayesian());

                        new Experiment(teacher, student).run();

                        String posteriors = student.getId() + separator + teacher.getResults();
                        String answers = student.getAnswers(teacher.getTotalNumberQuestions());
                        String profiles = student.getProfiles(teacher.getTotalNumberQuestions());

                        return new String[]{posteriors, answers, profiles};
                    } catch (Exception e) {
                        e.printStackTrace();
                        return ArrayUtils.EMPTY_STRING_ARRAY;
                    }
                })
                .collect(Collectors.toList());

        // Credal adaptive survey
        final List<Callable<String[]>> credalMinimalistic1x2x1TasksAdaptiveEntropy = students.stream()
                .map(student -> (Callable<String[]>) () -> {
                    try {
                        System.out.println("Credal Minimalistic1x2x1 adaptive + ExpectedEntropy" + student.getId());

                        final AgentTeacher teacher = new Teacher<>(
                                new CredalMinimalistic1x2x1(N_QUESTIONS, .4, .4, .6, .6),
                                new ScoringFunctionUpperExpectedEntropy()
                        )
                                .setPersist(new PersistCredal());

                        new Experiment(teacher, student).run();

                        String posteriors = student.getId() + separator + teacher.getResults();
                        String answers = student.getAnswers(teacher.getTotalNumberQuestions());
                        String profiles = student.getProfiles(teacher.getTotalNumberQuestions());

                        return new String[]{posteriors, answers, profiles};
                    } catch (Exception e) {
                        e.printStackTrace();
                        return ArrayUtils.EMPTY_STRING_ARRAY;
                    }
                })
                .collect(Collectors.toList());

        final List<Callable<String[]>> credalMinimalistic1x2x1TasksAdaptiveMode = students.stream()
                .map(student -> (Callable<String[]>) () -> {
                    try {
                        System.out.println("Credal Minimalistic1x2x1 adaptive + Mode" + student.getId());

                        final AgentTeacher teacher = new Teacher<>(
                                new CredalMinimalistic1x2x1(N_QUESTIONS, .4, .4, .6, .6),
                                new ScoringFunctionCredalMode()
                        )
                                .setPersist(new PersistCredal());

                        new Experiment(teacher, student).run();

                        String posteriors = student.getId() + separator + teacher.getResults();
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
//        final List<Future<String[]>> resultsBayesianNonAdaptive = es.invokeAll(bayesianMinimalistic1x2x1TasksNonAdaptive);
//        final List<Future<String[]>> resultsBayesianAdaptiveEntropy = es.invokeAll(bayesianMinimalistic1x2x1TasksAdaptiveEntropy);
        final List<Future<String[]>> resultsCredalAdaptiveEntropy = es.invokeAll(credalMinimalistic1x2x1TasksAdaptiveEntropy);

//        final List<Future<String[]>> resultsBayesianAdaptiveMode = es.invokeAll(bayesianMinimalistic1x2x1TasksAdaptiveMode);
//        final List<Future<String[]>> resultsCredalAdaptiveMode = es.invokeAll(credalMinimalistic1x2x1TasksAdaptiveMode);

        // wait until the end, then shutdown and proceed with the code
        es.shutdown();

        // write the output to file
        String path = "output/Minimalistic1x2x1/";
//        writeToFile(path, "Minimalistic1x2x1.profiles", resultsBayesianNonAdaptive, 2);

//        writeToFile(path, "Minimalistic1x2x1.posteriors.bayesian-non-adaptive", resultsBayesianNonAdaptive, 0);
//        writeToFile(path, "Minimalistic1x2x1.answers.bayesian-non-adaptive", resultsBayesianNonAdaptive, 1);
//
//        writeToFile(path, "Minimalistic1x2x1.posteriors.bayesian-adaptive-entropy", resultsBayesianAdaptiveEntropy, 0);
//        writeToFile(path, "Minimalistic1x2x1.answers.bayesian-adaptive-entropy", resultsBayesianAdaptiveEntropy, 1);
//        writeToFile(path, "Minimalistic1x2x1.posteriors.bayesian-adaptive-mode", resultsBayesianAdaptiveMode, 0);
//        writeToFile(path, "Minimalistic1x2x1.answers.bayesian-adaptive-mode", resultsBayesianAdaptiveMode, 1);

        writeToFile(path, "Minimalistic1x2x1.posteriors.credal-adaptive-entropy", resultsCredalAdaptiveEntropy, 0);
        writeToFile(path, "Minimalistic1x2x1.answers.credal-adaptive-entropy", resultsCredalAdaptiveEntropy, 1);
//        writeToFile(path, "Minimalistic1x2x1.posteriors.credal-adaptive-mode", resultsCredalAdaptiveMode, 0);
//        writeToFile(path, "Minimalistic1x2x1.answers.credal-adaptive-mode", resultsCredalAdaptiveMode, 1);
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
