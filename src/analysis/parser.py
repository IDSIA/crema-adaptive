import re

import numpy as np


def parse_predicted_probabilities(text_file):
    """

    :param text_file:
    :return predicted_profiles:
    """
    lines = text_file.readlines()
    text_file.close()

    probabilities = []

    for l in lines:
        skills_probs = re.findall("{+(.*?)}+", l)
        skills_probs = skills_probs[::2]

        skills_probabilites = []

        for skill_probs in skills_probs:
            rounded_skill_probs = np.round(np.array(skill_probs.split(',')).astype(np.float), 2)
            skills_probabilites.append(rounded_skill_probs.tolist())

        probabilities.append(skills_probabilites)

    return np.array(probabilities)


def parse_profiles(text_file):
    """

    :param text_file:
    :return observed_profiles:
    """
    lines = text_file.readlines()
    text_file.close()

    probabilities = []

    for l in lines:
        skills_prob = re.findall("{+(.*?)}+", l)

        rounded_skills_prob = np.round(np.array(skills_prob[0].split(',')).astype(np.float), 2).tolist()

        probabilities.append(rounded_skills_prob)

    return np.array(probabilities)
