# !/bin/bash

# Extract named entity from unlabeled data
java -cp NER.jar Testing.Extractor -strModel Corpus.model -strInput UnLabeledCorpus.txt -strOutput UnLabeledCorpus_Output.txt
