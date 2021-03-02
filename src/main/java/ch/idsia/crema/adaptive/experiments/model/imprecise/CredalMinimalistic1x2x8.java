package ch.idsia.crema.adaptive.experiments.model.imprecise;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.model.AbstractModelBuilder;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.model.graphical.DAGModel;

/**
 * Author:  		Giorgia Adorni
 * Project: 		crema-adaptive
 * Date:   			2.03.2021 14:15
 */
public class CredalMinimalistic1x2x8 extends AbstractModelBuilder<IntervalFactor> {

    /**
     * Build a Credal model with one skill with two states and a given number of questions.
     * Each skill has 8 templates of questions, each template has nQuestions questions, and each question has 2 states.
     * All the questions in a template have the same CPT.
     *
     * @param nQuestions number of questions in total, all the questions in a
     *                   template have the same CPT.
     */

    public CredalMinimalistic1x2x8(int nQuestions){
        model = new DAGModel<>();

        int s = model.addVariable(2);
        model.setFactor(s, new IntervalFactor(model.getDomain(s), Strides.EMPTY, new double[][]{{.325, .650}}, new double[][]{{.350, .675}}));

        skills.add(s);

        for (int i = 0; i < nQuestions; i++) {
            addQuestionNodeL1(model, s, 10 * s + 1);
            addQuestionNodeL2(model, s, 10 * s + 2);
            addQuestionNodeL3(model, s, 10 * s + 3);
            addQuestionNodeL4(model, s, 10 * s + 4);
            addQuestionNodeL5(model, s, 10 * s + 5);
            addQuestionNodeL6(model, s, 10 * s + 6);
            addQuestionNodeL7(model, s, 10 * s + 7);
            addQuestionNodeL8(model, s, 10 * s + 8);
        }
        
    }


    public void addQuestionNodeL1(DAGModel<IntervalFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final IntervalFactor fQ = new IntervalFactor(model.getDomain(q), model.getDomain(parent));
        fQ.setLower(new double[]{.87, 1 - .07}, 0); // lP(Q=right|S=0)
        fQ.setUpper(new double[]{.93, 1 - .13}, 0); // uP(Q=right|S=0)

        fQ.setLower(new double[]{.85, 1. - .90}, 1); // lP(Q=right|S=1)
        fQ.setUpper(new double[]{.90, 1. - .85}, 1); // uP(Q=right|S=1)

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    public void addQuestionNodeL2(DAGModel<IntervalFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final IntervalFactor fQ = new IntervalFactor(model.getDomain(q), model.getDomain(parent));
        fQ.setLower(new double[]{.82, .12}, 0); // lP(Q=right|S=0)
        fQ.setUpper(new double[]{.88, .18}, 0); // uP(Q=right|S=0)

        fQ.setLower(new double[]{.80, 1 - .85}, 1); // lP(Q=right|S=1)
        fQ.setUpper(new double[]{.85, 1 - .80}, 1); // uP(Q=right|S=1)

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    public void addQuestionNodeL3(DAGModel<IntervalFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final IntervalFactor fQ = new IntervalFactor(model.getDomain(q), model.getDomain(parent));
        fQ.setLower(new double[]{.77, .17}, 0); // lP(Q=right|S=0)
        fQ.setUpper(new double[]{.83, .23}, 0); // uP(Q=right|S=0)

        fQ.setLower(new double[]{.75, 1 - .80}, 1); // lP(Q=right|S=1)
        fQ.setUpper(new double[]{.80, 1 - .75}, 1); // uP(Q=right|S=1)

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    public void addQuestionNodeL4(DAGModel<IntervalFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final IntervalFactor fQ = new IntervalFactor(model.getDomain(q), model.getDomain(parent));
        fQ.setLower(new double[]{.72, .22}, 0); // lP(Q=right|S=0)
        fQ.setUpper(new double[]{.78, .28}, 0); // uP(Q=right|S=0)

        fQ.setLower(new double[]{.70, 1 - .75}, 1); // lP(Q=right|S=1)
        fQ.setUpper(new double[]{.75, 1 - .70}, 1); // uP(Q=right|S=1)

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    public void addQuestionNodeL5(DAGModel<IntervalFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final IntervalFactor fQ = new IntervalFactor(model.getDomain(q), model.getDomain(parent));
        fQ.setLower(new double[]{.67, .27}, 0); // lP(Q=right|S=0)
        fQ.setUpper(new double[]{.73, .33}, 0); // uP(Q=right|S=0)

        fQ.setLower(new double[]{.65, 1 - .70}, 1); // lP(Q=right|S=1)
        fQ.setUpper(new double[]{.70, 1 - .65}, 1); // uP(Q=right|S=1)

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    public void addQuestionNodeL6(DAGModel<IntervalFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final IntervalFactor fQ = new IntervalFactor(model.getDomain(q), model.getDomain(parent));
        fQ.setLower(new double[]{.62, .32}, 0); // lP(Q=right|S=0)
        fQ.setUpper(new double[]{.68, .38}, 0); // uP(Q=right|S=0)

        fQ.setLower(new double[]{.60, 1 - .65}, 1); // lP(Q=right|S=1)
        fQ.setUpper(new double[]{.65, 1 - .60}, 1); // uP(Q=right|S=1)

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    public void addQuestionNodeL7(DAGModel<IntervalFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final IntervalFactor fQ = new IntervalFactor(model.getDomain(q), model.getDomain(parent));
        fQ.setLower(new double[]{.57, .37}, 0); // lP(Q=right|S=0)
        fQ.setUpper(new double[]{.63, .43}, 0); // uP(Q=right|S=0)

        fQ.setLower(new double[]{.55, 1 - .60}, 1); // lP(Q=right|S=1)
        fQ.setUpper(new double[]{.60, 1 - .55}, 1); // uP(Q=right|S=1)

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    public void addQuestionNodeL8(DAGModel<IntervalFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final IntervalFactor fQ = new IntervalFactor(model.getDomain(q), model.getDomain(parent));
        fQ.setLower(new double[]{.52, .42}, 0); // lP(Q=right|S=0)
        fQ.setUpper(new double[]{.58, .48}, 0); // uP(Q=right|S=0)

        fQ.setLower(new double[]{.50, 1 - .55}, 1); // lP(Q=right|S=1)
        fQ.setUpper(new double[]{.55, 1 - .50}, 1); // uP(Q=right|S=1)

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }
}
