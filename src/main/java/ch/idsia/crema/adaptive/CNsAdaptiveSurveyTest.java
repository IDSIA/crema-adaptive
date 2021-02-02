package ch.idsia.crema.adaptive;

import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * Author:  Giorgia Adorni
 * Project: Crema
 * Date:    14.12.2020 9:00
 */
public class CNsAdaptiveSurveyTest {

    // Adaptive configuration data -------------------------------------------------------------------------------------
    private static final String credalFileName = "adaptive/cnParameters.txt";
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

    // First id, inclusive
    private static final int minStudent = 0;
    // Last id, exclusive
    private static final int maxStudent = 1;

    // Minimum value of entropy to stop the survey.
    private static final double STOP_THRESHOLD = 0.25;

    // Object variables ------------------------------------------------------------------------------------------------
    private final int student;

    private final double[][] rightQ = new double[nSkills][nDifficultyLevels];
    // {{0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}}
    private final double[][] wrongQ = new double[nSkills][nDifficultyLevels];
    // {{0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}}

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
    private int questionAnswered = 0;


    /**
     * Create a survey test for a single student. Each students will have its lists of questions, its personal test,
     * and its answer sheet.
     *
     * @param student reference id of the students
     */
    private CNsAdaptiveSurveyTest(int student) {
        this.student = student;

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

        final Path out_path = Paths.get("output_" + new SimpleDateFormat("yyyy.MM.dd_HH-mm-ss")
                                   .format(new Date()) + " .txt");

        // for each student
        for (int student = minStudent; student < maxStudent; student++) {
            try {
                System.out.printf("Start for student %d%n", student);

                CNsAdaptiveSurveyTest aslat = new CNsAdaptiveSurveyTest(student);
                aslat.test();

                saveToFile(aslat, out_path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static synchronized void saveToFile(CNsAdaptiveSurveyTest aslat, Path path) {
        try (BufferedWriter bw = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            StringBuilder out = new StringBuilder();
            out.append(String.format("%3d %2d ", aslat.student, aslat.questionAnswered));

            double[][][] results = aslat.getResults();
            for (int s = 0; s < nSkills; s++) {
                // interval dominance
                int[] dominating = intervalDominance(results[s][0], results[s][1]);

                out.append(s).append(": [ ");
                for (int d : dominating) {
                    out.append(d).append(" ");
                }
                out.append("]\n");
            }
            out.append("\n");

            bw.write(out.toString());
        } catch (IOException ignored) {
        }
    }

    private static int[] intervalDominance(double[] lowers, double[] uppers) {
        int n = lowers.length;

        // ordered from min to max
        int[] lOrdered = IntStream.range(0, lowers.length)
                .boxed().sorted(Comparator.comparingDouble(a -> lowers[a]))
                .mapToInt(e -> e).toArray();
        int[] uOrdered = IntStream.range(0, uppers.length)
                .boxed().sorted(Comparator.comparingDouble(a -> uppers[a]))
                .mapToInt(e -> e).toArray();

        List<Integer> dominating = new ArrayList<>();

        // remember min/max for up/low
        double maxU = 0.0;
        double minL = Double.MAX_VALUE;

        for (int i = n - 1; i > 0; i--) {
            dominating.add(lOrdered[i]);
            if (maxU < uppers[lOrdered[i]]) {
                maxU = uppers[lOrdered[i]];
            }
            if (minL > lowers[lOrdered[i]]) {
                minL = lowers[lOrdered[i]];
            }

            if (minL > uppers[uOrdered[i - 1]]) {
                break;
            }
        }

        int[] dominatingInts = new int[dominating.size()];
        for (int i = 0; i < dominating.size(); i++) {
            dominatingInts[i] = dominating.get(i);
        }

        return dominatingInts;
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
                Object[] testOutput = AdaptiveTests.germanTest(credalFileName, s, rightQ, wrongQ);
                priorResults[s] = (double[][]) testOutput[0];
                answerLikelihood[s] = (double[][][]) testOutput[1];

                // Entropy of the skill
                double[] distribution = abellanEntropy.getDistrWithMaxEntropy(priorResults[s][0], priorResults[s][1]);
                double HS = H(distribution);

                for (int dl = 0; dl < nDifficultyLevels; dl++) {
                    List<Integer> availableQuestions = questionSet.getQuestions(s, dl);

                    // compute entropy only if we have questions available
                    if (availableQuestions.size() == 0) {
                        System.out.println("No more question for skill " + s + " level " + dl);
                        continue;
                    }

                    double[] HResults = new double[2];
                    for (int answer = 0; answer < 2; answer++) {

                        if (answer == 0) {
                            wrongQ[s][dl] += 1;
                        } else {
                            rightQ[s][dl] += 1;
                        }

                        testOutput = AdaptiveTests.germanTest(credalFileName, s, rightQ, wrongQ);
                        hypotheticalPosteriorResults[s] = (double[][]) testOutput[0];

                        if (answer == 0) {
                            hypotheticalPosteriorResultsWrong[s] = (double[][]) testOutput[0];
                        } else {
                            hypotheticalPosteriorResultsRight[s] = (double[][]) testOutput[0];
                        }

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

                    // FIXME
                    for (int sl = 0; sl < nSkillLevels; sl++) {
                        // answerLikelihood[s][dl][sl][0] not 0
                        rightAnswerProbability += answerLikelihood[s][dl][sl][0] * priorResults[s][0][sl];
                        wrongAnswerProbability += (1 - answerLikelihood[s][dl][sl][0]) * priorResults[s][0][sl];
                    }

                    double sumProbs = rightAnswerProbability + wrongAnswerProbability;

                    if (Math.abs(1.0 - sumProbs) >= 0.000001) {
                        System.err.println("Sum of probabilities not 1 -> " + sumProbs);
                    }

                    double H = HResults[0] * wrongAnswerProbability + HResults[1] * rightAnswerProbability;

                    // max between right and wrong
                    // double H = Math.max(HResults[0], HResults[1]);
                    double ig = HS - H; // infogain

                    // end FIXME
                    
                    // minimize
                    if (ig < 0.000001) {
                        System.err.println("Negative information gain for skill " + s + " level " + dl +
                                ": \n IG = HS" + " - H = " + HS + " - " + H + "=" + ig);
                    }

                    System.out.printf("skill %d, difficulty level %d, ig %.4f, max_ig %.4f %n", s, dl, ig, maxIG);

                    if (ig > maxIG) {
                        System.out.printf(" ig > maxIG --> Updating maxIG...%n");

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

            // get available questions
            List<Integer> availableQuestions = null;

            try {
                availableQuestions = questionSet.getQuestions(nextSkill, nextDifficultyLevel);
            } catch (NullPointerException e) {
                System.out.print(questionSet);
                System.out.print("NullPointerException Caught");
            }

            assert availableQuestions != null;

            int indexQ = random.nextInt(availableQuestions.size());
            int nextQ = availableQuestions.get(indexQ);
            int answer = questionsPerSkill[nextSkill].getAnswer(student, nextQ);

            System.out.printf("Asked question %d, answer %d%n next skill %d, " +
                            "next difficulty level %d, (H=%.4f)%n", question, answer,
                    nextSkill, nextDifficultyLevel, maxIG);

            questionAnswered++;
            availableQuestions.remove(indexQ);

            if (answer == 0) {
                wrongQ[nextSkill][nextDifficultyLevel] += 1;
            } else {
                rightQ[nextSkill][nextDifficultyLevel] += 1;
            }

            // stop criteria
            stop = true;
            for (int s = 0; s < nSkills; s++) {
                Object[] output = AdaptiveTests.germanTest(credalFileName, s, rightQ, wrongQ);
                posteriorResults[s] = (double[][]) output[0];

                // entropy of the skill
                double[] distribution = abellanEntropy.getDistrWithMaxEntropy(posteriorResults[s][0], posteriorResults[s][1]);
                double HS = H(distribution);

                if (HS > STOP_THRESHOLD) {
                    System.out.println("HS(s=" + s + ") = " + HS + ", HS > STOP_THRESHOLD, continue");
                    stop = false;
                    break;
                } else {
                    System.out.println("HS(s=" + s + ") = " + HS + ", HS < STOP_THRESHOLD");
                }
            }

            System.out.printf("Right question %s%n", ArrayUtils.toString(rightQ));
            System.out.printf("Wrong question %s%n", ArrayUtils.toString(wrongQ));
            System.out.print("\n");

            if (questionSet.isEmpty()) {
                System.out.println("All questions done!");
                System.out.printf("Skills probabilities %s%n", ArrayUtils.toString(posteriorResults));
                break;
            }

            question++;
        } while (!stop);
        System.out.println("Done!");
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
            double logXv = Math.log(v) / Math.log(CNsAdaptiveSurveyTest.states);
            h += v * logXv;
        }

        return -h;
    }

    private double[][][] getResults() {
        double[][][] result = new double[nSkills][][];
        Object[] testResult;

        for (int s = 0; s < nSkills; s++) {

            testResult = AdaptiveTests.germanTest(credalFileName, s, rightQ, wrongQ);
            result[s] = (double[][]) testResult[0];
        }

        return result;
    }
}
