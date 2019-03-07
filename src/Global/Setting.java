package Global;
import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ini4j.Ini;

public class Setting
{
	public static Logger MyLog = LogManager.getLogger(Setting.class);
	public static String strType = "IDE"; // NER Tool Execution Model: Console or IDE
	public static String strVersion = "NCU WIDM DS4NER Kit v1.03 Last Update:2018-07-09";
	
	// INI
	public static Ini MyINI = new Ini();
	public static String strOS = ""; // Linux or Windows
	
	// Global
	public static String strSeparator_Sentence = "";
	public static String strSeparator_Tab = "";
	public static File dirWorkFolder;
	public static File dirCorpus;
	public static File dirCorpusTraining;
	public static File dirCorpusTesting;
	public static File dirDictionary;
	public static File dirTraining;
	public static File dirTesting;
	public static File dirBaseline;
	public static int iThread = 1;
	
	// PreProcessing
	public static String NewLine = System.getProperty("line.separator");
	
	// "<>＜＞/’" for html tag and "of" in English, e.g.: Mary's Book
	public static Pattern patternEngNum= Pattern.compile("[0-9a-z<>/]+", Pattern.CASE_INSENSITIVE);
	public static Pattern patternSymbol = Pattern.compile("[～！＠＃＄％︿＆＊＋－＿＝｜；:，、。？／＼．±…⋯᠁]+");
	public static Pattern patternEMail = Pattern.compile("^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-z0-9-]+\\.)+[a-z]{2,6}$", Pattern.CASE_INSENSITIVE);
	public static Pattern patternUrl = Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", Pattern.CASE_INSENSITIVE);

	public static Pattern patternSplitSymbol = Pattern.compile("[；，。！？]+");
	public static Pattern patternSplitSpace = Pattern.compile("[^0-9a-z<>＜＞]{1}﹍[^0-9a-z<>＜＞]{1}", Pattern.CASE_INSENSITIVE);
	
	// English letters only, does not contain numbers, English letters + number: 0021-007E
	public static Pattern patternEnglish = Pattern.compile("[\\u0041-\\u005A|\\u0061-\\u007A]+"); 
	public static Pattern patternChinese = Pattern.compile("[\\u2E80-\\u2FDF|\\u3400-\\u4DBF|\\u4E00-\\u9FFF]+");
	public static Pattern patternJapanese = Pattern.compile("[\\u3040-\\u30FF|\\u31F0-\\u31FF]+");
	public static Pattern patternKorean = Pattern.compile("[\\u1100-\\u11FF|\\u3130-\\u318F|\\uAC00-\\uD7AF]+");
	public static Pattern patternNumber = Pattern.compile("[0-9]+");
	
	public static String strSeparator = "<SPLIT>";
	public static String strSeparator_Extractor = "<EXTRACTOR>";
	public static String strEntity_Start = "<NE>";
	public static String strEntity_End = "</NE>";
	public static int iSubSentence_Length_Min = 3;
	public static int iSubSentence_Length_Max = 80;
	public static float fNoneCJK_Threshold = 0.5f; // a segmentation should contain less than this percentage of symbols and numbers
	
	// DataPreparation
	// |seed| < this value = short seed, this value ≦ |seed| < iEntity_Length_Min_Long = medium seed
	public static int iEntity_Length_Min_Medium = 0;
	public static int iEntity_Length_Min_Long = 0;			// this value ≦ |seed| = long seed
	// Labeling strategy contains "LSH" then LSH will enable, e.g.: Exact-LSH or LCS+LSH
	public static String strLabeling_Strategy_Long = "";	// Excat or LCS
	public static String strLabeling_Strategy_Medium = "";	// Exact or Guillemet
	public static String strLabeling_Strategy_Short = "";	// Ignore or Exact or Guillemet
	public static String strFiltering = "";				// None or First or Last or FirstLast or Core
	public static String strLongSeedsRanking = "Length";	// Length or Similarity
	public static boolean bMergeNeighbor = true;			// Merge Neighbor Entities, e.g.: <NE>A</NE><NE>B</NE> → <NE>AB</NE>
	public static String strFeature_Type = "Dictionary";	// Context or Dictionary
	
