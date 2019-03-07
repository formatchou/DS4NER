package PrepareData;
import Global.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import org.apache.commons.cli.*;

public class AutoLabeling
{
	static String strInput_S = "Address_S.txt";		// Corpus_S
	static String strSeeds_S= "Address_Seeds_S.txt";
	static String strSeeds_Core = "";				// Corpus_Core
	static String strOutput_L = "Address_Partial_L.txt";		// Corpus_L
	static boolean bFilterNegExamples = true;
	static boolean bPreProcessing = false;
	static File fInput_S;
	static File fSeeds_S;
	static File fSeeds_Core;
	static File fOutput_L;
	static File fOutput_Report;
	
	static ArrayList<String> alInput_S = new ArrayList<String>();
	static ArrayList<String> alSeeds_S = new ArrayList<String>();
	static ArrayList<String> alSeeds_Core = new ArrayList<String>();
	static HashMap<String, String> hmSeeds_Core = new HashMap<String, String>();
	static StrFloatPair pairSeeds_Short = new StrFloatPair();
	static StrFloatPair pairSeeds_Medium = new StrFloatPair();
	static StrFloatPair pairSeeds_Long = new StrFloatPair();
	static StrIntPair[] pairLabeledEntity;
	static String strGuillemet_HashTag = "#＃";
	static String strGuillemet_Left = "`’｀'“‘'‵〝\"[({⟨‹«<〔『《【＜（「";
	static String strGuillemet_Right = "`’｀'”’'′〞\"])}⟩›»>〕』》】＞）」";
	static int iCore = 3; // each seed has n core names
	
	// LSH parameters
	static LSH lshShort;						// LSH for short seeds
	static LSH lshMedium;						// LSH for medium seeds
	static LSH lshLong;							// LSH for long seeds
	static float fSimThreshold = 0.7f;			// Seed similarity threshold
	static int iMaxGap = 2;					// Max gaps between any two tokens of partial match
	
	public static void main(String[] args) throws Exception
	{	
		String strConfig = "";
		if(args.length > 0 && args[0].toUpperCase().startsWith("-CONFIG"))
		{
			strConfig = args[1];
		}
		
		if(Setting.Initialize(strConfig) && ParseArguments(args))
		{
			Start();
		}
		
		Setting.MyLog.info("========================= End =========================");
		Setting.MyLog.info("");
		System.gc();
		System.exit(0);
	}
	
