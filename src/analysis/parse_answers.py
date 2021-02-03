import numpy as np
import pandas as pd
import re

simulations = np.arange(1)

for sim in simulations:

    answers_file = "../../output/adaptive_entropy_test/sim_" + str(sim) + "/answers.txt"
    keys_file = "../../adaptive/keys.txt"

    answers = np.loadtxt(answers_file, delimiter=', ', usecols=np.arange(1, 81))
    question_skill = np.loadtxt(keys_file, delimiter=' ', usecols=(0, 2), dtype=str)

    skills = np.unique(question_skill[:, 1])
    all_skills = question_skill[:, 1]
    questions = question_skill[:, 0]

    question_skill_df = pd.DataFrame(question_skill)
    question_skill_dict = question_skill_df.groupby(1).apply(lambda g: g[0].values.tolist()).to_dict()

    for skill in skills:
        skill_file = "../../adaptive/%s.csv" % skill

        a = answers[:, np.array(question_skill_dict[skill], dtype=np.int32)].astype(np.int32)

        header = np.char.add(np.array(['Q']),np.array(question_skill_dict[skill], dtype=str))

        string_header = np.array2string(header)[2:-2]
        string_header = re.compile(r"[\\]*'").sub("", string_header)
        string_header = re.compile(r"\n").sub('', string_header).replace(' ', ', ')

        np.savetxt(skill_file, a, delimiter=', ', newline='\n', header=string_header, fmt='%d', comments='')
