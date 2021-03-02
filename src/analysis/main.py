import os

import matplotlib.pyplot as plt
import numpy as np
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score

from sklearn.metrics import confusion_matrix
from multilabel_classification_metrics import brier_multicategory, hamming_distance, exact_match_ratio, \
    plot_confusion_matrix, class_metrics_barplot, metric_per_question
from utils import save_metrics, check_dir, save_visualisation


def analyse_model(simulations_dirs, models, n_skills, n_skill_levels):
    """

    :param simulations_dirs:
    :param models:
    :param n_skills:
    :param n_skill_levels:
    """

    n_questions = 0

    for idx, sim in enumerate(simulations_dirs):
        cms = []

        sim_path = "../../output/" + sim + '/'
        pre_name = sim + '.'

        n_skill = n_skills[idx]
        n_states = n_skill_levels[idx]

        n_posteriors = n_skill * n_states

        list_dict_metrics = []
        avg_accuracy_per_question = []
        avg_brier_per_question = []

        for test in models:
            initial_profiles_file = sim_path + sim + ".profiles.csv"
            posteriors_file = sim_path + sim + ".posteriors." + test + ".csv"

            all_observed_classes = np.loadtxt(initial_profiles_file, delimiter=',', dtype=np.int32)
            observed_classes = np.reshape(all_observed_classes, (-1, n_skill))

            posteriors = np.loadtxt(posteriors_file, delimiter=',')[:, 1:]
            n_questions = int(np.shape(posteriors)[1] / n_posteriors)

            posteriors = np.reshape(posteriors, (-1, n_questions, n_skill, n_states))
            all_predicted_classes = np.argmax(posteriors, axis=3)

            final_posterior = posteriors[:, -1, ...]
            predicted_classes = all_predicted_classes[:, -1, ...]
            predicted_classes_5 = all_predicted_classes[:, 5, ...]

            # Analysis
            hamming_losses = []
            hamming_distances = []
            accuracy_scores = []

            all_accuracy_scores = []
            all_brier_scores = []

            brier_scores = brier_multicategory(observed_classes, final_posterior, n_classes=n_states)

            for i in range(len(predicted_classes)):
                hamming_loss, hamming_dist = hamming_distance(observed_classes[i], predicted_classes[i])
                hamming_losses.append(hamming_loss)
                hamming_distances.append(hamming_dist)

                accuracy = accuracy_score(observed_classes[i], predicted_classes[i])

                accuracy_scores.append(accuracy)

            for i in range(len(predicted_classes)):
                all_accuracies = []

                for j in range(n_questions):
                    all_accuracy = accuracy_score(observed_classes[i], all_predicted_classes[i][j])
                    all_accuracies.append(all_accuracy)

                all_accuracy_scores.append(all_accuracies)

            for j in range(n_questions):
                all_briers = brier_multicategory(observed_classes, posteriors[:, j, ...], n_classes=n_states)
                all_brier_scores.append(all_briers)

            hamming_losses = np.array(hamming_losses)
            hamming_distances = np.array(hamming_distances)
            accuracy_scores = np.array(accuracy_scores)
            all_accuracy_scores = np.array(all_accuracy_scores)

            avg_all_accuracy_scores = np.mean(all_accuracy_scores, axis=0)
            avg_accuracy_per_question.append(avg_all_accuracy_scores)

            avg_all_brier_scores = np.mean(np.array(all_brier_scores), axis=1)
            avg_brier_per_question.append(avg_all_brier_scores)

            out_dir = sim_path + "metrics/"
            check_dir(out_dir)

            out_file = out_dir + pre_name + 'metrics.' + test + '.csv'
            metrics = np.column_stack([brier_scores, hamming_losses, hamming_distances, accuracy_scores])
            header = 'brier_scores, hamming_losses, hamming_distances, accuracy_scores'
            save_metrics(out_file, metrics, header)

            # Save averaged metrics
            avg_brier_scores = np.mean(brier_scores)
            avg_hamming_losses = np.mean(hamming_losses)
            avg_hamming_distances = np.mean(hamming_distances)
            avg_accuracy_scores = np.mean(accuracy_scores)

            EMR = exact_match_ratio(observed_classes, predicted_classes)

            predictions = np.array(predicted_classes).flat
            observations = np.array(observed_classes).flat

            predictions_5 = np.array(predicted_classes_5).flat

            macroavg_precision = precision_score(observations, predictions, labels=np.arange(n_states), average='macro')
            macroavg_recall = recall_score(observations, predictions, labels=np.arange(n_states), average='macro')
            macroavg_f1 = f1_score(observations, predictions, labels=np.arange(n_states), average='macro')

            # Save avg metrics
            out_file = out_dir + pre_name + 'avg_metrics.' + test + '.csv'
            header = 'brier_scores, hamming_losses, hamming_distances, accuracy_scores, exact_match_ratio, macroavg_precision, macroavg_recall, macroavg_f1_score'
            metrics = np.column_stack([avg_brier_scores, avg_hamming_losses, avg_hamming_distances,
                                       avg_accuracy_scores, EMR, macroavg_precision, macroavg_recall, macroavg_f1])
            # metrics = np.c_[avg_brier_scores, avg_hamming_losses, avg_hamming_distances,
            #                 avg_accuracy_scores, EMR, macroavg_precision, macroavg_recall, macroavg_f1]
            save_metrics(out_file, metrics, header)

            cm = confusion_matrix(observations, predictions_5, labels=np.arange(n_states))
            cms.append(cm)

            # Compute metrics for each class:
            cm = cm.astype('float') / cm.sum(axis=1)[:, np.newaxis]  # normalize the diagonal entries

            class_accuracy = cm.diagonal()  # the diagonal entries are the accuracies of each class
            class_precision = precision_score(observations, predictions_5, labels=np.arange(n_states), average=None)
            class_recall = recall_score(observations, predictions_5, labels=np.arange(n_states), average=None)
            class_f1 = f1_score(observations, predictions_5, labels=np.arange(n_states), average=None)

            out_file = out_dir + pre_name + 'skills_avg_metrics.' + test + '.csv'

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

        n_models = len(models)

        # Save confusion matrices
        fig, axes = plt.subplots(1, n_models, sharey=True, figsize=(n_models * 3, 4), constrained_layout=True)
        fig.suptitle('Confusion matrix --- ' + sim, fontweight='bold')

        for idx, model in enumerate(models):
            if idx > 0:
                img = plot_confusion_matrix(axes[idx], cms[idx], labels=np.arange(n_states), title=models[idx], y_label=False)
            else:
                plot_confusion_matrix(axes[idx], cms[idx], labels=np.arange(n_states), title=models[idx])

        fig.colorbar(img, ax=axes.ravel().tolist())
        out_images = sim_path + "images/"
        check_dir(out_images)
        save_visualisation(pre_name + 'confusion_matrix', out_images)

        # Save barplots
        class_metrics_barplot(models, list_dict_metrics, dict_labels, sim)
        save_visualisation(pre_name + 'class_metrics', out_images)

        # Save accuracies
        # annotations = [(0, 10), (0, -10), (0, -20), (0, -15)]
        annotations = [(0, 20), (0, 20), (0, -20), (0, 10)]
        metric_per_question(n_questions, avg_accuracy_per_question, 'Accuracy', models, annotations, sim)
        save_visualisation(pre_name + 'accuracy_per_question', out_images)

        # Save brier scores
        annotations = [(0, 5), (0, 5), (0, 5), (0, 5)]
        metric_per_question(n_questions, avg_brier_per_question, 'Brier score', models, annotations, sim)
        save_visualisation(pre_name + 'brier_per_question', out_images)


if __name__ == '__main__':
    root = '../../output/'
    # simulations_dirs = next(os.walk(root))[1]

    simulations_dirs = ['Minimalistic1x2x8', 'Bayesian4x4x4']
    models = ['bayesian-adaptive-entropy', 'bayesian-adaptive-mode',
              'bayesian-adaptive-pright', 'bayesian-non-adaptive']

    number_of_skills = [1, 4]
    number_of_skill_levels = [2, 4]

    analyse_model(simulations_dirs, models, number_of_skills, number_of_skill_levels)