	private static boolean ParseArguments(String[] args) throws Exception
	{
		Setting.MyLog.info("======================== Start ========================");
		Setting.MyLog.info("Now execute: " + System.getProperty("sun.java.command"));
		
		boolean bParseArguments = true;
		HelpFormatter hf = new HelpFormatter();
		
		Options opts = new Options();
		opts.addOption(Option.builder("h").longOpt("help").desc("Shows argument examples").build());
		opts.addOption(Option.builder("Config").required(false).hasArg().build());
		opts.addOption(Option.builder("strInput_S").desc("<WorkFolder\\Training\\>strInput_S.txt").required(true).hasArg().build());
		opts.addOption(Option.builder("strSeeds_S").desc("<WorkFolder\\Training\\>strSeeds_S.txt").required(true).hasArg().build());
		opts.addOption(Option.builder("strSeeds_Core").desc("<WorkFolder\\Training\\>strSeeds_Core.txt").required(false).hasArg().build());
		opts.addOption(Option.builder("strOutput_L").desc("<WorkFolder\\Training\\>strOutput_L.txt").required(true).hasArg().build());
		opts.addOption(Option.builder("bFilterNegExamples").desc("True or False").required(true).hasArg().build());
		opts.addOption(Option.builder("bPreProcessing").desc("True or False").required(true).hasArg().build());
		
		try
		{
			if(Setting.strType.toUpperCase().equals("IDE"))
			{
				if(bPreProcessing)
				{
					fInput_S = Lib.SubFileOrFolder(Setting.dirCorpusTraining, strInput_S);
					fSeeds_S = Lib.SubFileOrFolder(Setting.dirCorpusTraining, strSeeds_S);
				}
				else
				{
					fInput_S = Lib.SubFileOrFolder(Setting.dirTraining, strInput_S);
					fSeeds_S = Lib.SubFileOrFolder(Setting.dirTraining, strSeeds_S);
				}
				
				fSeeds_Core = Lib.SubFileOrFolder(Setting.dirTraining, strSeeds_Core);
				fOutput_L = Lib.SubFileOrFolder(Setting.dirTraining, strOutput_L);
				fOutput_Report = Lib.GetFileWithSuffix(fOutput_L, "_Report");
			}
			else
			{
				DefaultParser parser = new DefaultParser();
				CommandLine cmd = parser.parse(opts, args);
				
				strInput_S = cmd.getOptionValue("strInput_S");
				strSeeds_S= cmd.getOptionValue("strSeeds_S");
				bPreProcessing = Boolean.valueOf(cmd.getOptionValue("bPreProcessing"));
				
				if(bPreProcessing)
				{
					fInput_S = Lib.SubFileOrFolder(Setting.dirCorpusTraining, cmd.getOptionValue("strInput_S"));
					fSeeds_S = Lib.SubFileOrFolder(Setting.dirCorpusTraining, cmd.getOptionValue("strSeeds_S"));
				}
				else
				{
					fInput_S = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strInput_S"));
					fSeeds_S = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strSeeds_S"));
				}
				
				fOutput_L = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strOutput_L"));
				bFilterNegExamples = Boolean.valueOf(cmd.getOptionValue("bFilterNegExamples"));
				fOutput_Report = Lib.GetFileWithSuffix(fOutput_L, "_Report");
				
				if(cmd.hasOption("strSeeds_Core"))
				{
					fSeeds_Core = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strSeeds_Core"));
				}
			}
			
			if(bParseArguments)
			{	
				Setting.MyLog.info("fInput_S = " + fInput_S.getCanonicalPath());
				Setting.MyLog.info("fSeeds_S = " + fSeeds_S.getCanonicalPath());
				if(fSeeds_Core != null)
				{
					Setting.MyLog.info("fSeeds_Core = " + fSeeds_Core.getCanonicalPath());
				}
				Setting.MyLog.info("fOutput_L = " + fOutput_L.getCanonicalPath());
				Setting.MyLog.info("bFilterNegExamples = " + bFilterNegExamples);
				Setting.MyLog.info("bPreProcessing = " + bPreProcessing);
			}
		}
		catch (Exception e)
		{
			bParseArguments = false;
			hf.printHelp("Argument examples", opts);
		}
		
		return bParseArguments;
	}
	
	private static void Start() throws Exception
	{
		Setting.MyLog.info("================= Main Function Start =================");
		
		long lStart = System.nanoTime();
		
		loadData();
		buildLSH();
		
		pairLabeledEntity = new StrIntPair[Setting.iThread];
		LabelingWorker[] worker = new LabelingWorker[Setting.iThread];
		ExecutorService es = Executors.newFixedThreadPool(Setting.iThread);
		
		for(int i = 0; i < Setting.iThread; i++)
		{
			pairLabeledEntity[i] = new StrIntPair();
			worker[i] = new LabelingWorker(i);
			es.execute(worker[i]);
		}
		
		Thread.sleep(3000);
		es.shutdown();
		
		while(true)
		{
			Thread.sleep(1000);
			if(Thread.activeCount() == 1) // all threads destroyed
			{
				ArrayList<String> alPOS = new ArrayList<String>(); 
				if(bFilterNegExamples)
				{
					Setting.MyLog.info("Start to filter out negative examples");
					alPOS = Lib.FilterNegExamples(alInput_S);
					Lib.SaveFile(alPOS, fOutput_L, false);
				}
				else
				{
					Lib.SaveFile(alInput_S, fOutput_L, false);
				}
				
				// for labeled entities report
				for(int i = 1; i < Setting.iThread; i++)
				{
					for(StrIntPair p:pairLabeledEntity[i])
					{
						pairLabeledEntity[0].addString(p.getString());
					}
				}
				
				ArrayList<String> alReport = new ArrayList<String>();
				
				if(bFilterNegExamples)
				{
					alReport.add("Sentence (POS examples only) = " + alPOS.size());
				}
				else
				{
					alReport.add("Sentence = " + alInput_S.size());
				}
				
				alReport.add("Labeled Entity = " + pairLabeledEntity[0].getIntSum());
				Lib.SaveFile(alReport, fOutput_Report, "", false);
				
				pairLabeledEntity[0].sortByInt(StrIntPair.SortBy.DESC);
				Lib.SaveFile(pairLabeledEntity[0], fOutput_Report, "", "all", true);
				
				break;
			}
		}
		
		System.out.println((int) ((System.nanoTime() - lStart) / 1e9));
	}
	
