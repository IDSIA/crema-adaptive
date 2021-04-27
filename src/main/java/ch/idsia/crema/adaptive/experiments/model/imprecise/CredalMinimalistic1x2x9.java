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
public class CredalMinimalistic1x2x9 extends AbstractModelBuilder<IntervalFactor> {

    /**
     * Build a Credal model with one skill with two states and a given number of questions.
     * Each skill has 8 templates of questions, each template has nQuestions questions, and each question has 2 states.
     * All the questions in a template have the same CPT.
     *
     * @param nQuestions number of questions in total, all the questions in a
     *                   template have the same CPT.
     */

    public CredalMinimalistic1x2x9(int nQuestions){
        model = new DAGModel<>();

        int s = model.addVariable(2);
        model.setFactor(s, new IntervalFactor(model.getDomain(s), Strides.EMPTY, new double[][]{{.45, .45}},
                new double[][]{{.55, .55}}));

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
            addQuestionNodeL9(model, s, 10 * s + 9);
        }
    }


    public void addQuestionNodeL1(DAGModel<IntervalFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final IntervalFactor fQ = new IntervalFactor(model.getDomain(q), model.getDomain(parent));
        fQ.setLower(new double[]{.40 + 0.05, 1 - (.80 + 0.05)}, 0); // lP(Q=right|S=0) {l1, 1 - u1}
        fQ.setUpper(new double[]{.80 + 0.05, 1 - (.40 + 0.05)}, 0); // uP(Q=right|S=0) {u1, 1 - l1}

        fQ.setLower(new double[]{.40 - 0.05, 1. - (.80 - 0.05)}, 1); // lP(Q=right|S=1) {l0, 1 - u0}
        fQ.setUpper(new double[]{.80 - 0.05, 1. - (.40 - 0.05)}, 1); // uP(Q=right|S=1) {u0, 1 - l0}

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    public void addQuestionNodeL2(DAGModel<IntervalFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final IntervalFactor fQ = new IntervalFactor(model.getDomain(q), model.getDomain(parent));
        fQ.setLower(new double[]{.35 + 0.05, 1 - (.85 + 0.05)}, 0); // lP(Q=right|S=0) {l1, 1 - u1}
        fQ.setUpper(new double[]{.85 + 0.05, 1 - (.35 + 0.05)}, 0); // uP(Q=right|S=0) {u1, 1 - l1}

        fQ.setLower(new double[]{.35 - 0.05, 1. - (.85 - 0.05)}, 1); // lP(Q=right|S=1) {l0, 1 - u0}
        fQ.setUpper(new double[]{.85 - 0.05, 1. - (.35 - 0.05)}, 1); // uP(Q=right|S=1) {u0, 1 - l0}

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    public void addQuestionNodeL3(DAGModel<IntervalFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final IntervalFactor fQ = new IntervalFactor(model.getDomain(q), model.getDomain(parent));
        fQ.setLower(new double[]{.3 + 0.05, 1 - (.9 + 0.05)}, 0); // lP(Q=right|S=0) {l1, 1 - u1}
        fQ.setUpper(new double[]{.9 + 0.05, 1 - (.3 + 0.05)}, 0); // uP(Q=right|S=0) {u1, 1 - l1}

        fQ.setLower(new double[]{.3 - 0.05, 1. - (.9 - 0.05)}, 1); // lP(Q=right|S=1) {l0, 1 - u0}
        fQ.setUpper(new double[]{.9 - 0.05, 1. - (.3 - 0.05)}, 1); // uP(Q=right|S=1) {u0, 1 - l0}

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    public void addQuestionNodeL4(DAGModel<IntervalFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final IntervalFactor fQ = new IntervalFactor(model.getDomain(q), model.getDomain(parent));
        fQ.setLower(new double[]{.3 + 0.05, 1 - (.7 + 0.05)}, 0); // lP(Q=right|S=0) {l1, 1 - u1}
        fQ.setUpper(new double[]{.7 + 0.05, 1 - (.3 + 0.05)}, 0); // uP(Q=right|S=0) {u1, 1 - l1}

        fQ.setLower(new double[]{.3 - 0.05, 1. - (.7 - 0.05)}, 1); // lP(Q=right|S=1) {l0, 1 - u0}
        fQ.setUpper(new double[]{.7 - 0.05, 1. - (.3 - 0.05)}, 1); // uP(Q=right|S=1) {u0, 1 - l0}

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    public void addQuestionNodeL5(DAGModel<IntervalFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final IntervalFactor fQ = new IntervalFactor(model.getDomain(q), model.getDomain(parent));
        fQ.setLower(new double[]{.25 + 0.05, 1 - (.75 + 0.05)}, 0); // lP(Q=right|S=0) {l1, 1 - u1}
        fQ.setUpper(new double[]{.75 + 0.05, 1 - (.25 + 0.05)}, 0); // uP(Q=right|S=0) {u1, 1 - l1}

        fQ.setLower(new double[]{.25 - 0.05, 1. - (.75 - 0.05)}, 1); // lP(Q=right|S=1) {l0, 1 - u0}
        fQ.setUpper(new double[]{.75 - 0.05, 1. - (.25 - 0.05)}, 1); // uP(Q=right|S=1) {u0, 1 - l0}

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    public void addQuestionNodeL6(DAGModel<IntervalFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final IntervalFactor fQ = new IntervalFactor(model.getDomain(q), model.getDomain(parent));
        fQ.setLower(new double[]{.2 + 0.05, 1 - (.8 + 0.05)}, 0); // lP(Q=right|S=0) {l1, 1 - u1}
        fQ.setUpper(new double[]{.8 + 0.05, 1 - (.2 + 0.05)}, 0); // uP(Q=right|S=0) {u1, 1 - l1}

        fQ.setLower(new double[]{.2 - 0.05, 1. - (.8 - 0.05)}, 1); // lP(Q=right|S=1) {l0, 1 - u0}
        fQ.setUpper(new double[]{.8 - 0.05, 1. - (.2 - 0.05)}, 1); // uP(Q=right|S=1) {u0, 1 - l0}

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    public void addQuestionNodeL7(DAGModel<IntervalFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final IntervalFactor fQ = new IntervalFactor(model.getDomain(q), model.getDomain(parent));
        fQ.setLower(new double[]{.2 + 0.05, 1 - (.6 + 0.05)}, 0); // lP(Q=right|S=0) {l1, 1 - u1}
        fQ.setUpper(new double[]{.6 + 0.05, 1 - (.2 + 0.05)}, 0); // uP(Q=right|S=0) {u1, 1 - l1}

        fQ.setLower(new double[]{.2 - 0.05, 1. - (.6 - 0.05)}, 1); // lP(Q=right|S=1) {l0, 1 - u0}
        fQ.setUpper(new double[]{.6 - 0.05, 1. - (.2 - 0.05)}, 1); // uP(Q=right|S=1) {u0, 1 - l0}

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    public void addQuestionNodeL8(DAGModel<IntervalFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final IntervalFactor fQ = new IntervalFactor(model.getDomain(q), model.getDomain(parent));
        fQ.setLower(new double[]{.15 + 0.05, 1 - (.65 + 0.05)}, 0); // lP(Q=right|S=0) {l1, 1 - u1}
        fQ.setUpper(new double[]{.65 + 0.05, 1 - (.15 + 0.05)}, 0); // uP(Q=right|S=0) {u1, 1 - l1}

        fQ.setLower(new double[]{.15 - 0.05, 1. - (.65 - 0.05)}, 1); // lP(Q=right|S=1) {l0, 1 - u0}
        fQ.setUpper(new double[]{.65 - 0.05, 1. - (.15 - 0.05)}, 1); // uP(Q=right|S=1) {u0, 1 - l0}

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    public void addQuestionNodeL9(DAGModel<IntervalFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final IntervalFactor fQ = new IntervalFactor(model.getDomain(q), model.getDomain(parent));
        fQ.setLower(new double[]{.1 + 0.05, 1 - (.7 + 0.05)}, 0); // lP(Q=right|S=0) {l1, 1 - u1}
        fQ.setUpper(new double[]{.7 + 0.05, 1 - (.1 + 0.05)}, 0); // uP(Q=right|S=0) {u1, 1 - l1}

        fQ.setLower(new double[]{.1 - 0.05, 1. - (.7 - 0.05)}, 1); // lP(Q=right|S=1) {l0, 1 - u0}
        fQ.setUpper(new double[]{.7 - 0.05, 1. - (.1 - 0.05)}, 1); // uP(Q=right|S=1) {u0, 1 - l0}

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }
}
