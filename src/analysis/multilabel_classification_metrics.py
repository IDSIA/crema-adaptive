import itertools
from typing import List

import matplotlib.pyplot as plt
import numpy as np
from scipy.spatial.distance import hamming

from utils import one_hot_encode


def brier_multicategory(targets, probs, n_classes):
    """
    (reference: https://en.wikipedia.org/wiki/Brier_score)
    :param targets:
    :param probs:
    :param n_classes
    :return brier_score:
    """
    one_hot_targets = one_hot_encode(targets, n_classes)

    brier_score = np.mean(np.sum((probs - one_hot_targets) ** 2, axis=2), axis=1)/2
    return brier_score


def hamming_distance(observations, predictions):
    """

    :param observations:
    :param predictions:
    :return hamming_loss, hamming_distance:
    """
    hamming_loss = hamming(observations, predictions)
    hamming_distance = hamming_loss * observations.size
    # FIXME
    # hamming_distance = hamming_loss * len(observations)

    return hamming_loss, hamming_distance


def precision(observations, predictions):
    """

    :param observations:
    :param predictions:
    :return precision:
    """
    temp = 0
    for i in range(observations.shape[0]):
        temp += sum(np.logical_and(observations[i], predictions[i])) / sum(observations[i])

    precision = temp / observations.shape[0]
    return precision


def exact_match_ratio(observations, predictions):
    """

    :param observations:
    :param predictions:
    :return exact match ratio:
    """
    return np.all(predictions == observations, axis=1).mean()


def plot_confusion_matrix(ax, cm, labels, title, y_label=True):
    """
    This function prints and plots the confusion matrix.
    Normalization can be applied by setting `normalize=True`.
    :param ax
    :param cm
    :param labels:
    :param title:
    :param: y_label
    :return cm, img:
    """
    np.set_printoptions(precision=2)

    cmap = plt.cm.Blues

    ax.set_title(title)

    img = ax.imshow(cm, interpolation='nearest', cmap=cmap)
    ax.set_xticks(labels)
    ax.set_yticks(labels)
    ax.tick_params(labelrotation=45)

    fmt = 'd'
    thresh = cm.max() / 2.
    for i, j in itertools.product(range(cm.shape[0]), range(cm.shape[1])):
        ax.text(j, i, format(cm[i, j], fmt),
                 horizontalalignment="center",
                 color="white" if cm[i, j] > thresh else "black")

    if y_label:
        ax.set_ylabel('Groundtruth')
    ax.set_xlabel('Prediction')

    return img


def grouped_barplot(ax: plt.Axes, runs: List[str], metrics, keys, title=None, legend=True):
    """

    :param ax:
    :param runs:
    :param metrics:
    :param keys:
    :param title:
    :param legend:
    :return:
    """

    n_runs = len(runs)
    n_keys = len(keys)
    idxs = np.arange(n_keys)

    if isinstance(keys, dict):
        xticklabels = keys.keys()
        keys = keys.values()
    else:
        xticklabels = keys

    padding = 0.2
    width = (1 - 2 * padding) / n_runs

    for i, (run, met) in enumerate(zip(runs, metrics)):
        pos = -0.5 + padding + idxs + i * width + width / 2
        values = [met.get(key, np.nan) for key in keys]

        ax.bar(pos, values, width, label=run if legend else None)

    ax.set_title(title, fontweight='bold')
    ax.grid(True, axis='y')
    ax.set_axisbelow(True)
    ax.set_xlim(-0.5, n_keys - 0.5)
    ax.set_ylim(0, 1)
    ax.set_xlabel('Skills')
    ax.set_xticks(idxs)
    ax.set_xticklabels(xticklabels)


def class_metrics_barplot(runs, metrics, labels, sim):
    """
    :param runs:
    :param metrics:
    :param labels
    :param sim
    """
    fig, (ax1, ax2, ax3, ax4) = plt.subplots(1, 4, figsize=(18, 5))
    grouped_barplot(ax1, runs, metrics, labels[0], title='Accuracy')
    grouped_barplot(ax2, runs, metrics, labels[1], title='Precision', legend=False)
    grouped_barplot(ax3, runs, metrics, labels[2], title='Recall', legend=False)
    grouped_barplot(ax4, runs, metrics, labels[3], title='F1 score', legend=False)
    fig.legend(loc='lower center', ncol=2)
    fig.subplots_adjust(bottom=0.25)
    fig.suptitle(sim, fontweight='bold')


def metric_per_question(n_questions, avg_metric, metric, tests, xytext, sim):
    """

    :param n_questions:
    :param avg_metric:
    :param metric:
    :param tests:
    :param xytext:
    :param sim:
    """
    # markers = ['o', 's', '^', 'D']
    line_markers = ['o-.', 's-',  '^--', 'D:']
    colors = ['C0', 'C1', 'C2', 'C3']

    for idx, el in enumerate(avg_metric):
        if n_questions < 20:
            m = 2
        elif n_questions < 50:
            m = 5
        else:
            m = 20

        markers_on = np.arange(n_questions, step=m)
        plt.plot(avg_metric[idx], line_markers[idx], color=colors[idx], label=tests[idx], markevery=markers_on)
        # plt.plot(np.arange(n_questions)[::m][1:], avg_metric[idx][::m][1:], markers[idx], color=colors[idx])

        # for x, y in zip(np.arange(n_questions)[::m][1:], avg_metric[idx][::m][1:]):
        #     label = "{:.2f}".format(y)
        #
        #     plt.annotate(label,  # this is the text
        #                  (x, y),  # this is the point to label
        #                  textcoords="offset points",  # how to position the text
        #                  xytext=xytext[idx],  # distance from text to points (x,y)
        #                  ha='center')

    plt.xlabel('Number of questions')
    plt.ylabel(metric)
    plt.xticks(markers_on)

    # plt.yscale('log')
    if metric == "Accuracy":
        # plt.ylim(0.49, 1.01)
        plt.legend(loc='lower right')
    else:
        # plt.ylim(-0.01, 0.26)
        plt.legend(loc='upper right')
    # else:
    #     plt.ylim()

    plt.grid()

    plt.title(sim, fontweight='bold', pad='30')
    plt.tight_layout()