	private static void loadData() throws Exception
	{
		// PreProcessing
		if(bPreProcessing)
		{
			File fInput_Temp = fInput_S;
			File fSeeds_Temp = fSeeds_S;
			fInput_S = Lib.SubFileOrFolder(Setting.dirTraining, strInput_S.replaceAll("(?i).txt", "_S.txt"));
			fSeeds_S = Lib.SubFileOrFolder(Setting.dirTraining, strSeeds_S.replaceAll("(?i).txt", "_S.txt"));
			PreProcessing.Segmentation.Run(fInput_Temp, fInput_S, "training", false, false, false, false);
			PreProcessing.Segmentation.Run(fSeeds_Temp, fSeeds_S, "seeds", false, false, false, false);
		}
		
		// Load training data and seeds
		alInput_S = Lib.LoadArrayList(fInput_S, "UTF-8", false);
		alSeeds_S = Lib.LoadArrayList(fSeeds_S, "UTF-8", false);
		
		for(String s:alSeeds_S)
		{
			// long seeds
			if(Setting.iEntity_Length_Min_Long <= Lib.GetTokenLength(s))
			{
				pairSeeds_Long.addString(s);
			}
			// medium seeds
			else if(Setting.iEntity_Length_Min_Medium <= Lib.GetTokenLength(s) && Lib.GetTokenLength(s) < Setting.iEntity_Length_Min_Long)
			{
				pairSeeds_Medium.addString(s);
			}
			// short seeds
			else if(!Setting.strLabeling_Strategy_Short.toLowerCase().contains("ignore"))
			{
				pairSeeds_Short.addString(s);
			}
		}
		
		pairSeeds_Long.sortByTokenLength(StrFloatPair.SortBy.DESC);
		pairSeeds_Medium.sortByTokenLength(StrFloatPair.SortBy.DESC);
		pairSeeds_Short.sortByTokenLength(StrFloatPair.SortBy.DESC);
		
		// Load seed core names
		if(fSeeds_Core!= null && fSeeds_Core.exists() && fSeeds_Core.isFile())
		{
			String strTemp = "";
			alSeeds_Core = Lib.LoadArrayList(fSeeds_Core, "UTF-8", false);
			
			for(String s:alSeeds_Core)
			{
				if(s.split(Setting.strSeparator_Tab).length <= iCore + 1) // top n cores
				{
					for(int i = 1 ; i < s.split(Setting.strSeparator_Tab).length ; i++)
					{
						strTemp += s.split(Setting.strSeparator_Tab)[i] + Setting.strSeparator_Tab;
					}
				}
				else // merged core names
				{
					for(int i = iCore + 1 ; i < s.split(Setting.strSeparator_Tab).length ; i++)
					{
						strTemp += s.split(Setting.strSeparator_Tab)[i] + Setting.strSeparator_Tab;
					}
				}
				
				hmSeeds_Core.put(s.split(Setting.strSeparator_Tab)[0], strTemp);
				strTemp = "";
			}
		}
		else
		{
			Setting.MyLog.warn("Core file is not exist!");
		}
	}
	
