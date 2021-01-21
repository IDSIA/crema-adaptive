# CREMA - Adaptive experiments

### Description
An application of the the [CREMA](https://github.com/IDSIA/crema) Library.

### Authors

**Giorgia Adorni** - [GiorgiaAuroraAdorni](https://github.com/GiorgiaAuroraAdorni) - giorgia.adorni@idsia.ch  
**Alessandro Antonucci** - [alessandroantonucci](https://github.com/alessandroantonucci) - alessandro@idsia.ch  
**Claudio Bonesana** - [cbonesana](https://github.com/cbonesana) - claudio.bonesana@idsia.ch  
**Francesca Mangili** - [mangilif](https://github.com/mangilif) - francesca@idsia.ch

### Usage

In the file `AdaptiveTests` is contained the code to make inference using the *CREMA* libray.

In the files `BNsAdaptiveSurveyTest` and `CNsAdaptiveSurveyTest` are contained examples of usage of the library:
a survey test for a single student is created, each students will have its lists of questions, its personal test,
and its answer sheet. At the end the skills of the students are returned.
The survey is performed using a Bayesian network, in the first case, while a Credal one, in the second.

The files `BNsAdaptiveSurveySimulation` and `CNsAdaptiveSurveySimulation` perform a simulation of a case study in
order to measure the performance of the model. In fact, the only difference from the two previous "Tests" is that the
answers to the questions are sampled using the probabilities of answering correctly or not to a given question. 
The student's real answers are used only as final validation.  

### Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.
