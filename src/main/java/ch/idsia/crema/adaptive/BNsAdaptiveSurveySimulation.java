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
import java.util.stream.DoubleStream;

/**
 * Author:  Giorgia Adorni
 * Date:    8.1.2021 15:50
 */
public class BNsAdaptiveSurveySimulation {

    // Adaptive configuration data -------------------------------------------------------------------------------------
    private static final String bayesianFileName = "adaptive/cnParametersBayes.txt";

    private static final int nSkills = 4;
    private static final int nDifficultyLevels = 4;
    private static final int nSkillLevels = 4;
    private static final int states = nDifficultyLevels;

    private static final long randomSeed = 42;

    // Define 16 students that correspond to the 16 possible profiles, that are
    // the combination of the 4 nSkillLevels of each skill
    private final int student;
    // First id, inclusive
    private static final int maxStudent = (int) Math.pow(nSkills, nDifficultyLevels); // Last id, exclusive
    private final int[] profile;
    private final int[] studentAnswers;

    // Minimum value of entropy to stop the survey.
    private static final double STOP_THRESHOLD = 0.25;

    // List containing the number of right and wrong answer to the questions,
    // for each combination of skill and difficulty level
    // {{0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}}
    private final double[][] rightQ = new double[nSkills][nDifficultyLevels];
    private final double[][] wrongQ = new double[nSkills][nDifficultyLevels];

    // Probabilities
    private final double[][][] priorResults = new double[nSkills][][];
    private final double[][][] hypotheticalPosteriorResults = new double[nSkills][][];

    private final double[][][][] answerLikelihood = new double[nSkills][][][];

    private final AdaptiveTests AdaptiveTests;
    private final AbellanEntropy abellanEntropy;
    private final QuestionSet questionSet;
    private final Random random;

    /**
     * Create a survey test for a single student. Each students will have its lists of questions, its personal test,
     * and its answer sheet.
     *
     * @param student reference id of the student
     * @param profile profile of the student
     */
    private BNsAdaptiveSurveySimulation(int student, int[] profile) {
        this.student = student;
        this.profile = profile;

        random = new Random(randomSeed + student);

        AdaptiveTests = new AdaptiveTests();
        abellanEntropy = new AbellanEntropy();
        questionSet = new QuestionSet();
        questionSet.loadKeyList();

        studentAnswers = new int[questionSet.getQuestionNum()];
        Arrays.fill(studentAnswers, -1);
    }

