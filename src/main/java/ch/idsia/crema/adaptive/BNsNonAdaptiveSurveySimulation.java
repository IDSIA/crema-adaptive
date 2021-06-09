package ch.idsia.crema.adaptive;

import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * Author:  Giorgia Adorni
 * Date:    3.2.2021 9:20
 */
public class BNsNonAdaptiveSurveySimulation {

    // Adaptive configuration data -------------------------------------------------------------------------------------
    private static final String bayesianFileName = "adaptive/cnParametersBayes.txt";

    private static final int nSkills = 4;
    private static final int nDifficultyLevels = 4;
    private static final int nSkillLevels = 4;
    private static final int states = nDifficultyLevels;

    private static final long randomSeed = 42;
    private static final double scale = Math.pow(10,2);

    // Define 16 students that correspond to the 16 possible profiles, that are
    // the combination of the 4 nSkillLevels of each skill
    private final int student;
    // First id, inclusive
    private static final int maxStudent = (int) Math.pow(nSkills, nDifficultyLevels); // Last id, exclusive
    private final int[] profile;
    private final int[] studentAnswers;

    // List containing the number of right and wrong answer to the questions,
    // for each combination of skill and difficulty level
    // {{0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}}
    private final double[][] rightQ = new double[nSkills][nDifficultyLevels];
    private final double[][] wrongQ = new double[nSkills][nDifficultyLevels];

    // Probabilities
    private final double[][][] priorResults = new double[nSkills][][];
    private final String[][][] posteriors;
    private final double[][][][] answerLikelihood = new double[nSkills][][][];

    private final Tests Tests;
    private final QuestionSet questionSet;
    private final Random random;

    /**
     * Create a survey test for a single student. Each students will have its lists of questions, its personal test,
     * and its answer sheet.
     *
     * @param student reference id of the student
     * @param profile profile of the student
     */
    private BNsNonAdaptiveSurveySimulation(int student, int[] profile) {
        this.student = student;
        this.profile = profile;

        random = new Random(randomSeed + student);

        Tests = new Tests();
        questionSet = new QuestionSet();
        questionSet.loadKeyList();
        posteriors = new String[questionSet.getQuestionNum()][nSkills][nDifficultyLevels];

        studentAnswers = new int[questionSet.getQuestionNum()];
        Arrays.fill(studentAnswers, -1);
    }

