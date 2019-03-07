# !/bin/bash

# Prepare Data
java -cp NER.jar PrepareData.AutoLabeling -strInput_S Corpus.txt -strSeeds_S Seeds.txt -strOutput_L Corpus_L.txt -bFilterNegExamples True -bPreProcessing True
java -cp NER.jar PrepareData.MineDict -strInput_L Corpus_L.txt -strMethod Supp -fThreshold 0.5f
java -cp NER.jar PrepareData.GenFeature -strInput_L Corpus_L.txt -strOutput_F Corpus_F.txt -strType Training -bPreProcessing False

# Train Model via CRF++
java -cp NER.jar Training.CRF -strInput_F Corpus_F.txt -strModel Corpus.model