	private static void buildLSH() throws Exception
	{
		if(Setting.strLabeling_Strategy_Long.toLowerCase().contains("lsh"))
		{
			Setting.MyLog.info("Start to build LSH for long seeds");
			if(Setting.strLabeling_Strategy_Long.toLowerCase().contains("exact"))
			{	
				lshLong = new LSH(pairSeeds_Long, Setting.iEntity_Length_Min_Long, 75, 2); // k-shingle = 6, bands = 50, rows = 3
			}
			else
			{
				lshLong = new LSH(pairSeeds_Long, 2, 50, 3); // k-shingle = 2, bands = 50, rows = 3
			}
		}
		
		if(Setting.strLabeling_Strategy_Medium.toLowerCase().contains("lsh"))
		{
			Setting.MyLog.info("Start to build LSH for medium seeds");
			if(Setting.strLabeling_Strategy_Medium.toLowerCase().contains("exact"))
			{
				lshMedium = new LSH(pairSeeds_Medium, Setting.iEntity_Length_Min_Medium, 50, 2); // k-shingle = 3, bands = 50, rows = 2
			}
			else
			{
				lshMedium = new LSH(pairSeeds_Medium, 2, 50, 2); // k-shingle = 2, bands = 50, rows = 2
			}
		}
		
		if(Setting.strLabeling_Strategy_Short.toLowerCase().contains("lsh"))
		{
			Setting.MyLog.info("Start to build LSH for short seeds");
			lshShort = new LSH(pairSeeds_Short, 1, 50, 1); // k-shingle = 1, bands = 50, rows = 1
		}
	}
	
	// ------------------------------
	
	private static class LabelingWorker implements Runnable
	{	
		private String strID;
		private int iID;
		private int iStart;
		private int iEnd;
		private StrFloatPair pairSeeds = new StrFloatPair();
		private String strSentence, strSeed, strLCS = "";
		private int iUnlabeledLength_Max = 0;
		private int iSeedLength_Min;
		
		private ArrayList<String> alMatched = new ArrayList<String>();
		private ArrayList<String> alEntity = new ArrayList<String>();
		
		public LabelingWorker(int id)
		{
			this.strID = String.valueOf(id);
			this.iID = id;
			this.iStart = id * alInput_S.size() / Setting.iThread;
			this.iEnd = (id + 1) * alInput_S.size() / Setting.iThread - 1;
			
			if(id == Setting.iThread - 1)
			{
				this.iEnd = alInput_S.size() - 1;
			}
		}
		
