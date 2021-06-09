package ch.idsia.crema.adaptive.experiments.model.precise;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.model.AbstractModelBuilder;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.DAGModel;

/**
 * Author:  Giorgia Adorni
 * Project: crema-adaptive
 * Date:    18.02.2021 16:00
 */
public class BayesianMinimalistic1x2x9 extends AbstractModelBuilder<BayesianFactor> {

    /**
     * A Bayesian model with one skill with two states and a given number of questions.
     * Each skill has 8 templates of questions, each template has nQuestions questions, and each question has 2 states.
     * * All the questions in a template have the same CPT.
     *
     * @param nQuestions number of questions in total, all the questions in a template have the same CPT.
     */
    public BayesianMinimalistic1x2x9(int nQuestions) {
        model = new DAGModel<>();

        int s = model.addVariable(2);
        model.setFactor(s, new BayesianFactor(model.getDomain(s), new double[]{.5, .5}));

        skills.add(s);

        for (int i = 0; i < nQuestions; i++) {
            addQuestionNodeTemplate(model, s, 10 * s + 1, .40, .80);
            addQuestionNodeTemplate(model, s, 10 * s + 2, .35, .85);
            addQuestionNodeTemplate(model, s, 10 * s + 3, .30, .90);
            addQuestionNodeTemplate(model, s, 10 * s + 4, .30, .70);
            addQuestionNodeTemplate(model, s, 10 * s + 5, .25, .75);
            addQuestionNodeTemplate(model, s, 10 * s + 6, .20, .80);
            addQuestionNodeTemplate(model, s, 10 * s + 7, .40, .60);
            addQuestionNodeTemplate(model, s, 10 * s + 8, .15, .65);
            addQuestionNodeTemplate(model, s, 10 * s + 9, .10, .70);
        }
    }

    public void addQuestionNodeTemplate(DAGModel<BayesianFactor> model, int parent, int template, double p0, double p1) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final BayesianFactor fQ = new BayesianFactor(model.getDomain(parent, q), false);
        fQ.setData(new double[]{1. - p0, 1. - p1, p0, p1});

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    public void addQuestionNodeL1(DAGModel<BayesianFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final BayesianFactor fQ = new BayesianFactor(model.getDomain(parent, q), false);
        fQ.setData(new int[]{q, parent}, new double[]{.8, .2, .4, .6});

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    public void addQuestionNodeL2(DAGModel<BayesianFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final BayesianFactor fQ = new BayesianFactor(model.getDomain(parent, q), false);
        fQ.setData(new int[]{q, parent}, new double[]{.85, .15, .35, .65});

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    public void addQuestionNodeL3(DAGModel<BayesianFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final BayesianFactor fQ = new BayesianFactor(model.getDomain(parent, q), false);
        fQ.setData(new int[]{q, parent}, new double[]{.90, .1, .3, .7});

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    public void addQuestionNodeL4(DAGModel<BayesianFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final BayesianFactor fQ = new BayesianFactor(model.getDomain(parent, q), false);
        fQ.setData(new int[]{q, parent}, new double[]{.7, .3, .3, .7});

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    public void addQuestionNodeL5(DAGModel<BayesianFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final BayesianFactor fQ = new BayesianFactor(model.getDomain(parent, q), false);
        fQ.setData(new int[]{q, parent}, new double[]{.75, .25, .25, .75});

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    public void addQuestionNodeL6(DAGModel<BayesianFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final BayesianFactor fQ = new BayesianFactor(model.getDomain(parent, q), false);
        fQ.setData(new int[]{q, parent}, new double[]{.8, .2, .2, .8});

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    public void addQuestionNodeL7(DAGModel<BayesianFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final BayesianFactor fQ = new BayesianFactor(model.getDomain(parent, q), false);
        fQ.setData(new int[]{q, parent}, new double[]{.6, .4, .2, .8});

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    public void addQuestionNodeL8(DAGModel<BayesianFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final BayesianFactor fQ = new BayesianFactor(model.getDomain(parent, q), false);
        fQ.setData(new int[]{q, parent}, new double[]{.65, .35, .15, .85});

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    public void addQuestionNodeL9(DAGModel<BayesianFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final BayesianFactor fQ = new BayesianFactor(model.getDomain(parent, q), false);
        fQ.setData(new int[]{q, parent}, new double[]{.7, .3, .1, .9});

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }
}
