import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score

from sklearn.metrics import confusion_matrix
from multilabel_classification_metrics import brier_multicategory, hamming_distance, exact_match_ratio, \
    plot_confusion_matrix, class_metrics_barplot, metric_per_question
from utils import save_metrics, check_dir, save_visualisation


def read_and_convert_input_files(n_questions, model_type, n_skill, n_states, sim, sim_path, test):
    """

    :param n_questions
    :param model_type:
    :param n_skill:
    :param n_states:
    :param sim:
    :param sim_path:
    :param test:
    :return: n_questions, all_predicted_classes, final_posteriors, observed_classes, posteriors, predicted_classes,
            predicted_classes_5
    """

    global final_posteriors
    initial_profiles_file = sim_path + sim + ".profiles.csv"
    posteriors_file = sim_path + sim + ".posteriors." + test + ".csv"

    pd.read_csv(posteriors_file)
    observed_classes = np.loadtxt(initial_profiles_file, delimiter=',', dtype=np.int32)
    observed_classes = np.reshape(observed_classes, (-1, n_skill))

    posteriors = pd.read_csv(posteriors_file, sep=',', header=None, index_col=0).to_numpy()
    student_id = pd.read_csv(posteriors_file, sep=',', header=None, usecols=[0]).to_numpy().flatten()

    # posteriors = np.loadtxt(posteriors_file, delimiter=',')[:, 1:]
    # student_id = np.loadtxt(posteriors_file, delimiter=',', dtype=np.int32)[:, 0]

    observed_classes = observed_classes[student_id]

    all_predicted_classes = []
    predicted_classes = []
    predicted_classes_5 = []  # class predicted after 5 steps (used to visualise the the confusion matrix)

    if model_type == 'bayesian':
        n_posteriors = n_skill * n_states
        n_questions = int(np.shape(posteriors)[1] / n_posteriors)

        posteriors = np.reshape(posteriors, (-1, n_questions, n_skill, n_states))

        _all_predicted_classes = np.argmax(posteriors, axis=3)
        all_predicted_classes.append(_all_predicted_classes)

        predicted_classes.append(_all_predicted_classes[:, -1, ...])
        predicted_classes_5.append(_all_predicted_classes[:, 5, ...])

        final_posteriors = [posteriors[:, -1, ...]]

        posteriors = [posteriors]

    elif model_type == 'credal':
        n_posteriors = (n_skill * n_states) * 2
        n_questions = int(np.shape(posteriors)[1] / n_posteriors)

        # (n_states, n_states) = lower and upper
        posteriors = np.reshape(posteriors, (-1, n_questions, n_skill, n_states, n_states))

        posteriors_lower = posteriors[:, :, :, :, 0]
        posteriors_upper = posteriors[:, :, :, 0, :]

        all_predicted_classes_lower = np.argmax(posteriors_lower, axis=3)
        all_predicted_classes_upper = np.argmax(posteriors_upper, axis=3)

        all_predicted_classes.append(all_predicted_classes_lower)
        all_predicted_classes.append(all_predicted_classes_upper)

        predicted_classes.append(all_predicted_classes_lower[:, -1, ...])
        predicted_classes.append(all_predicted_classes_upper[:, -1, ...])
        predicted_classes_5.append(all_predicted_classes_lower[:, 5, ...])
        predicted_classes_5.append(all_predicted_classes_upper[:, 5, ...])

        final_posterior_lower = posteriors[:, -1, :, 0]
        final_posterior_upper = posteriors[:, -1, 0, :]

        final_posteriors = [final_posterior_lower, final_posterior_upper]
        posteriors = [posteriors_lower, posteriors_upper]
    else:
        Exception('Model type not valid. Only bayesian or credal model supported.')

    return [n_questions, all_predicted_classes, final_posteriors, observed_classes, posteriors, predicted_classes,
            predicted_classes_5]


