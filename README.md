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


**Use the trained NER model to test unlabeled test documents:**

`extractor.cmd or extractor.sh`

> This batch file uses the trained model "Corpus.model" in the "Corpus\Training" directory and the test corpus "UnLabeledExtractorCorpus.txt" in the "Corpus\Testing" directory as input. Recognized named entities will be labeled with paired <NE></NE> tags with a new file name UnLabeledExtractorCorpus_Output.txt in the same "Corpus\Testing" directory.

**Model testing and performance evaluation with labeled test set:**

`evaluation.cmd or evaluation.sh`

> This batch file uses the trained model "Corpus.model" in the "Corpus\Training" directory and the test corpus "LabeledExtractorCorpus.txt" in the "Corpus\Testing" directory as input. The output is shown in "WorkFolder\Eva_Exact" and "WorkFolder\Eva_Partial" directories, representing the performance evaluation in terms of exact match and partial match. Note that the batch also includes Pre-Processing, Feature Generation, CRF Testing.

## File formats
The main input is a corpus with UTF=8 encoding, representing the corpus.

**Seeds file: Every line is an entity**
```
王建民
林書豪
李安
```

**Corpus: Each line represents a sentence**

```
郭泓志、羅嘉仁也可能搶進最後名單，還有18歲超級新秀曾仁和等，都是重點討論人選。
猿隊甫給「小飛機」陳冠任一紙3年總值936萬元的合約，正好成為周思齊的最佳比較指標。
```

**Testing corpus: Use <NE>, </NE> tag pairs for labeling recognized entities**

`在今年全英羽球公開賽前，「世界球后」<NE>戴資穎</NE>的世界排名積分，僅領先第2名、日本好手<NE>山口茜</NE>5826分，理論上尋求衛冕的<NE>戴資穎</NE>，是有可能在打不好的情況下，被<NE>山口茜</NE>取代球后寶座，不過這一切隨著<NE>戴資穎</NE>順利闖進今年全英羽球公開賽 4強而宣告破滅，<NE>戴資穎</NE>確定將續坐球后寶座。`

Note: Training, testing corpus could not contain `<NE>`、`</NE>` and `<Separator>` for they are reserved keywords. Change the entity start and end tags  in config.ini ([PreProcessing] section: Sentence, Entity_Start, Entity_End).
Config.ini contains the following parameters:

| Name               | Description                                            |
| -------------------| -------------------------------------------------------|
| CorpusDir          | Corpus directory                                       |
| CorpusTraining     | The directory name for training corpus                 |
| CorpusTesting      | The directory name for testing corpus                  |
| WorkFolder         | Working directory, default: WorkFolder                 |
| Entity_Start       | Start of entity, default: `<NE>`                         |
| Entity_End         | End of entity, default: `</NE>`                          |




