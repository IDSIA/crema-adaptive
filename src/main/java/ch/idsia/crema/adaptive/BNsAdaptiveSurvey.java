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
 * Date:    24.11.2020 11:40
 */

public class BNsAdaptiveSurvey {

    // Adaptive configuration data -------------------------------------------------------------------------------------

    private static final String bayesianFileName = "adaptive/cnParametersBayes.txt";
    private static final String[] dataset = {
            "adaptive/Hören2015-16.csv",
            "adaptive/Kommunikation2015-16.csv",
            "adaptive/Lesen2015-16.csv",
            "adaptive/Wortschatz und Strukturen2015-16.csv"
    };

    private static final int numberOfSkills = 4;
    private static final int numberOfDifficultyLevels = 4;
    private static final int numberOfLevels = 4;
    private static final int states = numberOfDifficultyLevels;

    private static final long randomSeed = 42;

    // First id, inclusive
    private static final int minStudent = 0;
    // Last id, exclusive
    private static final int maxStudent = 1;

    // Minimum value of entropy to stop the survey.
    private static final double STOP_THRESHOLD = 0.25;

    // Object variables ------------------------------------------------------------------------------------------------
    private final int student;

    //    FIXME
    private final double[][] askedQuestion = new double[numberOfSkills][numberOfDifficultyLevels]; // {{0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}}
    //    FIXME
    private final double[][] rightAnswer = new double[numberOfSkills][numberOfDifficultyLevels]; // {{0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}}

    private final double[][][] priorResults = new double[numberOfSkills][][];
    private final double[][][] hypoteticPosteriorResults = new double[numberOfSkills][][];

    // Debug variables
    private final double[][][] hypoteticPosteriorResultsWrong = new double[numberOfSkills][][];
    private final double[][][] hypoteticPosteriorResultsTrue = new double[numberOfSkills][][];

    private final double[][][] posteriorResults = new double[numberOfSkills][][];

    private final double[][][][] answerLikelihood = new double[numberOfSkills][][][];

    private final AnswerSet[] questionsPerSkill = new AnswerSet[numberOfSkills];

    private final BNsAdaptiveTests BNsAdaptiveTests;
    private final AbellanEntropy abellanEntropy;
    private final QuestionSet questionSet;
    private final Random random;

    private int i = 0;
    private int questionAnswered = 0;

    /**
     * Create a survey test for a single student. Each students will have its lists of questions, its personal test,
     * and its answer sheet.
     *
     * @param student reference id of the students
     */
    private BNsAdaptiveSurvey(int student) {
        this.student = student;

        random = new Random(randomSeed + student);
        BNsAdaptiveTests = new BNsAdaptiveTests();
        abellanEntropy = new AbellanEntropy();
        questionSet = new QuestionSet();
        questionSet.loadKeyList();

        for (int i = 0; i < questionsPerSkill.length; i++) {
            questionsPerSkill[i] = new AnswerSet().load(dataset[i]);
        }
    }

