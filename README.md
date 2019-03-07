# DS4NER Introduction

DS4NER is a simple, customizable implementation of Distant Supervision for Named entity recognition. DS4NER is designed for preparing training data for Named Entity Recognition.
    
* Efficient data crawling for sentence preparation based on input entity list
* Automatic labeling of training sentences based on input entity list
* Generate feature sets and labeled data for sequence labeling (such as CRF++)
* Written in Java
* Can specify source other than Web
* Ready for error analysis
* Available as an open source software


## Installation
* Requirements Java JRE or JDK 8 (does not support JRE/JDK 9


## Getting started
**Data preparation and model training:**

`training.cmd (for Windows) or training.sh (for Linux)`

> This batch includes Pre-Processing, Automatic Labeling, Dictionary Mining, Feature Generation and CRF Training.
> The input is two files: Corpus.txt and Seeds.txt under the "Corpus\Training directory" where the former is the sentence corpus needs to be labeled while the later is a list of seed entities.
> The output is the NER model, "Corpus.model", in the "Corpus\Training" directory.


**Use the trained NER model to test unlabeled test documents**

`extractor.cmd or extractor.sh`

> This batch file uses the trained model "Corpus.model" in the "Corpus\Training" directory and the test corpus "UnLabeledExtractorCorpus.txt" in the "Corpus\Testing" directory as input. Recognized named entities will be labeled with paired <NE></NE> tags with a new file name UnLabeledExtractorCorpus_Output.txt in the same "Corpus\Testing" directory.

**Model testing and performance evaluation with labeled test set**

`evaluation.cmd or evaluation.sh`

> This batch file uses the trained model "Corpus.model" in the "Corpus\Training" directory and the test corpus "LabeledExtractorCorpus.txt" in the "Corpus\Testing" directory as input. The output is shown in "WorkFolder\Eva_Exact" and "WorkFolder\Eva_Partial" directories, representing the performance evaluation in terms of exact match and partial match. Note that the batch also includes Pre-Processing, Feature Generation, CRF Testing.