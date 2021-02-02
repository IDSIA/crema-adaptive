package ch.idsia.crema.adaptive;

import com.google.common.math.Stats;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.util.stream.DoubleStream;

/**
 * Author:  Giorgia Adorni
 * Project: Crema
 * Date:    24.11.2020 11:40
 */

public class BNsAdaptiveSurveyTest {

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

    private final double[][][] priorResults = new double[nSkills][][];
    private final double[][][] hypotheticalPosteriorResults = new double[nSkills][][];
    private final double[][][] posteriorResults = new double[nSkills][][];

    private final double[][][][] answerLikelihood = new double[nSkills][][][];

    // Debug variables
    private final double[][][] hypotheticalPosteriorResultsWrong = new double[nSkills][][];
    private final double[][][] hypotheticalPosteriorResultsRight = new double[nSkills][][];

    private final AnswerSet[] questionsPerSkill = new AnswerSet[nSkills];

    private final AdaptiveTests AdaptiveTests;
    private final AbellanEntropy abellanEntropy;
    private final QuestionSet questionSet;
    private final Random random;

    private int question = 0;

    /**
     * Create a survey test for a single student. Each students will have its lists of questions, its personal test,
     * and its answer sheet.
     *
     * @param student reference id of the students
     */
    private BNsAdaptiveSurveyTest(int student, int[] profile) {
        this.student = student;
        this.profile = profile;

        random = new Random(randomSeed + student);

        AdaptiveTests = new AdaptiveTests();
        abellanEntropy = new AbellanEntropy();
        questionSet = new QuestionSet();
        questionSet.loadKeyList();

        studentAnswers = new int[questionSet.getQuestionNum()];
        Arrays.fill(studentAnswers, -1);

        for (int i = 0; i < questionsPerSkill.length; i++) {
            questionsPerSkill[i] = new AnswerSet().load(dataset[i]);
        }
    }

    public static void main(String[] args) {
        // for each student
        final int[][] profiles = new int[maxStudent][nSkillLevels];

        final Quaternary quaternary = new Quaternary(nSkillLevels);
        quaternary.generate(profiles);
        final List<int[]> profilesList = Arrays.asList(profiles);

        profilesList.parallelStream().forEach(profile -> {
            try {
                int student = profilesList.indexOf(profile);
                System.out.printf("Started for student %d with profile %s %n", student,
                        ArrayUtils.toString(profile));

                BNsAdaptiveSurveyTest aslat = new BNsAdaptiveSurveyTest(student, profile);
                aslat.test();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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

                    // compute entropy only if we have questions available
                    if (availableQuestions.size() == 0) {
                        System.out.println("No more question for skill " + s + " level " + dl);
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

                        if (answer == 0) {
                            hypotheticalPosteriorResultsWrong[s] = (double[][]) testOutput[0];
                        } else {
                            hypotheticalPosteriorResultsRight[s] = (double[][]) testOutput[0];
                        }

                        computeEntropy(hypotheticalPosteriorResults[s], HResults, answer);

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

                    double ig = HS - H; // infogain

                    if (ig < 0.0000001) {
                        System.err.println("Negative information gain for skill " + s + " level " + dl +
                                ": \n IG = HS" + " - H = " + HS + " - " + H + "=" + ig);
                    }

                    // Decide the optimal pair skill and level that will be used to choose the questions
                    if (ig > maxIG) {
                        System.out.printf(" ig > maxIG --> Updating maxIG...%n");
                        maxIG = ig;
                        nextSkillAndLevelRank[s][dl] += incrementRank;
                        incrementRank++;
                    }
                }
            }

            ArrayList<ArrayList<Integer>> nextSkillsAndLevels = new ArrayList<>();
            boolean iterate = true;

            while (iterate) {
                Case highestValue = Case.findHighestValue2D(nextSkillAndLevelRank);

                if (highestValue == null | Objects.requireNonNull(highestValue).getValue() == 0) {
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

            // Get available questions
            List<Integer> availableQuestions = null;
            
            try {
                for (ArrayList<Integer> skillAndLevel : nextSkillsAndLevels) {
                    nextSkill = skillAndLevel.get(0);
                    nextDifficultyLevel = skillAndLevel.get(1);

                    availableQuestions = questionSet.getQuestions(nextSkill, nextDifficultyLevel);

                    assert availableQuestions != null;
                    if (availableQuestions.size() > 0) {
                        break;
                    }
                }
                assert availableQuestions != null;
                if (availableQuestions.size() <= 0) {
                    break;
                }
            } catch(NullPointerException e) {
                System.out.print(questionSet);
                System.out.println("NullPointerException caught...\n");
                System.out.println("No more answers available!\n");

                break;
            }

            int indexQ = random.nextInt(availableQuestions.size());
            int indexA = availableQuestions.get(indexQ);
            int answer = questionsPerSkill[nextSkill].getAnswer(student, indexA);
            studentAnswers[indexA] = answer;

            System.out.printf("Asked question %d, answer %d%n " +
                                "Testing skill %d, " +
                                "of difficulty level %d, (H=%.4f)%n", question, answer,
                                nextSkill, nextDifficultyLevel, maxIG);

            // Mark the question as answered and remove it from the list of available questions
            availableQuestions.remove(indexQ);
            questionSet.addAskedQuestion();

            if (answer == 0) {
                wrongQ[nextSkill][nextDifficultyLevel] += 1;
            } else {
                rightQ[nextSkill][nextDifficultyLevel] += 1;
            }

            // stop criteria
            stop = stopCriteria();

            if (questionSet.isEmpty()) {
                break;
            }

            question++;

        } while (!stop);
        System.out.println("\n--------------------------------------------\n");
        System.out.printf("Skills probabilities %s%n", ArrayUtils.toString(priorResults));
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

        int[] skillsLevels = new int[nSkills];

        for (int s = 0; s < nSkills; s++) {
            double max = Double.NEGATIVE_INFINITY;
            int index = -1;

            for (int i = 0; i < priorResults[s][0].length; i++) {
                if (max < priorResults[s][0][i]) {
                    max = priorResults[s][0][i];
                    index = i;
                }
            }

            skillsLevels[s] = index;
        }

        System.out.printf("Skills levels %s%n", ArrayUtils.toString(skillsLevels));
        System.out.print("\n");

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
            // log base 4
            double logXv = Math.log(v) / Math.log(BNsAdaptiveSurveyTest.states);
            h += v * logXv;
        }

        return -h;
    }

    private boolean stopCriteria() {
        boolean stop;
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
                stop = false;
                break;
            }
        }
        return stop;
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

    public int[] getProfile() {
        return profile;
    }
}
