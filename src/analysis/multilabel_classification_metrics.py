import itertools
from typing import List

import matplotlib.pyplot as plt
import numpy as np
from scipy.spatial.distance import hamming
from sklearn.metrics import confusion_matrix

from src.analysis.utils import save_visualisation, one_hot_encode


def brier_multicategory(targets, probs):
    """
    (reference: https://en.wikipedia.org/wiki/Brier_score)
    :param targets:
    :param probs:
    :return brier_score:
    """
    n_classes = targets.shape[1]
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
    hamming_distance = hamming_loss * len(observations)

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


def plot_confusion_matrix(ax, observations, predictions, labels, title):
    """
    This function prints and plots the confusion matrix.
    Normalization can be applied by setting `normalize=True`.
    :param observations:
    :param predictions:
    :param labels:
    :param title:
    :return cm, img:
    """
    cm = confusion_matrix(observations, predictions, labels=labels)
    np.set_printoptions(precision=2)

    cmap = plt.cm.Blues
    normalize = False

    if normalize:
        cm = cm.astype('float') / cm.sum(axis=1)[:, np.newaxis]

    ax.set_title(title)

    img = ax.imshow(cm, interpolation='nearest', cmap=cmap)
    ax.set_xticks(labels)
    ax.set_yticks(labels)
    ax.tick_params(labelrotation=45)

    fmt = '.2f' if normalize else 'd'
    thresh = cm.max() / 2.
    for i, j in itertools.product(range(cm.shape[0]), range(cm.shape[1])):
        ax.text(j, i, format(cm[i, j], fmt),
                 horizontalalignment="center",
                 color="white" if cm[i, j] > thresh else "black")

    if title == 'adaptive_entropy':
        ax.set_ylabel('Observed label')
    ax.set_xlabel('Predicted label')

    return cm, img


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
    ax.set_xlabel(title)
    ax.set_xticks(idxs)
    ax.set_xticklabels(xticklabels)


def class_metrics_barplot(runs, metrics):
    """
    :param runs:
    :param metrics:
    :return:
    """
    fig, (ax1, ax2, ax3, ax4) = plt.subplots(1, 4, figsize=(18, 4))
    grouped_barplot(ax1, runs, metrics, dict(X0='accuracy/X0',
                                             X1='accuracy/X1',
                                             X2='accuracy/X2',
                                             X3='accuracy/X3'),
                    title='Accuracy')
    grouped_barplot(ax2, runs, metrics, dict(X0='precision/X0',
                                             X1='precision/X1',
                                             X2='precision/X2',
                                             X3='precision/X3'),
                    title='Precision', legend=False)
    grouped_barplot(ax3, runs, metrics, dict(X0='recall/X0',
                                             X1='recall/X1',
                                             X2='recall/X2',
                                             X3='recall/X3'),
                    title='Recall', legend=False)
    grouped_barplot(ax4, runs, metrics, dict(X0='f1_score/X0',
                                             X1='f1_score/X1',
                                             X2='f1_score/X2',
                                             X3='f1_score/X3'),
                    title='F1 score', legend=False)
    fig.legend(loc='lower center', ncol=2)
    fig.subplots_adjust(bottom=0.25)


def metric_per_question(avg_metric, metric, tests, xytext):
    """

    :param avg_metric:
    :param metric:
    :return:
    """
    plt.plot(avg_metric[0], color='C0', label=tests[0])
    plt.plot(np.arange(80)[::10][1:], avg_metric[0][::10][1:], 'o', color='C0')
    for x, y in zip(np.arange(80)[::10][1:], avg_metric[0][::10][1:]):
        label = "{:.2f}".format(y)

        plt.annotate(label,  # this is the text
                     (x, y),  # this is the point to label
                     textcoords="offset points",  # how to position the text
                     xytext=xytext[0],  # distance from text to points (x,y)
                     ha='center')
    plt.plot(avg_metric[1], '--', color='C1', label=tests[1])
    plt.plot(np.arange(80)[::10][1:], avg_metric[1][::10][1:], 's', color='C1')
    for x, y in zip(np.arange(80)[::10][1:], avg_metric[1][::10][1:]):
        label = "{:.2f}".format(y)

        plt.annotate(label,  # this is the text
                     (x, y),  # this is the point to label
                     textcoords="offset points",  # how to position the text
                     xytext=xytext[1],  # distance from text to points (x,y)
                     ha='center')
    plt.xlabel('Number of questions')
    plt.ylabel(metric)
    plt.xlim(0, 80)
    plt.ylim(0, 1)
    plt.legend()
    plt.grid()