    public static void main(String[] args) {

        final int numOfSimulations = 1;

        // for each student
        final int[][] profiles = new int[maxStudent][nSkillLevels];

        final Quaternary quaternary = new Quaternary(nSkillLevels);
        quaternary.generate(profiles);

        final List<int[]> profilesList = Arrays.asList(profiles);

//        FIXME
//        final List<int[]> _profilesList = profilesList.subList(0, 1);

        //  Loop that iterate over 5/10 simulations for each profile
        for (int s = 0; s < numOfSimulations; s++) {
            final File simDir = new File("output/non_adaptive_entropy/sim_" + s);
            simDir.mkdirs();

            final Path answersPath = Paths.get(simDir + "/answers.txt");
            final Path initProfilePath = Paths.get(simDir + "/initial_profiles.txt");
//            final Path finalProfilePath = Paths.get(simDir + "/predicted_profiles.txt");
            final Path finalPosteriorProfilePath = Paths.get(simDir + "/final_posterior.txt");
            final Path posteriorsProfilePath = Paths.get(simDir + "/posteriors.txt");

            profilesList.parallelStream().forEach(profile -> {
                try {
                    int student = profilesList.indexOf(profile);
                    long start = System.currentTimeMillis();
                    System.out.printf("Started for student %d with profile %s %n", student,
                            ArrayUtils.toString(profile));

                    BNsNonAdaptiveSurveySimulation aslat = new BNsNonAdaptiveSurveySimulation(student, profile);
                    aslat.test();

                    // Parse the outputs of the test
                    String[] studentAnswers = Arrays.stream(aslat.studentAnswers)
                            .mapToObj(String::valueOf)
                            .toArray(String[]::new);

                    String[][] priorResultsS = new String[aslat.priorResults.length][];

                    extractParsedPosteriors(priorResultsS, aslat.priorResults);

                    String[] finalPosteriorResults = Arrays.stream(priorResultsS)
                            .flatMap(Arrays::stream)
                            .toArray(String[]::new);

                    String[] posteriorsResults = Arrays.stream(aslat.posteriors)
                            .flatMap(x -> Arrays.stream(x).flatMap(Arrays::stream))
                            .toArray(String[]::new);

                    String[] initProfile = Arrays.stream(profile).mapToObj(String::valueOf).toArray(String[]::new);

                    // Append the outputs to file
                    synchronized (BNsNonAdaptiveSurveySimulation.class) {
                        appendToFile(student, studentAnswers, answersPath);
                        appendToFile(student, initProfile, initProfilePath);
                        appendToFile(student, finalPosteriorResults, finalPosteriorProfilePath);
                        appendToFile(student, posteriorsResults, posteriorsProfilePath);
                    }
                    long end = System.currentTimeMillis();
                    float msec = end - start;
                    float sec = msec/1000F;
                    float minutes = sec/60F;

                    System.out.printf("%30s %d %s %.2f %s%n", "Finished for student", student, "in", minutes, "minutes");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * Perform the adaptive test with the initialized data.
     */
    private void test() {
        do {
            for (int s = 0; s < nSkills; s++) {
                // Current prior
                Object[] testOutput = Tests.germanTest(bayesianFileName, s, rightQ, wrongQ);
                priorResults[s] = (double[][]) testOutput[0];
                answerLikelihood[s] = (double[][][]) testOutput[1];
            }

            if (questionSet.getAskedQuestion() == 80) {
                System.err.println("BUG");
            }
            extractParsedPosteriors(posteriors[questionSet.getAskedQuestion()], priorResults);

            //  Lets ask 80 questions:
            //  5 questions for each skill and difficulty level,
            //  in total, 20 easy, 20 medium-easy, 20 medium-hard, 20 hard
            List<Integer> availableQuestions = questionSet.getQuestionsFromRemaining();

            int indexQ = random.nextInt(availableQuestions.size());
            int indexA = availableQuestions.get(indexQ);

            int nextSkill = questionSet.getKeys(indexA).get(0);
            int nextDifficultyLevel = questionSet.getKeys(indexA).get(1);

            // Sample the answer
            double rd = random.nextDouble();

            //  if the random double sampled 'rd' is higher than
            //  answerLikelihood[nextSkill][nextDifficultyLevel][skillLevel][0]
            //  the answer to the question is true, else false
            int answer = 0;

            if (rd < answerLikelihood[nextSkill][nextDifficultyLevel][profile[nextSkill]][0]) {
                answer = 1;
            }

            // Save the answer of the student
            studentAnswers[indexA] = answer;

            // Mark the question as answered and remove it from the list of available questions
            availableQuestions.remove(indexQ);

            if (questionSet.getRemainingQuestions().contains(indexA)) {
                System.err.println("BUG");
            }
            if (studentAnswers[indexA] == -1) {
                System.err.println("BUG");
            }

            questionSet.addAskedQuestion();

            if (answer == 0) {
                wrongQ[nextSkill][nextDifficultyLevel] += 1;
            } else {
                rightQ[nextSkill][nextDifficultyLevel] += 1;
            }

        } while (!questionSet.isEmpty());
    }

    private static void extractParsedPosteriors(String[][] posteriorsArray, double[][][] priorsArray) {
        for (int i = 0; i < posteriorsArray.length; i++) {
            posteriorsArray[i] = Arrays.stream(priorsArray[i][0])
                    .map(x -> Math.round(x * BNsNonAdaptiveSurveySimulation.scale) / BNsNonAdaptiveSurveySimulation.scale)
                    .mapToObj(String::valueOf)
                    .toArray(String[]::new);
        }
    }

    private static void appendToFile(int id, String[] variable, Path outputPath) {

        try (BufferedWriter bw = Files.newBufferedWriter(outputPath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            bw.write(id + ", ");
            for (String item : variable){
                bw.write(item + ", ");
            }
            bw.write( "\n");

        } catch (IOException ignored) {
        }
    }

    public int getStudent() {
        return student;
    }

}
