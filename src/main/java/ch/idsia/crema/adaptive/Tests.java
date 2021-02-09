package ch.idsia.crema.adaptive;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.inference.approxlp.Inference;
import ch.idsia.crema.model.Strides;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.preprocess.RemoveBarren;
import ch.idsia.crema.search.ISearch;
import ch.idsia.crema.search.impl.GreedyWithRandomRestart;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Author:  Giorgia Adorni
 * Project: Crema
 * Date:    24.11.2020 11:40
 */
public class Tests {

    // Global variables
    public static final int nSkills = 4; // Number of skill variables
    public static final int skillLevels = 4; // Number of state for the skill variables
    public static final int difficultyLevels = 4; // Number of state for the question variables
    public static final double cutOff = 1E-3; // Cutoff to remove numerical problems
    public static final String myPath = "adaptive/"; // Path to find input/output files
    public static final int nStudents = 3; // Number of students

    public static void main(String[] args) {

        // Text file where we store the BN pars
        String bayesFileName = "cnParametersBayes.txt";

        // FIXME: Demonstrative answers sequence
        //  Whole set of answers
        double[][][] rightQ = {{{0.0, 4.0, 5.0, 5.0}, {0.0, 4.0, 1.0, 5.0}, {0.0, 4.0, 3.0, 0.0}, {0.0, 1.0, 2.0, 1.0}}, {{0.0, 3.0, 6.0, 2.0}, {0.0, 4.0, 1.0, 1.0}, {0.0, 3.0, 2.0, 0.0}, {0.0, 2.0, 4.0, 4.0}}, {{0.0, 5.0, 0.0, 0.0}, {0.0, 1.0, 0.0, 0.0}, {0.0, 0.0, 0.0, 0.0}, {0.0, 0.0, 0.0, 0.0}}};
        double[][][] wrongQ = {{{0.0, 6.0, 5.0, 5.0}, {0.0, 4.0, 7.0, 3.0}, {0.0, 1.0, 2.0, 5.0}, {0.0, 5.0, 8.0, 9.0}}, {{0.0, 7.0, 4.0, 8.0}, {0.0, 4.0, 7.0, 7.0}, {0.0, 2.0, 3.0, 5.0}, {0.0, 4.0, 6.0, 6.0}}, {{0.0, 5.0, 10.0, 10.0}, {0.0, 7.0, 8.0, 8.0}, {0.0, 5.0, 5.0, 5.0}, {0.0, 6.0, 10.0, 10.0}}};

        // Initialise objects
        Tests myTest = new Tests();

        // Local variables
        double[][] results;
        Object[] output;
        long startTime, difference;

        // Start the clock (elapsed time)
        startTime = System.nanoTime();

        // Loop over the students
        for (int student = 0; student < nStudents; student++) {

            // Loop over the skills
            for (int skill = 0; skill < nSkills; skill++) {
                // Compute and print the results of the Bayesian
                output = myTest.germanTest(myPath + bayesFileName, skill, rightQ[student], wrongQ[student]);
                results = (double[][]) output[0];

                System.out.print("[ID" + student + "][S" + skill + "][Bayes]\t\t");
				// Same upper and lower bounds in case of a Bayesian inference
                for (double p : results[0]) System.out.printf(Locale.ROOT, "%2.3f\t", p * 100);
                System.out.print("\n");
            }
        }

        // Stops the clock and write the elapsed time
        difference = System.nanoTime() - startTime;
        System.out.println("Elapsed time " + String.format("%d min, %d sec", TimeUnit.NANOSECONDS.toHours(difference),
                TimeUnit.NANOSECONDS.toSeconds(difference)
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.NANOSECONDS.toMinutes(difference))));
    }

    /**
     * Multiply a logarithmic CPT by a constant and exponentiate.
     *
     * @param logTab logarithmic CPT
     * @return exponential value of the CPT
     */
    public double[][] fromLogsToProbs(double[][] logTab) {

        // Initialise the output
        double[][] probs = new double[skillLevels][2];

        // Compute the maximum of the logs
        double maximumLog = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < skillLevels; i++)
            if (logTab[i][1] > maximumLog)
                maximumLog = logTab[i][1];

        // Renormalization
        for (int i = 0; i < skillLevels; i++) {
            for (int j = 0; j < 2; j++) {
                probs[i][j] = Math.exp(logTab[i][j] - maximumLog);
                if (probs[i][j] < cutOff)
                    probs[i][j] = cutOff;
            }
        }

        return probs;
    }

    public Object[] germanTest(String fileName, int queriedSkill, double[][] rightQ, double[][] wrongQ) {

        // S0 -> S1 -> S2 -> S3
        //  v     v     v     v
        // Q0     Q1    Q2    Q3

        // Vertex Specification
        SparseModel<GenericFactor> model = new SparseModel<>();

        // Read probabilities from external file
        double[][] netPars = AdaptiveFileTools.readMyFile(fileName);

        // -------------- //
        // SET THE SKILLS //
        // -------------- //
        // Prepare the domains of the skills (nLevels states each)
        // Local models over the skills initialized
        IntervalFactor[] skillsCPT = new IntervalFactor[nSkills]; // Array of factors
        Strides[] domSkill = new Strides[nSkills];
        int[] skill = new int[nSkills];

        for (int s = 0; s < nSkills; s++) { // s is the skill under consideration
            skill[s] = model.addVariable(skillLevels);
            domSkill[s] = Strides.as(skill[s], skillLevels);

            if (s == 0) {
                skillsCPT[s] = new IntervalFactor(domSkill[s], Strides.EMPTY);
                skillsCPT[s].setLower(netPars[0]);
                skillsCPT[s].setUpper(netPars[1]);
            } else {
                skillsCPT[s] = new IntervalFactor(domSkill[s], domSkill[s - 1]);
                skillsCPT[s].setLower(netPars[2], 0); //P(Si|Si-1=0)
                skillsCPT[s].setUpper(netPars[3], 0);
                skillsCPT[s].setLower(netPars[4], 1); //P(Si|Si-1=0)
                skillsCPT[s].setUpper(netPars[5], 1);
                skillsCPT[s].setLower(netPars[6], 2); //P(Si|Si-1=0)
                skillsCPT[s].setUpper(netPars[7], 2);
                skillsCPT[s].setLower(netPars[8], 3); //P(Si|Si-1=0)
                skillsCPT[s].setUpper(netPars[9], 3);
            }
            model.setFactor(skill[s], skillsCPT[s]);
        }

        // ----------------------------- //
        // PARSE THE QUESTION PARAMETERS //
        // ----------------------------- //
        double[][][] questionsCPT = new double[skillLevels][difficultyLevels][2];
        questionsCPT[0][0] = Arrays.copyOfRange(netPars[10], 0, 2); // P(Q|S) easy : lP(Q=right|S=0),uP(Q=right|S=0)
        questionsCPT[0][1] = Arrays.copyOfRange(netPars[11], 0, 2); // P(Q|S) easy : lP(Q=right|S=1),uP(Q=right|S=1)
        questionsCPT[0][2] = Arrays.copyOfRange(netPars[12], 0, 2); // P(Q|S) easy : lP(Q=right|S=2),uP(Q=right|S=2)
        questionsCPT[0][3] = Arrays.copyOfRange(netPars[13], 0, 2); // P(Q|S) easy : lP(Q=right|S=3),uP(Q=right|S=3)

        questionsCPT[1][0] = Arrays.copyOfRange(netPars[14], 0, 2);
        questionsCPT[1][1] = Arrays.copyOfRange(netPars[15], 0, 2);
        questionsCPT[1][2] = Arrays.copyOfRange(netPars[16], 0, 2);
        questionsCPT[1][3] = Arrays.copyOfRange(netPars[17], 0, 2);

        questionsCPT[2][0] = Arrays.copyOfRange(netPars[18], 0, 2);
        questionsCPT[2][1] = Arrays.copyOfRange(netPars[19], 0, 2);
        questionsCPT[2][2] = Arrays.copyOfRange(netPars[20], 0, 2);
        questionsCPT[2][3] = Arrays.copyOfRange(netPars[21], 0, 2);

        questionsCPT[3][0] = Arrays.copyOfRange(netPars[22], 0, 2);
        questionsCPT[3][1] = Arrays.copyOfRange(netPars[23], 0, 2);
        questionsCPT[3][2] = Arrays.copyOfRange(netPars[24], 0, 2);
        questionsCPT[3][3] = Arrays.copyOfRange(netPars[25], 0, 2);

        // ----------------- //
        // SET THE QUESTIONS //
        // ----------------- //
        // A question for each skill (embedding all the answers)
        int nQuestions = nSkills;

        // Local models over the questions initialized
        IntervalFactor[] qFact = new IntervalFactor[nQuestions]; // Array of factors

        // Prepare the domains of the questions (two states each)
        Strides[] domQuestion = new Strides[nQuestions];
        int[] question = new int[nQuestions];
        for (int s = 0; s < nQuestions; s++) {
            question[s] = model.addVariable(2);
            domQuestion[s] = Strides.as(question[s], 2);
            qFact[s] = new IntervalFactor(domQuestion[s], domSkill[s]);
            double[][] myLogs = new double[skillLevels][2];
            double[][] lP = new double[skillLevels][2];
            double[][] uP = new double[skillLevels][2];

  			// Computing the entropy
            for (int sl = 0; sl < skillLevels; sl++) { // livelli skill
                for (int dl = 0; dl < difficultyLevels; dl++) { // livelli di difficoltá
                    myLogs[sl][0] += Math.log(questionsCPT[dl][sl][0]) * rightQ[s][dl];
                    myLogs[sl][1] += Math.log(questionsCPT[dl][sl][1]) * rightQ[s][dl];
                    myLogs[sl][0] += Math.log(1.0 - questionsCPT[dl][sl][1]) * wrongQ[s][dl];
                    myLogs[sl][1] += Math.log(1.0 - questionsCPT[dl][sl][0]) * wrongQ[s][dl];
                }
            }

            double[][] probs = fromLogsToProbs(myLogs);

            for (int skillLevel = 0; skillLevel < skillLevels; skillLevel++) {
                lP[skillLevel][0] = probs[skillLevel][0];
                lP[skillLevel][1] = 1.0 - probs[skillLevel][1];

                uP[skillLevel][0] = probs[skillLevel][1];
                uP[skillLevel][1] = 1.0 - probs[skillLevel][0];

                qFact[s].setLower(lP[skillLevel].clone(), skillLevel);
                qFact[s].setUpper(uP[skillLevel].clone(), skillLevel);
            }

            model.setFactor(question[s], qFact[s]);
        }

        // Dummy variable implementing the observation of the questions
        // this is a common child of the three questions
        // FIXME: serve a specificare l'evidenza? Come va fatto e come viene invece fatto qui? ...
        int dummy = model.addVariable(2);
        BayesianFactor fDummy = new BayesianFactor(model.getDomain(question[0], question[1], question[2], question[3], dummy), false);
        fDummy.setValue(1.0, 0, 0, 0, 0, 1);
        fDummy.setValue(1.0, 0, 0, 0, 1, 0);
        fDummy.setValue(1.0, 0, 0, 1, 0, 0);
        fDummy.setValue(1.0, 0, 0, 1, 1, 0);
        fDummy.setValue(1.0, 0, 1, 0, 0, 0);
        fDummy.setValue(1.0, 0, 1, 0, 1, 0);
        fDummy.setValue(1.0, 0, 1, 1, 0, 0);
        fDummy.setValue(1.0, 0, 1, 1, 1, 0);
        fDummy.setValue(1.0, 1, 0, 0, 0, 0);
        fDummy.setValue(1.0, 1, 0, 0, 1, 0);
        fDummy.setValue(1.0, 1, 0, 1, 0, 0);
        fDummy.setValue(1.0, 1, 0, 1, 1, 0);
        fDummy.setValue(1.0, 1, 1, 0, 0, 0);
        fDummy.setValue(1.0, 1, 1, 0, 1, 0);
        fDummy.setValue(1.0, 1, 1, 1, 0, 0);
        fDummy.setValue(1.0, 1, 1, 1, 1, 0);
        model.setFactor(dummy, fDummy);
        model = new RemoveBarren().execute(model, skill[queriedSkill], dummy);

        // Compute the inferences
        Inference<BayesianFactor> approx = new Inference<>();
        approx.initialize(new HashMap<>() {{
            put(ISearch.MAX_TIME, "8");
            put(GreedyWithRandomRestart.MAX_RESTARTS, "4");
            put(GreedyWithRandomRestart.MAX_PLATEAU, "2");
        }});

        try {
            IntervalFactor resultsALP = approx.query(model, skill[queriedSkill], dummy);
            // Return the results of ApproxLP
            double[][] posteriors = new double[2][skillLevels];
            posteriors[0] = resultsALP.getLower();
            posteriors[1] = resultsALP.getUpper();

            // TODO: ritornare anche la probabilitá di rispondere giusto/sbagliato
            

            // Return, in addition to the probability of the skill (posterior or the updated prior), the
            // probability of the answer given the skill, used in BNsAdaptiveSurvey to compute the probability of the
            // answer
            Object[] finalOutput = new Object[2];
            finalOutput[0] = posteriors;
            finalOutput[1] = questionsCPT;

            return finalOutput;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }
}