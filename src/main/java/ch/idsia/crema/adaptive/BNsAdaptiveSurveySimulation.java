package ch.idsia.crema.adaptive;

import com.google.common.math.Stats;
import org.apache.commons.lang3.ArrayUtils;
import java.util.Random;
import java.io.BufferedWriter;
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
    private static final String[] dataset = {
            "adaptive/Hören2015-16.csv",
            "adaptive/Kommunikation2015-16.csv",
            "adaptive/Lesen2015-16.csv",
            "adaptive/Wortschatz und Strukturen2015-16.csv"
    };

    private static final int nSkills = 4;
    private static final int nDifficultyLevels = 4;
    private static final int nSkillLevels = 4;
    private static final int states = nDifficultyLevels;

    private static final long randomSeed = 42;

    // Define 16 students that correspond to the 16 possible profiles, that are
    // the combination of the 4 nSkillLevels of eack skill
    private final int student;
    private static final int minStudent = 0;  // First id, inclusive
    private static final int maxStudent = (int) Math.pow(nSkills, nDifficultyLevels); // Last id, exclusive
    private final int[] profile;

    // Minimum value of entropy to stop the survey.
    private static final double STOP_THRESHOLD = 0.25;

    // List containing the number of right and wrong answer to the questions,
    // for each combination of skill and difficulty level
    private final double[][] rightQ = new double[nSkills][nDifficultyLevels]; // {{0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}}
    private final double[][] wrongQ = new double[nSkills][nDifficultyLevels]; // {{0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}}

    // Probabilities
    private final double[][][] priorResults = new double[nSkills][][];
    private final double[][][] hypotheticalPosteriorResults = new double[nSkills][][];
    private final double[][][] posteriorResults = new double[nSkills][][];

    private final double[][][][] answerLikelihood = new double[nSkills][][][];

    private double rightAnswerProbability = 0;
    private double wrongAnswerProbability = 0;

    private final AnswerSet[] questionsPerSkill = new AnswerSet[nSkills];

    private final AdaptiveTests AdaptiveTests;
    private final AbellanEntropy abellanEntropy;
    private final QuestionSet questionSet;
    private final Random random;

    private int question = 0;
    private int questionAnswered = 0;

    /**
     * Create a survey test for a single student. Each students will have its lists of questions, its personal test,
     * and its answer sheet.
     *
     * @param student reference id of the students
     */
    private BNsAdaptiveSurveySimulation(int student, int[] profile) {
        this.student = student;
        this.profile = profile;

        random = new Random(randomSeed + student);
        AdaptiveTests = new AdaptiveTests();
        abellanEntropy = new AbellanEntropy();
        questionSet = new QuestionSet();
        questionSet.loadKeyList();

        for (int i = 0; i < questionsPerSkill.length; i++) {
            questionsPerSkill[i] = new AnswerSet().load(dataset[i]);
        }
    }

    public static void main(String[] args) {
        // for each student
        final int[][] profiles = new int[maxStudent][nSkillLevels];

        Quaternary quaternary = new Quaternary(nSkillLevels);
        quaternary.generate(profiles);

        for (int student = minStudent; student < maxStudent; student++) {
            try {
                System.out.printf("Start for student %d with profile %s %n", student,
                        ArrayUtils.toString(profiles[student]));

                //  FIXME add the loop to create 5/10 simulations for each profile
                BNsAdaptiveSurveySimulation aslat = new BNsAdaptiveSurveySimulation(student, profiles[student]);
                aslat.test();

                // Append to file the right and wrong answer count
                final Path right_out_path = Paths.get("output/right_answers-profile_" + student + ".txt");
                final Path wrong_out_path = Paths.get("output/wrong_answers-profile_" + student + ".txt");

                appendToFile(aslat, right_out_path, wrong_out_path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static synchronized void appendToFile(BNsAdaptiveSurveySimulation aslat, Path right_path, Path wrong_path) {
        try (BufferedWriter bw = Files.newBufferedWriter(right_path, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            bw.write(ArrayUtils.toString(aslat.rightQ) + "\n");

        } catch (IOException ignored) {
        }
        try (BufferedWriter bw = Files.newBufferedWriter(wrong_path, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            bw.write(ArrayUtils.toString(aslat.rightQ) + "\n");
        } catch (IOException ignored) {
        }
    }

    /**
     * Perform the adaptive test with the initialized data.
     */
    private void test() {
        boolean stop;
        do {
            // search for next question using
            double maxIG = 0.0;
            int nextSkill = -1;
            int nextDifficultyLevel = -1;

            for (int s = 0; s < nSkills; s++) {
                // Current prior
                Object[] testOutput = AdaptiveTests.germanTest(bayesianFileName, s, rightQ, wrongQ);
                priorResults[s] = (double[][]) testOutput[0];
                answerLikelihood[s] = (double[][][]) testOutput[1];

                // Entropy of the skill
                for (int dl = 0; dl < nDifficultyLevels; dl++) {
                    if (Math.abs(priorResults[s][0][dl] - priorResults[s][1][dl]) >= 0.000001) {
                        System.err.println("Different lower and upper in priors!! " + priorResults[s][0][dl] + ", " +
                                priorResults[s][1][dl]);
                        break;
                    }
                }

                double HS = H(priorResults[s][0]);

                for (int dl = 0; dl < nDifficultyLevels; dl++) {
                    List<Integer> availableQuestions = questionSet.getQuestions(s, dl);

                    // compute entropy only if we have questions available
                    if (availableQuestions.size() == 0) {
                        continue;
                    }

                    double[] HResults = new double[2];
                    // in the first iteration of the loop simulate to answer wrong, in the secondo to answer right
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

                    rightAnswerProbability = 0;
                    wrongAnswerProbability = 0;

                    for (int sl = 0; sl < nSkillLevels; sl++) {
                        rightAnswerProbability += answerLikelihood[s][dl][sl][0] * priorResults[s][0][sl];
                        wrongAnswerProbability += (1 - answerLikelihood[s][dl][sl][0]) * priorResults[s][0][sl];
                    }

                    double sumProbs = rightAnswerProbability + wrongAnswerProbability;

                    if (Math.abs(1.0 - sumProbs) >= 0.000001) {
                        System.err.println("Sum of probabilities not 1 -> " + sumProbs);
                    }

                    double H = HResults[0] * wrongAnswerProbability + HResults[1] * rightAnswerProbability;

                    double ig = HS - H; // infogain

                    if (ig < 0.000001) {
                        System.err.println("Negative information gain for skill " + s + " level " + dl +
                                ": \n IG = HS" + " - H = " + HS + " - " + H + "=" + ig);
                    }

                    if (ig > maxIG) {
                        maxIG = ig;
                        nextSkill = s;
                        nextDifficultyLevel = dl;
                    }
                }
            }

            if (maxIG == Double.MAX_VALUE) {
                System.err.println("No min entropy found! (maxIG = " + maxIG);
                break;
            }

            // TODO:
            //  Lets ask 40 questions,
            //  10 easy, 10 medium-easy, 10 medium-hard, 10 hard
            //  now we aske the same

            // get available questions
            List<Integer> availableQuestions = null;

            try {
                availableQuestions = questionSet.getQuestions(nextSkill, nextDifficultyLevel);
            } catch (NullPointerException e) {
                System.out.print(questionSet);
                System.out.println("NullPointerException Caught\n");
                System.out.println("No more answers available!\n");
                break;
            }

            assert availableQuestions != null;
            int indexQ = random.nextInt(availableQuestions.size());

            // Sample the answer
            Random random = new Random();
            double rd = random.nextDouble();

            //  if the random double sampled 'rd' is higher than
            //  answerLikelihood[nextSkill][nextDifficultyLevel][skillLevel][0]
            //  the answer to the question is true, else false
            int answer = 0;
            if (rd < answerLikelihood[nextSkill][nextDifficultyLevel][profile[nextSkill]][0]) {
                answer = 1;
            }
            System.out.printf("Asked question %d, answer %d%n next skill %d, " +
                              "next difficulty level %d, (H=%.4f)%n", question, answer,
                               nextSkill, nextDifficultyLevel, maxIG);

            questionAnswered++;
            availableQuestions.remove(indexQ);
            questionSet.revomeQuestion();

            if (answer == 0) {
                wrongQ[nextSkill][nextDifficultyLevel] += 1;
            } else {
                rightQ[nextSkill][nextDifficultyLevel] += 1;
            }

            // stop criteria
            stop = true;
            for (int s = 0; s < nSkills; s++) {
                Object[] output = AdaptiveTests.germanTest(bayesianFileName, s, rightQ, wrongQ);
                posteriorResults[s] = (double[][]) output[0];

                // entropy of the skill
                for (int dl = 0; dl < nDifficultyLevels; dl++) {
                    if (Math.abs(posteriorResults[s][0][dl] - posteriorResults[s][1][dl]) >= 0.000001) {
                        System.err.println("Different lower and upper in posteriors!! " + posteriorResults[s][0][dl] +
                                ", " + posteriorResults[s][1][dl]);
                        break;
                    }
                }
                double HS = H(posteriorResults[s][0]);

                if (HS > STOP_THRESHOLD) {
                    System.out.println("HS(s=" + s + ") = " + HS + ", HS > STOP_THRESHOLD, continue");
                    stop = false;
                    break;
                } else {
                    System.out.println("HS(s=" + s + ") = " + HS + ", HS < STOP_THRESHOLD");
                }
            }

            if (questionSet.isEmpty()) {
                System.out.println("All questions done!");
                break;
            }

            question++;

        } while (!stop);
        System.out.println("\n--------------------------------------------\n");

        System.out.printf("Skills probabilities %s%n", ArrayUtils.toString(posteriorResults));
        System.out.printf("Right answers %s%n", ArrayUtils.toString(rightQ));
        System.out.printf("Wrong answers %s%n", ArrayUtils.toString(wrongQ));

        double[] rightA = new double[nSkills];
        double[] totalA = new double[nSkills];
        double[] rightAnswerPercentage = new double[nSkills];

        for (int s = 0; s < nSkills; s++) {
            for (int dl = 0; dl < nDifficultyLevels; dl++) {
                rightA[s] += rightQ[s][dl];
                totalA[s] += rightQ[s][dl] + wrongQ[s][dl];
            }
        }

        for (int s = 0; s < nSkills; s++) {
            rightAnswerPercentage[s] = rightA[s] / totalA[s];
        }

        System.out.printf("Total questions %s%n", ArrayUtils.toString(totalA));
        System.out.printf("Percentage of correct answer %s%n", ArrayUtils.toString(rightAnswerPercentage));
        System.out.printf("Average of correct answer %.2f%%%n", Stats.meanOf(rightAnswerPercentage));
        System.out.println("\n--------------------------------------------\n");
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

    private double[][][] getResults() {
        double[][][] result = new double[nSkills][][];
        Object[] testResult;

        for (int s = 0; s < nSkills; s++) {

            testResult = AdaptiveTests.germanTest(bayesianFileName, s, rightQ, wrongQ);
            result[s] = (double[][]) testResult[0];
        }

        return result;
    }
}