    public static void main(String[] args) {

        final int numOfSimulations = 5;

        // for each student
        final int[][] profiles = new int[maxStudent][nSkillLevels];

        final Quaternary quaternary = new Quaternary(nSkillLevels);
        quaternary.generate(profiles);

        final List<int[]> profilesList = Arrays.asList(profiles);

//        final List<int[]> newProfilesList = profilesList.subList(0, 1);

        //  Loop that iterate over 5/10 simulations for each profile
        for (int s = 0; s < numOfSimulations; s++) {
            final File simDir = new File("output/adaptive_entropy/sim_" + s);
            simDir.mkdirs();

            final Path answersPath = Paths.get(simDir + "/answers.txt");
            final Path initProfilePath = Paths.get(simDir + "/initial_profiles.txt");
            final Path finalProfilePath = Paths.get(simDir + "/predicted_profiles.txt");
            final Path posteriorProfilePath = Paths.get(simDir + "/posterior.txt");

            profilesList.parallelStream().forEach(profile -> {
                try {
                    int student = profilesList.indexOf(profile);
                    long start = System.currentTimeMillis();
                    System.out.printf("Started for student %d with profile %s %n", student,
                            ArrayUtils.toString(profile));

                    BNsAdaptiveSurveySimulation aslat = new BNsAdaptiveSurveySimulation(student, profile);
                    aslat.test();

                    // Parse the outputs of the test
                    String[] studentAnswers = Arrays.stream(aslat.studentAnswers)
                            .mapToObj(String::valueOf)
                            .toArray(String[]::new);

                    String[][] priorResultsS = new String[aslat.priorResults.length][];
                    int[][] priorResultsD = new int[aslat.priorResults.length][];
                    String[] finalProfile = new String[profile.length];

                    for (int i = 0; i < priorResultsS.length; i++) {
                        priorResultsS[i] = Arrays.stream(aslat.priorResults[i][0])
                                .map(x -> Math.round(x * Math.pow(10,2)) / Math.pow(10,2))
                                .mapToObj(String::valueOf)
                                .toArray(String[]::new);

                        priorResultsD[i] = Arrays.stream(aslat.priorResults[i][0])
                                .map(x -> Math.round(x * Math.pow(10,2)))
                                .mapToInt(x -> (int)x)
                                .toArray();

                        finalProfile[i] = Integer.toString(Objects.requireNonNull(
                                Case.CaseUtils.findHighestValue1D(priorResultsD[i])).getRow());
                    }

                    String[] posteriorResults = Arrays.stream(priorResultsS)
                            .flatMap(Arrays::stream)
                            .toArray(String[]::new);

                    String[] initProfile = Arrays.stream(profile).mapToObj(String::valueOf).toArray(String[]::new);

                    // Append the outputs to file
                    synchronized (BNsAdaptiveSurveySimulation.class) {
                        appendToFile(studentAnswers, answersPath);
                        appendToFile(initProfile, initProfilePath);
                        appendToFile(finalProfile, finalProfilePath);
                        appendToFile(posteriorResults, posteriorProfilePath);
                    }
                    long end = System.currentTimeMillis();
                    float msec = end - start;
                    float sec = msec/1000F;
                    float minutes = sec/60F;

                    System.out.printf("Finished for student %d%n in %.4f", student, minutes);

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
        while (true){
            // search for next question using
            double maxIG = 0.0;
            int nextSkill = -1;
            int nextDifficultyLevel = -1;
            int[][] nextSkillAndLevelRank = new int[nSkills][nDifficultyLevels];
            int incrementRank = 1;

            for (int s = 0; s < nSkills; s++) {
                // Current prior
                Object[] testOutput = AdaptiveTests.germanTest(bayesianFileName, s, rightQ, wrongQ);
                priorResults[s] = (double[][]) testOutput[0];
                answerLikelihood[s] = (double[][][]) testOutput[1];

                for (int dl = 0; dl < nDifficultyLevels; dl++) {
                    if (Math.abs(priorResults[s][0][dl] - priorResults[s][1][dl]) >= 0.000001) {
                        System.err.println("Different lower and upper in priors!! " + priorResults[s][0][dl] + ", " +
                                priorResults[s][1][dl]);
                        break;
                    }
                }

                // Entropy of the skill
                double HS = H(priorResults[s][0]);

                for (int dl = 0; dl < nDifficultyLevels; dl++) {
                    List<Integer> availableQuestions = questionSet.getQuestions(s, dl);

                    // Compute entropy only if we have questions available
                    if (availableQuestions.size() == 0) {
                        continue;
                    }

                    double[] HResults = new double[2];
                    // Simulate the two possible outcomes:
                    // in the first iteration of the loop answer wrong,
                    // in the second answer right
                    for (int answer = 0; answer < 2; answer++) {
                        if (answer == 0) {
                            wrongQ[s][dl] += 1;
                        } else {
                            rightQ[s][dl] += 1;
                        }

                        testOutput = AdaptiveTests.germanTest(bayesianFileName, s, rightQ, wrongQ);
                        hypotheticalPosteriorResults[s] = (double[][]) testOutput[0];

                        computeEntropy(hypotheticalPosteriorResults[s], HResults, answer);

                        // clear
                        if (answer == 0) {
                            wrongQ[s][dl] -= 1;
                        } else {
                            rightQ[s][dl] -= 1;
                        }
                    }

                    double rightAnswerProbability = 0;
                    double wrongAnswerProbability = 0;

                    for (int sl = 0; sl < nSkillLevels; sl++) {
                        rightAnswerProbability += answerLikelihood[s][dl][sl][0] * priorResults[s][0][sl];
                        wrongAnswerProbability += (1 - answerLikelihood[s][dl][sl][0]) * priorResults[s][0][sl];
                    }

                    double sumProbs = rightAnswerProbability + wrongAnswerProbability;

                    if (Math.abs(1.0 - sumProbs) >= 0.000001) {
                        System.err.println("Sum of probabilities not 1 -> " + sumProbs);
                    }

                    double H = HResults[0] * wrongAnswerProbability + HResults[1] * rightAnswerProbability;

                    double ig = HS - H;

                    // Decide the optimal pair skill and level that will be used to choose the questions
                    if (ig > maxIG) {
                        maxIG = ig;
                        nextSkillAndLevelRank[s][dl] += incrementRank;
                        incrementRank++;
                    }
                }
            }

            ArrayList<ArrayList<Integer>> nextSkillsAndLevels = new ArrayList<>();
            boolean iterate = true;

            while (iterate) {
                Case highestValue = Case.CaseUtils.findHighestValue2D(nextSkillAndLevelRank);

                if (highestValue == null | highestValue.getValue() == 0) {
                    iterate = false;
                } else {
                    ArrayList<Integer> nextSkillAndLevel = new ArrayList<>();

                    nextSkillAndLevel.add(highestValue.getRow());
                    nextSkillAndLevel.add(highestValue.getCol());
                    nextSkillsAndLevels.add(nextSkillAndLevel);

                    nextSkillAndLevelRank[highestValue.getRow()][highestValue.getCol()] = 0;
                }
            }

            if (maxIG == Double.MAX_VALUE) {
                System.err.println("No min entropy found! (maxIG = " + maxIG);
                break;
            }

            //  Lets ask 80 questions:
            //  5 questions for each skill and difficulty level,
            //  in total, 20 easy, 20 medium-easy, 20 medium-hard, 20 hard
            List<Integer> availableQuestions = null;

            try {
                for (ArrayList<Integer> skillAndLevel : nextSkillsAndLevels) {
                    nextSkill = skillAndLevel.get(0);
                    nextDifficultyLevel = skillAndLevel.get(1);

                    availableQuestions = questionSet.getQuestions(nextSkill, nextDifficultyLevel);

                    if (availableQuestions.size() > 0) {
                        break;
                    }
                }
                if (Objects.requireNonNull(availableQuestions).size() <= 0) {
                    availableQuestions = questionSet.getRemainingQuestions();
                }
            } catch (NullPointerException e) {
                nextSkill = -1;
                nextDifficultyLevel = -1;
                availableQuestions = questionSet.getRemainingQuestions();
            }

            int indexQ = random.nextInt(availableQuestions.size());
            int indexA = availableQuestions.get(indexQ);

            ArrayList<Integer> nextSkillAndLevel = questionSet.getKeys(indexA);

            if (nextSkill == -1 & nextDifficultyLevel == -1) {
                nextSkill = nextSkillAndLevel.get(0);
                nextDifficultyLevel = nextSkillAndLevel.get(1);
            } else {
                assert (nextSkill == nextSkillAndLevel.get(0));
                assert (nextDifficultyLevel == nextSkillAndLevel.get(1));
            }

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
            questionSet.removeQuestion();
            questionSet.addAskedQuestion();

            if (answer == 0) {
                wrongQ[nextSkill][nextDifficultyLevel] += 1;
            } else {
                rightQ[nextSkill][nextDifficultyLevel] += 1;
            }

            // All questions done
            if (questionSet.isEmpty()) {
                break;
            }
        }
    }

    private void computeEntropy(double[][] results, double[] HResults, int r) {
        if (DoubleStream.of(results[0]).sum() > 1 - 10E-15) {
            // precise model
            HResults[r] = H(results[0]);
        } else {
            // imprecise model
            double[] maxLocalEntropy = abellanEntropy.getDistrWithMaxEntropy(results[0], results[1]);
            HResults[r] = H(maxLocalEntropy);
        }
    }

    private double H(double[] d) {
        double h = 0.0;

        for (double v : d) {
            double logXv = Math.log(v) / Math.log(BNsAdaptiveSurveySimulation.states);
            h += v * logXv;
        }

        return -h;
    }

    private static void appendToFile(String[] variable, Path outputPath) {

        try (BufferedWriter bw = Files.newBufferedWriter(outputPath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
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