	// TriTraining
	public static int iClassifier_Count = 3;
	public static File dirTriTraining;
	public static File dirTriTraining_ErrorRate;
	public static File dirTriTraining_Model;
	public static File dirTriTraining_Training;
	public static File dirTriTraining_Evaluation_Exact;
	public static File dirTriTraining_Evaluation_Partial;
	public static float fCoLabeling_Threshold = 0.5f; // the threshold of co-labeling θ, default 0.5
	public static float fDelta = 1.0f;
	public static float fPickMultiple = 0.0f; // Pick |PickMultiple * iUpperBound| from U to prepare newly training data
	public static float fPostiveRate = -1.0f; // Postive/Negative example rate in newly examples, -1f = Random
	public static int iTesting_TopN = 3;
	
	// CRFPackage
	public static String strCRFTraining = "";
	public static String strCRFTraining_Separator = "";
	public static String strCRFTesting = "";
	public static String strCRFTesting_Separator = "";
	
	public static boolean Initialize(String strConfig) throws Exception
	{
		Setting.MyLog.info("=======================================================");
		Setting.MyLog.info(Setting.strVersion);
		boolean bCheckArguments = false;
		
		try
		{
			// INI
			if(MyINI.isEmpty())
			{
				if(strConfig.length() > 0)
				{
					MyINI.load(new File(strConfig));
				}
				else
				{
					MyINI.load(new File("config.ini"));
				}
				
				if(System.getProperty("os.name").toLowerCase().indexOf("win") != -1)
				{
					strOS = "Windows";
				}
				else
				{
					strOS = "Linux";
				}
			}
			
			// Global
			strSeparator_Sentence = getINIOption("Global", "Separator_Sentence").replace("\"", "");
			strSeparator_Tab = getINIOption("Global", "Separator_Tab").replace("\"", "");
			dirWorkFolder = new File(getINIOption("Global", "WorkFolder").replace("[separator]", File.separator));
			dirCorpus = new File(getINIOption("Global", "Corpus").replace("[separator]", File.separator));
			dirCorpusTraining = Lib.SubFileOrFolder(dirCorpus, getINIOption("Global", "CorpusTraining"));
			dirCorpusTesting = Lib.SubFileOrFolder(dirCorpus, getINIOption("Global", "CorpusTesting"));
			dirDictionary = Lib.SubFileOrFolder(dirWorkFolder, "Dictionary");
			dirTraining = Lib.SubFileOrFolder(dirWorkFolder, "Training");
			dirTesting = Lib.SubFileOrFolder(dirWorkFolder, "Testing");
			dirBaseline = Lib.SubFileOrFolder(dirWorkFolder, "Baseline");
			iThread = Integer.valueOf(getINIOption("Global", "Thread"));
			
			// PreProcessing
			strSeparator = getINIOption("PreProcessing", "Separator").toUpperCase();
			strSeparator_Extractor = getINIOption("PreProcessing", "Separator_Extractor").toUpperCase();
			strEntity_Start = getINIOption("PreProcessing", "Entity_Start").toUpperCase();
			strEntity_End = getINIOption("PreProcessing", "Entity_End").toUpperCase();
			iSubSentence_Length_Min = Integer.valueOf(getINIOption("PreProcessing", "SubSentence_Length_Min"));
			iSubSentence_Length_Max = Integer.valueOf(getINIOption("PreProcessing", "SubSentence_Length_Max"));
			fNoneCJK_Threshold = Float.valueOf(getINIOption("PreProcessing", "NoneCJK_Threshold"));
//			Lib.patternHtmlTag = Pattern.compile(Setting.strEntity_Start + "|" + Setting.strEntity_End);
			
			// DataPreparation
			iEntity_Length_Min_Medium = Integer.valueOf(getINIOption("DataPreparation", "Entity_Length_Min_Medium"));;
			iEntity_Length_Min_Long = Integer.valueOf(getINIOption("DataPreparation", "Entity_Length_Min_Long"));;
			strLabeling_Strategy_Long = getINIOption("DataPreparation", "Labeling_Strategy_Long");
			strLabeling_Strategy_Medium = getINIOption("DataPreparation", "Labeling_Strategy_Medium");
			strLabeling_Strategy_Short = getINIOption("DataPreparation", "Labeling_Strategy_Short");
			strFiltering = getINIOption("DataPreparation", "Filtering");
			strLongSeedsRanking = getINIOption("DataPreparation", "LongSeedsRanking");
			bMergeNeighbor = Boolean.valueOf(getINIOption("DataPreparation", "MergeNeighbor"));
			strFeature_Type = getINIOption("DataPreparation", "Feature_Type");
			
			// TriTraining
			iClassifier_Count = Integer.valueOf(getINIOption("TriTraining", "Classifier_Count"));
			dirTriTraining = Lib.SubFileOrFolder(dirWorkFolder, "TriTraining");
			dirTriTraining_ErrorRate = Lib.SubFileOrFolder(dirTriTraining, "ErrorRate");
			dirTriTraining_Model = Lib.SubFileOrFolder(dirTriTraining, "Model");
			dirTriTraining_Training = Lib.SubFileOrFolder(dirTriTraining, "Training");
			dirTriTraining_Evaluation_Exact = Lib.SubFileOrFolder(dirTriTraining, "Evaluation_Exact");
			dirTriTraining_Evaluation_Partial = Lib.SubFileOrFolder(dirTriTraining, "Evaluation_Partial");
			fCoLabeling_Threshold = Float.valueOf(getINIOption("TriTraining", "CoLabeling_Threshold"));
			fDelta = Float.valueOf(getINIOption("TriTraining", "Delta"));
			fPickMultiple = Float.valueOf(getINIOption("TriTraining", "PickMultiple"));
			fPostiveRate = Float.valueOf(getINIOption("TriTraining", "PostiveRate"));
			iTesting_TopN = Integer.valueOf(getINIOption("TriTraining", "Testing_TopN"));
			
			// CRFPackage
			strCRFTraining = getINIOption("CRF++", "Training").replace("[separator]", File.separator);
			strCRFTraining_Separator = getINIOption("CRF++", "Training_Separator").replace("\"", "");
			strCRFTesting = getINIOption("CRF++", "Testing").replace("[separator]", File.separator);
			strCRFTesting_Separator = getINIOption("CRF++", "Testing_Separator").replace("\"", "");
			
			if(strOS.equals("Windows"))
			{
				strCRFTraining = strCRFTraining.replace("[CRFLocation]", getINIOption("CRF++", "CRFLocation") + File.separator);
				strCRFTesting = strCRFTesting.replace("[CRFLocation]", getINIOption("CRF++", "CRFLocation") + File.separator);
			}
			else
			{
				strCRFTraining = strCRFTraining.replace("[CRFLocation]", "");
				strCRFTesting = strCRFTesting.replace("[CRFLocation]", "");
			}
			
			bCheckArguments = true;
			Setting.MyLog.info("NER Tool Execution Mode = " + strType);
			return bCheckArguments;
		}
		catch(Exception e)
		{
			Setting.MyLog.warn("Initialize Error!");
			Setting.MyLog.warn(e.toString());
			return bCheckArguments;
		}
	}
	
	private static String getINIOption(String strSection, String strOption)
	{
		return MyINI.get(strSection, strOption).split(";")[0].trim();
	}
	
	private static File getTriTrainingRun() throws Exception
	{
		if(!dirTriTraining.exists())
		{
			return Lib.SubFileOrFolder(dirTriTraining, "Run01");
		}
		else
		{
			ArrayList<String> alFolder = Lib.GetFolderNameList(dirTriTraining);
			
			int iMaxRunID = 0;
			for(String s:alFolder)
			{	
				if(s.toLowerCase().contains("run"))
				{
					s = s.toLowerCase().replace("run", "");
					int iRunID = Integer.valueOf(s);
					
					if(iRunID > iMaxRunID)
					{
						iMaxRunID = iRunID;
					}
				}
			}
			
			return Lib.SubFileOrFolder(dirTriTraining, "Run" + String.format("%02d", iMaxRunID));
		}
	}
}