def analyse_model(simulations_dirs, models, n_skills, n_skill_levels, model_type):
    """

    :param simulations_dirs:
    :param models:
    :param n_skills:
    :param n_skill_levels:
    :param model_type:
    """
    n_models = len(models)
    n_questions = 0

    for idx, sim in enumerate(simulations_dirs):
        cms = []

        sim_path = "../../output/" + sim + '/'
        pre_name = sim + '.'

        n_skill = n_skills[idx]
        n_states = n_skill_levels[idx]

        list_dict_metrics = []
        question_accuracy = []  # (n_question)
        question_brier_score = []  # (n_question)

        if model_type == 'bayesian':
            indices = [0]
        else:
            indices = [0, 1]

        for test in models:
            out = read_and_convert_input_files(n_questions, model_type, n_skill, n_states, sim, sim_path, test)
            n_questions, question_predicted_classes, final_posteriors, observed_classes, posteriors, \
            predicted_classes, predicted_classes_5 = out

            for el in indices:
                n_student = len(predicted_classes[el])

                # variables use to distinguish, in case of credal network, between lower and upper metrics
                student_hamming_loss = []          # (n_student)
                student_hamming_distance = []      # (n_student)
                student_accuracy = []              # (n_student)
                # student_brier_score                (n_student)

                student_question_accuracy = []     # (n_student, n_question)
                student_question_brier_score = []  # (n_student, n_question)

                # For each student:
                for i in range(n_student):
                    hamming_loss, hamming_dist = hamming_distance(observed_classes[i], predicted_classes[el][i])
                    student_hamming_loss.append(hamming_loss)
                    student_hamming_distance.append(hamming_dist)

                    a1 = accuracy_score(observed_classes[i], predicted_classes[el][i])
                    student_accuracy.append(a1)

                    # Compute the accuracy and brier score metrics for each question
                    question_accuracies = []  # this is supposed to have shape (19)

                    for j in range(n_questions):
                        # accuracy corresponding to the first question and first student
                        a2 = accuracy_score(observed_classes[i], question_predicted_classes[el][i][j])
                        question_accuracies.append(a2)

                    student_question_accuracy.append(question_accuracies)  # this is supposed to have shape (n_student, n_question)

                # For each question
                for j in range(n_questions):
                    b = brier_multicategory(observed_classes, posteriors[el][:, j, ...], n_classes=n_states)
                    student_question_brier_score.append(b)  # this is supposed to have shape (256, 19)???

                student_question_accuracy = np.array(student_question_accuracy)
                student_question_brier_score = np.array(student_question_brier_score).T

                student_brier_score = brier_multicategory(observed_classes, final_posteriors[el], n_classes=n_states)
                student_hamming_loss = np.array(student_hamming_loss)
                student_hamming_distance = np.array(student_hamming_distance)
                student_accuracy = np.array(student_accuracy)

                # Averaged metrics
                inner_question_accuracy = np.mean(student_question_accuracy, axis=0)
                inner_question_brier_score = np.mean(student_question_brier_score, axis=0)

                # Save some metrics
                out_dir = sim_path + "metrics/"
                check_dir(out_dir)

                # Student metrics
                if model_type == 'bayesian':
                    out_file = out_dir + pre_name + 'student_metrics.' + test + '.csv'
                else:
                    if el == 1:
                        credal_type = 'upper'
                    elif el == 0:
                        credal_type = 'lower'
                    else:
                        Exception('Invalid indices for model type.')

                    out_file = out_dir + pre_name + 'student_metrics.' + test + '.' + credal_type + '.csv'

                metrics = np.column_stack(
                    [student_brier_score, student_hamming_loss, student_hamming_distance, student_accuracy])
                header = 'student_brier_score, student_hamming_loss, student_hamming_distance, student_accuracy'
                save_metrics(out_file, metrics, header)

                # Averaged metrics
                avg_brier_scores = np.mean(student_brier_score)
                avg_hamming_losses = np.mean(student_hamming_loss)
                avg_hamming_distances = np.mean(student_hamming_distance)
                avg_accuracy_scores = np.mean(student_accuracy)

                EMR = exact_match_ratio(observed_classes, predicted_classes[el])

                predictions = np.array(predicted_classes[el]).flat
                observations = np.array(observed_classes).flat

                predictions_5 = np.array(predicted_classes_5[el]).flat

                macroavg_precision = precision_score(observations, predictions, labels=np.arange(n_states),
                                                     average='macro')
                macroavg_recall = recall_score(observations, predictions, labels=np.arange(n_states), average='macro')
                macroavg_f1 = f1_score(observations, predictions, labels=np.arange(n_states), average='macro')

                if model_type == 'bayesian':
                    out_file = out_dir + pre_name + 'avg_metrics.' + test + '.csv'
                else:
                    out_file = out_dir + pre_name + 'avg_metrics.' + test + '.' + credal_type + '.csv'

                header = 'brier_score, hamming_losses, hamming_distances, accuracy_scores, exact_match_ratio, macroavg_precision, macroavg_recall, macroavg_f1_score'
                metrics = np.column_stack([avg_brier_scores, avg_hamming_losses, avg_hamming_distances,
                                           avg_accuracy_scores, EMR, macroavg_precision, macroavg_recall, macroavg_f1])
                save_metrics(out_file, metrics, header)

                # Compute metrics for each skill
                cm = confusion_matrix(observations, predictions_5, labels=np.arange(n_states))
                cms.append(cm)

                _cm = cm.astype('float') / cm.sum(axis=1)[:, np.newaxis]  # normalize the diagonal entries

                class_accuracy = _cm.diagonal()  # the diagonal entries are the accuracies of each class
                class_precision = precision_score(observations, predictions_5, labels=np.arange(n_states), average=None)
                class_recall = recall_score(observations, predictions_5, labels=np.arange(n_states), average=None)
                class_f1 = f1_score(observations, predictions_5, labels=np.arange(n_states), average=None)

                if model_type == 'bayesian':
                    out_file = out_dir + pre_name + 'skills_avg_metrics.' + test + '.csv'
                else:
                    out_file = out_dir + pre_name + 'skills_avg_metrics.' + test + '.' + credal_type + '.csv'

                header = ''
                els = range(class_accuracy.shape[0])
                dict_metrics = {}
                dict_acc = {}
                dict_prec = {}
                dict_rec = {}
                dict_f1 = {}

                for el in els:
                    header = header + 'skill_' + str(el) + '_accuracy, '
                    dict_metrics['accuracy/X' + str(el)] = class_accuracy[el]
                    dict_acc['X' + str(el)] = 'accuracy/X' + str(el)
                for el in els:
                    header = header + 'skill_' + str(el) + '_precision, '
                    dict_metrics['precision/X' + str(el)] = class_precision[el]
                    dict_prec['X' + str(el)] = 'precision/X' + str(el)
                for el in els:
                    header = header + 'skill_' + str(el) + '_recall, '
                    dict_metrics['recall/X' + str(el)] = class_recall[el]
                    dict_rec['X' + str(el)] = 'recall/X' + str(el)
                for el in els:
                    header = header + 'skill_' + str(el) + '_f1_score, '
                    dict_metrics['f1_score/X' + str(el)] = class_f1[el]
                    dict_f1['X' + str(el)] = 'f1_score/X' + str(el)

                dict_labels = [dict_acc, dict_prec, dict_rec, dict_f1]
                metrics = np.column_stack([class_accuracy[::, None].T, class_precision[::, None].T,
                                           class_recall[::, None].T, class_f1[::, None].T])
                save_metrics(out_file, metrics, header)

                list_dict_metrics.append(dict_metrics)

                question_accuracy.append(inner_question_accuracy)
                question_brier_score.append(inner_question_brier_score)

        question_accuracy = np.array(question_accuracy)
        question_brier_score = np.array(question_brier_score)

        cms = np.array(cms)

        for el in indices:
            if model_type == 'bayesian':
                test_indices = [0, 1, 2, 3]
            elif model_type == 'credal':
                if el == 0:
                    credal_type = 'lower'
                    test_indices = [0, 2, 4]
                elif el == 1:
                    credal_type = 'upper'
                    test_indices = [1, 3, 5]
                else:
                    Exception('Invalid indices for model type.')
            else:
                Exception('Model type not valid. Only bayesian or credal model supported.')

            # Save confusion matrices
            fig, axes = plt.subplots(1, n_models, sharey=True, figsize=(n_models * 3, 4), constrained_layout=True)
            fig.suptitle(sim, fontweight='bold')

            for idx, model in enumerate(models):
                if idx > 0:
                    img = plot_confusion_matrix(axes[idx], cms[test_indices][idx], labels=np.arange(n_states), title=model,
                                                y_label=False)
                else:
                    plot_confusion_matrix(axes[idx], cms[test_indices][idx], labels=np.arange(n_states), title=model)

            fig.colorbar(img, ax=axes.ravel().tolist())
            out_images = sim_path + "images/"
            check_dir(out_images)

            if model_type == 'bayesian':
                save_visualisation(pre_name + model_type + '.confusion_matrix.' + model_type, out_images)
            else:
                save_visualisation(pre_name + model_type + '.confusion_matrix.' + model_type + '.' + credal_type, out_images)

            # else:
            # save_visualisation(pre_name + 'confusion_matrix.' + '.' + credal_type, out_images)

            # Save barplots
            class_metrics_barplot(models, list_dict_metrics, dict_labels, sim)

            if model_type == 'bayesian':
                save_visualisation(pre_name + 'class_metrics.' + model_type, out_images)
            else:
                save_visualisation(pre_name + 'class_metrics.' + model_type + '.' + credal_type, out_images)

            # Save accuracies
            # annotations = [(0, 10), (0, -10), (0, -20), (0, -15)]
            annotations = [(0, 20), (0, 20), (0, -20), (0, 10)]

            metric_per_question(n_questions, question_accuracy[test_indices], 'Accuracy', models, annotations, sim)
            if model_type == 'bayesian':
                save_visualisation(pre_name + 'accuracy_per_question.' + model_type, out_images)
            else:
                save_visualisation(pre_name + 'accuracy_per_question.' + model_type + '.' + credal_type, out_images)

            # Save brier scores
            annotations = [(0, 5), (0, 5), (0, 5), (0, 5)]
            metric_per_question(n_questions, question_brier_score[test_indices], 'Brier score', models, annotations, sim)
            if model_type == 'bayesian':
                save_visualisation(pre_name + 'brier_per_question.' + test, out_images)
            else:
                save_visualisation(pre_name + 'brier_per_question.' + test + '.' + credal_type, out_images)


if __name__ == '__main__':
    root = '../../output/'
    # simulations_dirs = next(os.walk(root))[1]

    simulations_dirs = ['Minimalistic1x2x9']

    number_of_skills = [1]
    number_of_skill_levels = [2]

    # Bayesian
    model = 'bayesian'
    bayesian_models = ['bayesian-adaptive-entropy', 'bayesian-adaptive-mode',
                       'bayesian-adaptive-pright', 'bayesian-non-adaptive']

    # analyse_model(simulations_dirs, bayesian_models, number_of_skills, number_of_skill_levels, model)

    # Credal
    model = 'credal'
    credal_models = ['credal-adaptive-entropy', 'credal-adaptive-mode',
                     'credal-adaptive-pright']

    analyse_model(simulations_dirs, credal_models, number_of_skills, number_of_skill_levels, model)