		public void run()
		{
			Setting.MyLog.info("Thread " + strID + " begins work (start index = " + iStart + "\t, end index = " + iEnd + ")");
			
			try
			{
				for(int i = iStart; i <= iEnd; i++)
				{
					strSentence = alInput_S.get(i);
					iUnlabeledLength_Max = Lib.GetTokenLength(strSentence);
					
					// long seeds
					parpareSeeds("long");
					for(int j = 0; j < pairSeeds.size(); j++)
					{
						strSeed = pairSeeds.get(j).getString();
						
						// long seeds support labeling strategy = exact or guillemet or LCS
						if(strSentence.contains(strSeed))
						{
							if(Setting.strLabeling_Strategy_Long.toLowerCase().contains("exact") || Setting.strLabeling_Strategy_Long.toLowerCase().contains("partial"))
							{	
								Labeling(strSeed, false);
							}
							else if(Setting.strLabeling_Strategy_Long.toLowerCase().contains("guillemet"))
							{
								Labeling(strSeed, true);
							}
							
						}
//						else if(Setting.strLabeling_Strategy_Long.toLowerCase().contains("partial")) // partial labeling
//						{
//							if(Filtering_FirstLast())
//							{	
//								if(calcSimilarity(strSentence, strSeed) >= fSimThreshold)
//								{	
//									strLCS = getLCS();
//									
//									if(strLCS.length() > 0 && Filtering_Core(strLCS))
//									{
//										Labeling(strLCS, false);
//									}
//								}
//							}
//						}
						
						if(iUnlabeledLength_Max < iSeedLength_Min)
						{
							break;
						}
					}
					
					// long seeds support labeling strategy = exact or guillemet or LCS
					if(Setting.strLabeling_Strategy_Long.toLowerCase().contains("partial"))
					{
						for(int j = 0; j < pairSeeds.size(); j++)
						{
							strSeed = pairSeeds.get(j).getString();
							
							if(Filtering_FirstLast())
							{	
								if(calcSimilarity(strSentence, strSeed) >= fSimThreshold)
								{	
									strLCS = getLCS();
									
									if(strLCS.length() > 0 && Filtering_Core(strLCS))
									{
										Labeling(strLCS, false);
									}
								}
							}
							
							if(iUnlabeledLength_Max < iSeedLength_Min)
							{
								break;
							}
						}
					}
					
					// medium seeds
					parpareSeeds("medium");
					for(int j = 0; j < pairSeeds.size(); j++)
					{
						strSeed = pairSeeds.get(j).getString();
						
						if(strSentence.toUpperCase().contains(strSeed.toUpperCase()))
						{
							if(Setting.strLabeling_Strategy_Medium.toLowerCase().contains("exact"))
							{
								Labeling(strSeed, false);
							}
							else if(Setting.strLabeling_Strategy_Medium.toLowerCase().contains("guillemet"))
							{
								Labeling(strSeed, true);
							}	
						}
						
						if(iUnlabeledLength_Max < iSeedLength_Min)
						{
							break;
						}
					}
					
					// short seeds
					parpareSeeds("short");
					for(int j = 0; j < pairSeeds.size(); j++)
					{
						strSeed = pairSeeds.get(j).getString();
						
						if(strSentence.toUpperCase().contains(strSeed.toUpperCase()))
						{
							if(Setting.strLabeling_Strategy_Short.toLowerCase().contains("exact"))
							{
								Labeling(strSeed, false);
							}
							else if(Setting.strLabeling_Strategy_Short.toLowerCase().contains("guillemet"))
							{
								Labeling(strSeed, true);
							}
						}
						
						if(iUnlabeledLength_Max < iSeedLength_Min)
						{
							break;
						}
					}
					
					// restore
					if(alMatched.size() > 0)
					{
						for(int j = 0; j < alMatched.size(); j++)
						{
							strSentence = strSentence.replace(Setting.strEntity_Start + String.valueOf(j) + Setting.strEntity_End, Setting.strEntity_Start + alMatched.get(j) + Setting.strEntity_End);
						}
						
						// Merge neighbor entities, e.g.: <NE>A</NE><NE>B</NE> → <NE>AB</NE>
						if(Setting.bMergeNeighbor)
						{
							strSentence = strSentence.replace(Setting.strEntity_End + Setting.strEntity_Start, "");
						}
						
						// Add labeled (and merged) entities to report
						alEntity = Lib.GetLabeledEntity(strSentence, false);
						for(String s:alEntity)
						{
							pairLabeledEntity[iID].addString(s);
						}
						
						alMatched.clear();
						alEntity.clear();
						alInput_S.set(i, strSentence);
					}
					
					if((i - iStart + 1) > 0 && ((i - iStart + 1) % 10000 == 0 || i == iEnd - 1))
					{
						Setting.MyLog.info("Thread " + strID + ": " + Lib.GetProgress(i - iStart + 1, iEnd - iStart + 1));
					}
				}
			}
			catch(Exception ex)
			{
				Setting.MyLog.warn("Thread " + strID + " interrupted");
				ex.printStackTrace();
			}
			
			Setting.MyLog.info("Thread " + strID + " end work");
		}
		
		private void parpareSeeds(String strType)
		{
			pairSeeds.reset();
			
			switch(strType.toLowerCase())
			{
				case "long": // long seeds
					float fSim = 0.0f;
					
					if(Setting.strLabeling_Strategy_Long.toLowerCase().contains("lsh"))
					{
						pairSeeds = (StrFloatPair) lshLong.getCandidatePairs(strSentence).clone();
					}
					else
					{
						pairSeeds = (StrFloatPair) pairSeeds_Long.clone();
					}
					
					// When labeling strategy is partial lebeling (ignore LSH enable or disable), filter out seeds with low similarity
					if(Setting.strLabeling_Strategy_Long.toLowerCase().contains("partial"))
					{
						for(int i = pairSeeds.size() - 1; i >= 0; i--)
						{
							fSim = calcSimilarity(strSentence, pairSeeds.get(i).getString());
							
							if(fSim >= fSimThreshold)
							{
								pairSeeds.get(i).setFloat(fSim);							
							}
							else
							{
								pairSeeds.remove(i);
							}
						}
						
						// ranking by similarity
						if(Setting.strLongSeedsRanking.toLowerCase().equals("similarity"))
						{
							pairSeeds.sortByFloat(StrFloatPair.SortBy.DESC);
						}
					}
					break;
				case "medium": // medium seeds
					if(Setting.strLabeling_Strategy_Medium.toLowerCase().contains("lsh"))
					{
						pairSeeds = (StrFloatPair) lshMedium.getCandidatePairs(strSentence).clone();
					}
					else
					{
						pairSeeds = (StrFloatPair) pairSeeds_Medium.clone();
					}
					break;
				case "short": // short seeds
					if(Setting.strLabeling_Strategy_Short.toLowerCase().contains("lsh"))
					{
						pairSeeds = (StrFloatPair) lshShort.getCandidatePairs(strSentence).clone();
					}
					else
					{
						pairSeeds = (StrFloatPair) pairSeeds_Short.clone();
					}
					break;
			}
			
			if(pairSeeds.size() > 0)
			{
				iSeedLength_Min = Lib.GetTokenLength(pairSeeds.get(pairSeeds.size() - 1).getString());
			}
			else
			{
				iSeedLength_Min = 0;
			}
		}
		
