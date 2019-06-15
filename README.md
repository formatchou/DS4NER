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
| Entity_Start       | Start of entity, default:`<NE>`                         |
| Entity_End         | End of entity, default:`</NE>`                          |

## Automatic labeling
`Java –cp NER.jar PrepareData.AutoLabeling -strInput_S <Input Corpus> -strSeeds_S <Seed File> -strOutput_L <Output Corpus> -bFilterNegExamples True -bPreProcessing True`

> Automatic labeling use the Corpus.txt and Seeds.txt file in the "Corpus\Training" directory for unlabeled training corpus and seed entities. The output is labeled corpus in the WorkFolder\Training\Corpus_L.txt。
> 
* Use "-strInput" or "-strSeeds" to chan
* ge Corpus and names in the entity list. 
* Use "-bFilterNegExamples True" to remove sentences not containing entities. 
* Use "-bPreProcessing True" to show the preprocessing time. 
* When "-bPreProcessing True" then the corpus source is "Corpus\Training"; otherwise "-bPreProcessing False" then source is "Workfolder\Training".

## Dictionary mining
`java -cp NER.jar PrepareData.MineDict -strInput_L <Labeled Corpus> -strMethod Supp -fThreshold 0.5f`

* Given automatically labeled Corpus_L.txt under "WorkFolder\Training\" directory, output 4 dictionaries under the "WorkFolder\Dictionary".
* Use "-strInput_L", "-strMethod", and "-fThreshold" to specify input labeled corpus, feature selection method (Supp、Conf、HMCS)  and the parameter。 

## Feature generation
`java -cp NER.jar PrepareData.GenFeature -strInput_L <Labeled Corpus>　-strOutput_F <Labeled Matrix> -strType Training -bPreProcessing False`

* 特徵擷取使用「WorkFolder\Training」目錄下的Corpus_L.txt，以及「WorkFolder\Dictionary」 下的字典檔做為輸入，產生特徵矩陣標記資料檔案Corpus_F.txt。
* 可以透過-strInput_L、-strOutput_F、strType改變<Labeled Corpus>輸入及<Labeled Matrix>輸出檔名以及此特徵矩陣標記檔案的用途(Training、Testing…)。輸入檔案為包含標記的文件。
* -bPreProcessing: 輸入檔案是否需執行前處理，依據-StrType以及-bPreProcessing兩個參數決定資料來源，細節請參考文件。


## Model training & testing with CRF++
`java -cp NER.jar Training.CRF -strInput_F <Labeled Matrix> -strModel <Model Name>`

* 訓練模型使用Automatic labeling產出的已標記資料，預設輸入檔案預設位置為:「WorkFolder\Training\Corpus_F.txt」，輸出模型檔案位置為「WorkFolder\Training\Corpus.model
* strInput_F: 輸入檔案格式為Feature Generation的輸出Labeled Matrix，預設檔案放置目錄為「WorkFolder\Training」。
* strModel: 輸出模型的檔案名稱，使用方法「-strModel Corpus.model」。預設輸出檔案放置目錄為「WorkFolder\Training」。
 

`java -cp NER.jar PrepareData.GenFeature -strInput_L <Labeled Corpus> -strOutput_F <Labeled Matrix> -strType Testing -bPreProcessing True`

* strInput_L: 輸入檔案格式為Feature Generation的輸出<Labeled Matrix>，預設位置為: 「Corpus\Testing\TestCorpus_F.txt」。
* strOutput_F: 轉成特徵矩陣標記格式的輸出，預設輸出檔案放置目錄為「WorkFolder\Testing」。
* -bPreProcessing: 輸入檔案是否需執行前處理，依據-StrType以及-bPreProcessing兩個參數決定資料來源，細節請參考文件。

`java -cp NER.jar Testing.Evaluation -strModel Corpus.model -strMethod <Method> -strOutput_Dir <Folder Name>`

* strMethod: 評估方式有兩種分別是「Exact」和「Partial」，使用方法「- strMethod Partial or Exact」。
* strOutput_Dir: 評估結果的輸出目錄，使用方法「-strOutput_Dir Eva_Partial」。預設輸出目錄位於「WorkFolder」目錄下。

## Data crawling
`java -cp NER.jar Crawler.WebCrawler -strSeeds <Seed File> -strOutput <Output File>`

* 爬取語料庫: 若使用者有興趣自行收集資料，DS4NER亦提供一支Crawler (網路爬蟲)，可自Google的搜尋結果中擷取包含seed的句子作為語料。
* strSeeds: 關鍵字清單的檔案名稱，檔案格式參考Seed file。預設實體列表檔案放置路徑為「Corpus\Training」。
* strOutput: 輸出語料庫的檔案名稱，檔案格式參考Corpus format。預設輸出檔案放置路徑為「Corpus\Training」。

## References
* Chien-Lung Chou, Chia-Hui Chang, Yuan-Hao Lin, On the construction of NER model training tools based distant supervision, In preparation
* Chien-Lung Chou, Chia-Hui Chang, Mining features for web ner model construction based on distant learning. IALP 2017
* Chien-Lung Chou, Chia-Hui Chang, Ya-Yun Huang, Boosted Web Named Entities Recognition via Tri-Training, Transactions on Asian and Low-Resource Language Information Processing, Volume 16 Issue 2, November 2016.
* Ya-Yun Huang, Chia-Hui Chang, Chien-Lung Chou, A Tool for Web NER Model Generation Using Search Snippets of Known Entities (基於已知名稱搜尋結果的網路實體辨識模型建立工具), ROCLING 2015.
* Chien-Lung Chou, Chia-Hui Chang: Named Entity Extraction via Automatic Labeling and Tri-training: Comparison of Selection Methods. AIRS 2014: 244-25.