    public static void main(String[] args) {

        final Path out_path = Paths.get("output_" + new SimpleDateFormat("yyyy.MM.dd_HH-mm-ss").format(new Date()) + " .txt");

        // for each student
        for (int student = minStudent; student < maxStudent; student++) {
            try {
                System.out.printf("Start for student %d%n", student);

                BNsAdaptiveSurvey aslat = new BNsAdaptiveSurvey(student);
                aslat.test();

                saveToFile(aslat, out_path);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private static synchronized void saveToFile(BNsAdaptiveSurvey aslat, Path path) {
        try (BufferedWriter bw = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            StringBuilder out = new StringBuilder();
            out.append(String.format("%3d %2d ", aslat.student, aslat.questionAnswered));

            double[][][] results = aslat.getResults();
            for (int s = 0; s < numberOfSkills; s++) {
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
            int nextS = -1;
            int nextL = -1;


            for (int s = 0; s < numberOfSkills; s++) {
//              Current prior
                Object[] output = BNsAdaptiveTests.germanTest(bayesianFileName, s, askedQuestion, rightAnswer);
                priorResults[s] = (double[][])output[0];
                answerLikelihood[s] = (double[][][])output[1];

                // entropy of the skill
                for (int k = 0; k < numberOfSkills; k ++) {
                    if (Math.abs(priorResults[s][0][k] - priorResults[s][1][k] ) >= 0.000001) {
                        System.err.println("Different lower and upper in priors!! " + priorResults[s][0][k] + ", " + priorResults[s][1][k]);
                        break;
                    }
                }

                double HS = H(priorResults[s][0]);

                for (int l = 0; l < numberOfDifficultyLevels; l++) {
                    List<Integer> availableQuestions = questionSet.getQuestions(s, l);

                    // compute entropy only if we have questions available
                    if (availableQuestions.size() == 0) {
                        System.out.println("No more question for skill " + s + " level " + l);
                        continue;
                    }

                    double[] HResults = new double[2];
                    for (int r = 0; r < 2; r++) {
                        askedQuestion[s][l] += 1;
                        rightAnswer[s][l] += r;

                        output = BNsAdaptiveTests.germanTest(bayesianFileName, s, askedQuestion, rightAnswer);
                        hypoteticPosteriorResults[s] = (double[][])output[0];

                        if (r == 0) {
                            hypoteticPosteriorResultsWrong[s] = (double[][]) output[0];
                        } else {
                            hypoteticPosteriorResultsTrue[s] = (double[][]) output[0];
                        }

                        computeEntropy(hypoteticPosteriorResults[s], HResults, r);

                        // clear
                        askedQuestion[s][l] -= 1;
                        rightAnswer[s][l] -= r;
                    }

                    double rightAnswerProbability = 0;
                    double wrongAnswerProbability = 0;

                    for (int j = 0; j < numberOfLevels; j++) {
                        // FIXME before priorResults[s][0][j] was priorResults[0][0][j]
                        //  same for answerLikelihood[s][l][j][0] that was answerLikelihood[0][l][j][0]
                        rightAnswerProbability += answerLikelihood[s][l][j][0] * priorResults[s][0][j];
                        wrongAnswerProbability += (1 - answerLikelihood[s][l][j][0]) * priorResults[s][0][j];
                    }

                    double sumProbs = rightAnswerProbability + wrongAnswerProbability;

                    if (Math.abs(1.0 - sumProbs) >= 0.000001) {
                        System.err.println("Sum of probabilities not 1 -> " + sumProbs);
                    }

                    double H = HResults[0] * wrongAnswerProbability + HResults[1] * rightAnswerProbability;

                    // FIXME: before the max between right and wrong was computed
//                    double H = Math.min(HResults[0], HResults[1]);
                    double ig = HS - H; // infogain

                    // minimize
//					FIXME: what means??
                    if (ig < 0) {
                        System.err.println("Negative information gain for skill " + s + " level " + l + ": \n IG = HS" +
                                " - H = " + HS + " - " +  H + "=" + ig);
//						System.err.println(HResults[0]);
//						System.err.println(HResults[1]);
                    }
                    if (ig > maxIG) {
                        maxIG = H;
                        nextS = s;
                        nextL = l;
                    }
                }
            }

            if (maxIG == Double.MAX_VALUE) {
                System.err.println("No min entropy found! (maxIG = " + maxIG);
                break;
            }

            // get available questions
            List<Integer> availableQuestions = questionSet.getQuestions(nextS, nextL);

            int indexQ = random.nextInt(availableQuestions.size());
            int nextQ = availableQuestions.get(indexQ);
            int answer = questionsPerSkill[nextS].getAnswer(student, nextQ);

            System.out.printf("%d next: %d %d (H=%.4f), Q=%d, answer: %d%n", i, nextS, nextL, maxIG, indexQ, answer);

            questionAnswered++;
            availableQuestions.remove(indexQ);

            askedQuestion[nextS][nextL] += 1;
            rightAnswer[nextS][nextL] += answer;

            // stop criteria
            stop = true;
            for (int s = 0; s < numberOfSkills; s++) {
                Object[] output = BNsAdaptiveTests.germanTest(bayesianFileName, s, askedQuestion, rightAnswer);
                posteriorResults[s] = (double[][])output[0];

                // entropy of the skill
                for (int k = 0; k < numberOfSkills; k ++) {
                    if (Math.abs(posteriorResults[s][0][k] - posteriorResults[s][1][k] ) >= 0.000001) {
                        System.err.println("Different lower and upper in posteriors!! " + posteriorResults[s][0][k] + ", " + posteriorResults[s][1][k]);
                        break;
                    }
                }
                double HS = H(posteriorResults[s][0]);

                if (HS > STOP_THRESHOLD) {
                    System.out.println("HS(" + s + ") = " + HS + ", continue");
                    stop = false;
                    break;
                }
            }

            System.out.printf("Asked question %s%n", ArrayUtils.toString(askedQuestion));
            System.out.printf("Right question %s%n", ArrayUtils.toString(rightAnswer));

            if (questionSet.isEmpty()) {
                System.out.println("All questions done!");
                break;
            }

            i++;
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
//			FIXME log base 4
            double logXv = Math.log(v) / Math.log(BNsAdaptiveSurvey.states);
            h += v * logXv;
        }

        return -h;
    }

    private double[][][] getResults() {
        double[][][] res = new double[numberOfSkills][][];
        Object[] out;

        for (int s = 0; s < numberOfSkills; s++) {

            out = BNsAdaptiveTests.germanTest(bayesianFileName, s, askedQuestion, rightAnswer);
            res[s] = (double[][]) out[0];
        }

        return res;
    }

}