		private boolean Filtering_FirstLast()
		{
			ArrayList<String> alSentence = Lib.SentenceToToken(strSentence);
			ArrayList<String> alSeed = Lib.SentenceToToken(strSeed);
			String strCondition = Setting.strFiltering.toLowerCase();
			boolean bResult = false;
			
			if(!strCondition.contains("first") && !strCondition.contains("last"))
			{
				bResult = true;
			}
			else if(strCondition.contains("first") && strCondition.contains("last"))
			{
				int iIndexFirst = alSentence.indexOf(alSeed.get(0));
				int iIndexLast = alSentence.indexOf(alSeed.get(alSeed.size() - 1));
				if(iIndexFirst != -1 && iIndexLast != -1 && ((iIndexLast - iIndexFirst) > iMaxGap))
				{
					bResult = true;
				}
			}
			else if(strCondition.contains("first"))
			{
				if(alSentence.contains(alSeed.get(0)))
				{
					bResult = true;
				}	
			}
			else if(strCondition.contains("last"))
			{
				if(alSentence.contains(alSeed.get(alSeed.size() - 1)))
				{
					bResult = true;
				}	
			}
			
			return bResult;
		}
		
		private boolean Filtering_Core(String strLCS) // check candidate seeds post LCS
		{
			String strCondition = Setting.strFiltering.toLowerCase();
			boolean bResult = false;
			
			if(strCondition.contains("core"))
			{
				if(hmSeeds_Core.containsKey(strSeed))
				{	
					String strCore = hmSeeds_Core.get(strSeed);
					
					for(int i = 0; i < strCore.split(Setting.strSeparator_Tab).length; i++)
					{
						if(strLCS.contains(strCore.split(Setting.strSeparator_Tab)[i]))
						{
							bResult = true;
						}
					}
				}
			}
			else
			{
				bResult = true;
			}
			
			return bResult;
		}
		
		private float calcSimilarity(String strSentence, String strSeed) // calculate Seed Similarity
		{
			ArrayList<String> alSentence = Lib.SentenceToToken(strSentence);
			ArrayList<String> alSeed = Lib.SentenceToToken(strSeed);
			int iEqual = 0;
			
			for(String s:alSeed)
			{
				if(alSentence.contains(s))
				{
					iEqual++;
				}
			}
			
			return (float) iEqual / alSeed.size();
		}
		
		private boolean checkSeedCoreCondition(String strLCS)
		{
			boolean bResult = false;
			
			if(hmSeeds_Core.containsKey(strSeed))
			{	
				String strCore = hmSeeds_Core.get(strSeed);
				
				for(int i = 0; i < strCore.split(Setting.strSeparator_Tab).length; i++)
				{
					if(strLCS.contains(strCore.split(Setting.strSeparator_Tab)[i]))
					{
						bResult = true;
					}
				}
			}
			
			return bResult;
		}
		
