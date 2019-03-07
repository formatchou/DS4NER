REM Performance evaluation
java -cp NER.jar PrepareData.GenFeature -strInput_L LabeledCorpus.txt -strOutput_F LabeledCorpus_F.txt -strType Testing -bPreProcessing True
java -cp NER.jar Testing.Evaluation -strModel Corpus.model -strMethod Exact -strOutput_Dir Eva_Exact
java -cp NER.jar Testing.Evaluation -strModel Corpus.model -strMethod Partial -strOutput_Dir Eva_Partial

pause