		private String getLCS()
		{
			ArrayList<String> alTokens_Org = Lib.SentenceToToken(strSentence);
			ArrayList<String> alTokens = Lib.SentenceToToken(strSentence.toUpperCase());
			ArrayList<String> alSeed = Lib.SentenceToToken(strSeed.toUpperCase());
			ArrayList<Integer> alIndex = new ArrayList<Integer>();
			int m = alTokens.size();
			int n = alSeed.size();
			strLCS = "";
			
			// Build LCS Matrix
			int[][] LCS_Length = new int[m + 1][n + 1];
			
			for(int i = 0; i <= m; i++)
			{
				for(int j = 0; j <= n; j++)
				{
					if(i == 0 || j == 0)
					{
						LCS_Length[i][j] = 0;
					}
					else if(Lib.DuplicateSymbols(alTokens.get(i-1)).equals(Lib.DuplicateSymbols(alSeed.get(j-1))))
					{	
						LCS_Length[i][j] = LCS_Length[i - 1][j - 1] + 1;
					}
					else
					{
						LCS_Length[i][j] = Math.max(LCS_Length[i - 1][j], LCS_Length[i][j - 1]);
					}
				}
			}
			
			// BackTracking for LCS
			while(m > 0 && n > 0)
			{
				if(Lib.DuplicateSymbols(alTokens.get(m - 1)).equals(Lib.DuplicateSymbols(alSeed.get(n - 1))))
				{
//					alIndex.add(0, alTokens.get(m - 1)); // LCS
					alIndex.add(0, m - 1); // Index
					
					m--;
					n--;
				}
				else if(LCS_Length[m - 1][n] > LCS_Length[m][n - 1])
				{
					m--;
				}
				else
				{
					n--;
				}
			}
			
			// Check MaxGaps, if a gap between any neighboring tokens > threshold → discard this LCS result
			String strLCS = "";
			if(alIndex.size() >= iMaxGap)
			{
				int iIndex_Start = alIndex.get(0);
				int iIndex_End = alIndex.get(alIndex.size() - 1);
				
				if(((iIndex_End - iIndex_Start + 1) - alIndex.size()) <= iMaxGap)
				{
					for(int i = iIndex_Start; i <= iIndex_End ; i++)
					{
						strLCS += alTokens_Org.get(i);
					}
				}
			}
			
			// if LCS contains <NE> or </NE> then discard
			if(strLCS.toUpperCase().contains(Setting.strEntity_Start) || strLCS.toUpperCase().contains(Setting.strEntity_End))
			{
				strLCS = "";
			}
			
			// if |LCS| <= short seed → discard the LCS result
			if(Setting.strLabeling_Strategy_Short.toLowerCase().equals("ignore") && Lib.GetTokenLength(strLCS) < Setting.iEntity_Length_Min_Medium)
			{
				strLCS = "";
			}
			
			return strLCS;
		}
		
		private void Labeling(String strSeed, boolean bGuillemet)
		{	
			ArrayList<String> alTokens_Org = Lib.SentenceToToken(strSentence);
			ArrayList<String> alTokens = Lib.SentenceToToken(strSentence.toUpperCase());
			ArrayList<String> alSeed = Lib.SentenceToToken(strSeed.toUpperCase());
			ArrayList<Integer> alIndex_Start = new ArrayList<Integer>();
			ArrayList<Integer> alIndex_End = new ArrayList<Integer>();
			boolean bGuillemetPass = true;
			boolean bConditionPass = true;
			String strTemp = "";
			int iIndex_Sentence = 0;
			int iIndex_Start = 0;
			int iIndex_End = 0;
			
//			System.out.println("S strSentence = " + strSentence + "\tstrSeed = " + strSeed);
//			System.out.println("alTokens_Org = " + alTokens_Org);
			
			// find exact matching part(s)
			for(int i = 0; i < alTokens.size(); i++)
			{
				bConditionPass = true;
				
				// avoid nest labeling, index_start can't contain previous index_end
				if(alIndex_End.size() > 0 && i <= alIndex_End.get(alIndex_End.size() - 1))
				{
					bConditionPass = false;
				}
				
				// avoid <NE>1</NE> → <NE><NE>2</NE></NE>
				if(i - 1 >= 0 && alTokens.get(i-1).equals(Setting.strEntity_Start))
				{
					bConditionPass = false;
				}
				
				if(bConditionPass && alTokens.get(i).equals(alSeed.get(0)))
				{
					
					
					if(alSeed.size() == 1)
					{
						alIndex_Start.add(i);
						alIndex_End.add(i);
					}
					else
					{
//						System.out.println("yyy");
						
												
						iIndex_Sentence = i + 1;
//						System.out.println("iIndex_Sentence = " + iIndex_Sentence);
						for(int j = 1; j < alSeed.size(); j++)
						{
							if((iIndex_Sentence > alTokens.size() - 1) || !alTokens.get(iIndex_Sentence).equals(alSeed.get(j)))
							{
//								System.out.println("iIndex_Sentence = " + iIndex_Sentence + "\t alTokens.size() = " + alTokens.size());
//								System.out.println("break alTokens.get(i) = " + alTokens.get(iIndex_Sentence) + "\talSeed.get(0) = " + alSeed.get(j));
								break;
							}
							
							if(j == alSeed.size() - 1)
							{
								alIndex_Start.add(i);
								alIndex_End.add(iIndex_Sentence);
							}
							
							iIndex_Sentence++;
						}
					}
				}
			}
			
//			System.out.println("alIndex_Start = " + alIndex_Start + "\talIndex_End = " + alIndex_End);
			
			for(int i = 0 ; i < alIndex_Start.size() ; i++) // Check guillemet and labeling
			{
				iIndex_Start = alIndex_Start.get(i);
				iIndex_End = alIndex_End.get(i);
				strTemp = "";
				for(int j = iIndex_Start ; j <= iIndex_End; j++)
				{
					strTemp += alTokens_Org.get(j); 
				}
				
				bGuillemetPass = true;
				
				if(bGuillemet)
				{
					if(iIndex_Start == 0 || iIndex_End == (alTokens_Org.size() - 1)) // previous/next token is not exist
					{
						bGuillemetPass = false;
					}
					else if(strGuillemet_HashTag.contains(alTokens_Org.get(iIndex_Start - 1))) // previous token = hashtag
					{
						if((iIndex_Start - 2 >= 0) && (!strGuillemet_Left.contains(alTokens_Org.get(iIndex_Start - 2)) || !strGuillemet_Right.contains(alTokens_Org.get(iIndex_End + 1))))
						{
							bGuillemetPass = false;
						}
					}
					else if(!strGuillemet_Left.contains(alTokens_Org.get(iIndex_Start - 1)) || !strGuillemet_Right.contains(alTokens_Org.get(iIndex_End + 1)))
					{
						bGuillemetPass = false;
					}
				}
				
				if(bGuillemetPass) // Labeling
				{
					for(int j = iIndex_Start; j <= iIndex_End; j++)
					{
						if(iIndex_Start == iIndex_End) // Tag = S
						{	
							alTokens_Org.set(j, Setting.strEntity_Start + String.valueOf(alMatched.size()) + Setting.strEntity_End);
						}
						else if(j == iIndex_Start) // Tag = B
						{
							alTokens_Org.set(j, Setting.strEntity_Start + String.valueOf(alMatched.size()));
						}
						else if(j == iIndex_End) // Tag = E 
						{
							alTokens_Org.set(j, Setting.strEntity_End);
						}
						else
						{
							alTokens_Org.set(j, ""); // Tag = I
						}
					}
					
					alMatched.add(strSeed);
					iUnlabeledLength_Max = getUnlabeledLength_Max(strSentence);
					strSentence = Lib.Tokens2Sentence(alTokens_Org);
				}
			}
			
//			System.out.println("E strSentence = " + strSentence + "\tstrSeed = " + strSeed);
//			System.out.println("");
		}
		
		private int getUnlabeledLength_Max(String strInput)
		{
			int iLength_Max = 0;
			
			// remove number between strEntity_Start and strEntity_End, e.g.: <NE>0</NE> → <NE></NE>
			strInput = strInput.replaceAll(Setting.strEntity_Start + "[0-9]{1,2}" + Setting.strEntity_End, Setting.strEntity_Start + Setting.strEntity_End);
			String[] strSegment = strInput.split(Setting.strEntity_Start + Setting.strEntity_End);
			
			for(String s:strSegment)
			{
				if(Lib.GetTokenLength(s) > iLength_Max)
				{
					iLength_Max = Lib.GetTokenLength(s);
				}
			}
			
			return iLength_Max;
		}
	}
}








