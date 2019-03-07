package PrepareData;
import Global.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;

public class MineDict
{
	static String strInput_L = "LOC_L_ECJK_Ellipsis.txt";			// POI_L_v1, POI_TrainingData_L_Exact_POSOnly
	static String strMethod = "Supp";					// Conf or Supp or HMCS
	static float fThreshold = 0.5f;					// Cumulative Score Threshold for Support/Confidence/HMCS, 0.0f-1.0f
	static File fInput_L;
	static int iDict = 4;								// CommonBefore, EntityPrefix, EntitySuffix, CommonAfter
	static int iGram = 3;								// 1-, 2-, 3-gram
	static ArrayList<String> alInput_L;
	static HMCS[] hmcsCommonBefore;
	static HMCS[] hmcsEntityPrefix;
	static HMCS[] hmcsEntitySuffix;
	static HMCS[] hmcsCommonAfter;
	static StrIntPair pairGlobal;
	static int iThread = 1;
	
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
		opts.addOption(Option.builder("strInput_L").desc("<WorkFolder\\Training\\>strInput_L.txt").required(true).hasArg().build());
		opts.addOption(Option.builder("strMethod").desc("Supp or Conf or HMCS").required(true).hasArg().build());
		opts.addOption(Option.builder("fThreshold").desc("Cumulative Score Threshold, 0.0f-1.0f").required(true).hasArg().build());
		
		try
		{
			if(Setting.strType.toUpperCase().equals("IDE"))
			{
				fInput_L = Lib.SubFileOrFolder(Setting.dirTraining, strInput_L);
			}
			else
			{
				DefaultParser parser = new DefaultParser();
				CommandLine cmd = parser.parse(opts, args);
				
				fInput_L = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strInput_L"));
				strMethod = cmd.getOptionValue("strMethod");
				fThreshold = Float.valueOf(cmd.getOptionValue("fThreshold"));
			}
			
			if(bParseArguments)
			{
				Setting.MyLog.info("fInput_L = " + fInput_L.getCanonicalPath());
				Setting.MyLog.info("strMethod = " + strMethod);
				Setting.MyLog.info("fThreshold = " + fThreshold);
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

		alInput_L = Lib.LoadArrayList(fInput_L, "UTF-8", true);
		hmcsCommonBefore = new HMCS[iGram];
		hmcsEntityPrefix = new HMCS[iGram];
		hmcsEntitySuffix = new HMCS[iGram];
		hmcsCommonAfter = new HMCS[iGram];
		pairGlobal = new StrIntPair();
		
		for(int i = 0 ; i < iGram; i++)
		{
			hmcsCommonBefore[i] = new HMCS(fThreshold);
			hmcsEntityPrefix[i] = new HMCS(fThreshold);
			hmcsEntitySuffix[i] = new HMCS(fThreshold);
			hmcsCommonAfter[i] = new HMCS(fThreshold);
		}
		
		// ----------
		
		ParseCorpus();	// First pass, collect entire dictionary terms with multi-threads
		UpdateInfo();	// Second pass, update each dictionary terms' information
		FilterTerms();	// Filter Terms by Supp/Conf/HMCS Method
		SaveDictionaries();
	}
	
	// First pass, collect entire dictionary terms with multi-threads
	private static void ParseCorpus() throws Exception
	{
		DictWorker[] worker = new DictWorker[iThread];
		ExecutorService es = Executors.newFixedThreadPool(iThread);
		
		for(int i = 0; i < iThread; i++)
		{
			worker[i] = new DictWorker(i);
			es.execute(worker[i]);
		}
		
		Thread.sleep(1000);
		es.shutdown();
		
		while(true)
		{
			Thread.sleep(1000);
			if(Thread.activeCount() == 1) // all threads destroyed
			{
				// merge collected dictionary terms from each thread worker
				Setting.MyLog.info("Start to merge dictionery terms, wait a moment please ...");
				
				for(int i = 0; i < iThread; i++)
				{
					for(int j = 0; j < iGram; j++)
					{
						for(StrIntPair pair:worker[i].getInfo("CommonBefore", j))
						{
							hmcsCommonBefore[j].addTerm(pair.getString(), pair.getInt());							
						}
						
						for(StrIntPair pair:worker[i].getInfo("EntityPrefix", j))
						{
							hmcsEntityPrefix[j].addTerm(pair.getString(), pair.getInt());							
						}
						
						for(StrIntPair pair:worker[i].getInfo("EntitySuffix", j))
						{
							hmcsEntitySuffix[j].addTerm(pair.getString(), pair.getInt());							
						}
						
						for(StrIntPair pair:worker[i].getInfo("CommonAfter", j))
						{
							hmcsCommonAfter[j].addTerm(pair.getString(), pair.getInt());							
						}
					}
					
					// merge collected global terms occurrence count from each thread worker
					for(StrIntPair pair:worker[i].getInfo("TermInfo", 0))
					{
						pairGlobal.addPair(pair.getString(), pair.getInt());							
					}
				}
				
				break;
			}
		}
	}
	
	// Second pass, update each dictionary terms' information
	private static void UpdateInfo() throws Exception
	{
		Setting.MyLog.info("Update total count now, wait a moment please ...");
		
		for(int i = 0; i < iGram; i++)
		{
			hmcsCommonBefore[i].calcSupport();
			hmcsEntityPrefix[i].calcSupport();
			hmcsEntitySuffix[i].calcSupport();
			hmcsCommonAfter[i].calcSupport();
		}
		Setting.MyLog.info("Filter low occurrence terms done.");
		
		// ----------
		
		for(int i = 0; i < iGram; i++)
		{
			for(HMCS h:hmcsCommonBefore[i])
			{	
				h.setTotal(pairGlobal.getInt(h.getTerm()));
			}
			
			for(HMCS h:hmcsEntityPrefix[i])
			{
				h.setTotal(pairGlobal.getInt(h.getTerm()));
			}
			
			for(HMCS h:hmcsEntitySuffix[i])
			{
				h.setTotal(pairGlobal.getInt(h.getTerm()));
			}
			
			for(HMCS h:hmcsCommonAfter[i])
			{
				h.setTotal(pairGlobal.getInt(h.getTerm()));
			}
		}
		
		Setting.MyLog.info("Update each term's total count done.");
	}
	
	// Filter Terms by Supp/Conf/HMCS Method
	private static void FilterTerms() throws Exception
	{
		for(int i = 0; i < iGram; i++)
		{
			switch(strMethod.toUpperCase())
			{
				case "SUPP":
					hmcsCommonBefore[i].filterBySupport();
					hmcsEntityPrefix[i].filterBySupport();
					hmcsEntitySuffix[i].filterBySupport();
					hmcsCommonAfter[i].filterBySupport();
					break;
				
				case "CONF":
					hmcsCommonBefore[i].filterByConfidence();
					hmcsEntityPrefix[i].filterByConfidence();
					hmcsEntitySuffix[i].filterByConfidence();
					hmcsCommonAfter[i].filterByConfidence();
					break;
					
				case "HMCS":
					hmcsCommonBefore[i].filterByHMCS();
					hmcsEntityPrefix[i].filterByHMCS();
					hmcsEntitySuffix[i].filterByHMCS();
					hmcsCommonAfter[i].filterByHMCS();
					break;
			}
		}
	}
	
	private static void SaveDictionaries() throws Exception
	{
		File fCommonBefore = Lib.SubFileOrFolder(Setting.dirDictionary, "CommonBefore.txt");
		File fCommonAfter = Lib.SubFileOrFolder(Setting.dirDictionary, "CommonAfter.txt");
		File fEntityPrefix = Lib.SubFileOrFolder(Setting.dirDictionary, "EntityPrefix.txt");
		File fEntitySuffix = Lib.SubFileOrFolder(Setting.dirDictionary, "EntitySuffix.txt");
		
		for(int i = 0 ; i < iGram ; i++)
		{
			if(i == 0) // 1-gram
			{	
				Lib.SaveFile(hmcsCommonBefore[i], fCommonBefore, strMethod, true, false);
				Lib.SaveFile(hmcsEntityPrefix[i], fEntityPrefix, strMethod, true, false);
				Lib.SaveFile(hmcsEntitySuffix[i], fEntitySuffix, strMethod, true, false);
				Lib.SaveFile(hmcsCommonAfter[i], fCommonAfter, strMethod, true, false);
			}
			else // 2-, 3-gram
			{
				Lib.SaveFile(hmcsCommonBefore[i], fCommonBefore, strMethod, false, true);
				Lib.SaveFile(hmcsEntityPrefix[i], fEntityPrefix, strMethod, false, true);
				Lib.SaveFile(hmcsEntitySuffix[i], fEntitySuffix, strMethod, false, true);
				Lib.SaveFile(hmcsCommonAfter[i], fCommonAfter, strMethod, false, true);
			}
		}		
	}
	
	// ----------
	
	private static class DictWorker implements Runnable
	{
		private String strID;
		private int iID;
		private int iStart;
		private int iEnd;
		
		private String strTerm = "";
		private ArrayList<String> alToken = new ArrayList<String>();
		private StrIntPair[] pairCommonBefore = new StrIntPair[iGram];
		private StrIntPair[] pairEntityPrefix = new StrIntPair[iGram];
		private StrIntPair[] pairEntitySuffix = new StrIntPair[iGram];
		private StrIntPair[] pairCommonAfter = new StrIntPair[iGram];
		private StrIntPair pairTermInfo = new StrIntPair();
		
		public DictWorker(int id)
		{
			this.strID = String.valueOf(id);
			this.iID = id;
			this.iStart = id * alInput_L.size() / iThread;
			this.iEnd = (id + 1) * alInput_L.size() / iThread - 1;
			
			if(id == iThread - 1)
			{
				this.iEnd = alInput_L.size() - 1;
			}
			
			for(int i = 0 ; i < iGram ; i++)
			{
				pairCommonBefore[i] = new StrIntPair();
				pairEntityPrefix[i] = new StrIntPair();
				pairEntitySuffix[i] = new StrIntPair();
				pairCommonAfter[i] = new StrIntPair();
			}
		}

		public void run()
		{
			Setting.MyLog.info("DictWorker " + strID + " begins work (start index = " + iStart + "\t, end index = " + iEnd + ")");
			
			try
			{	
				// first pass, extract dictionary terms
				for(int i = iStart; i <= iEnd; i++)
				{
					alToken = Lib.SentenceToToken(alInput_L.get(i));
					
					for(int j = 0; j < alToken.size(); j++)
					{
						updateTotalCount(Lib.DuplicateSymbols(alToken.get(j)));	// one-gram
						
						if(j + 1 < alToken.size()) // bi-gram
						{
							updateTotalCount(Lib.DuplicateSymbols(alToken.get(j)) + Lib.DuplicateSymbols(alToken.get(j + 1)));
						}
						
						if(j + 2 < alToken.size()) // tri-gram
						{
							updateTotalCount(Lib.DuplicateSymbols(alToken.get(j)) + Lib.DuplicateSymbols(alToken.get(j + 1)) + Lib.DuplicateSymbols(alToken.get(j + 2)));
						}
						
						// ------------------------------
						
						if(alToken.get(j).equals(Setting.strEntity_Start))
						{	
							if(j - 1 >= 0) // Common Before with lenth = 1
							{
								strTerm = Lib.DuplicateSymbols(alToken.get(j - 1));
								if(!strTerm.contains(Setting.strEntity_Start) && !strTerm.contains(Setting.strEntity_End))
								{
									pairCommonBefore[0].addString(strTerm);
								}
							}
							
							if(j - 2 >= 0) // Common Before with lenth = 2
							{
								strTerm = Lib.DuplicateSymbols(alToken.get(j - 2)) + Lib.DuplicateSymbols(alToken.get(j - 1));
								if(!strTerm.contains(Setting.strEntity_Start) && !strTerm.contains(Setting.strEntity_End))
								{
									pairCommonBefore[1].addString(strTerm);
								}
							}
							
							if(j - 3 >= 0) // Common Before with lenth = 3
							{
								strTerm = Lib.DuplicateSymbols(alToken.get(j - 3)) + Lib.DuplicateSymbols(alToken.get(j - 2)) + Lib.DuplicateSymbols(alToken.get(j - 1));
								if(!strTerm.contains(Setting.strEntity_Start) && !strTerm.contains(Setting.strEntity_End))
								{
									pairCommonBefore[2].addString(strTerm);
								}
							}
							
							// ----------
							
							if(j + 1 <= alToken.size() - 1) // Entity Prefix with length = 1
							{
								strTerm = Lib.DuplicateSymbols(alToken.get(j + 1));
								if(!strTerm.contains(Setting.strEntity_Start) && !strTerm.contains(Setting.strEntity_End))
								{
									pairEntityPrefix[0].addString(strTerm);
								}
							}
							
							if(j + 2 <= alToken.size() - 1) // Entity Prefix with length = 2
							{
								strTerm = Lib.DuplicateSymbols(alToken.get(j + 1)) + Lib.DuplicateSymbols(alToken.get(j + 2));
								if(!strTerm.contains(Setting.strEntity_Start) && !strTerm.contains(Setting.strEntity_End))
								{
									pairEntityPrefix[1].addString(strTerm);
								}
							}
							
							if(j + 3 <= alToken.size() - 1) // Entity Prefix with length = 3
							{
								strTerm = Lib.DuplicateSymbols(alToken.get(j + 1)) + Lib.DuplicateSymbols(alToken.get(j + 2)) + Lib.DuplicateSymbols(alToken.get(j + 3));
								if(!strTerm.contains(Setting.strEntity_Start) && !strTerm.contains(Setting.strEntity_End))
								{
									pairEntityPrefix[2].addString(strTerm);
								}
							}
						}
						
						// ------------------------------
						
						if(alToken.get(j).equals(Setting.strEntity_End))
						{
							if(j - 1 >= 0) // Entity Suffix with length = 1
							{
								strTerm = Lib.DuplicateSymbols(alToken.get(j - 1));
								if(!strTerm.contains(Setting.strEntity_Start) && !strTerm.contains(Setting.strEntity_End))
								{
									pairEntitySuffix[0].addString(strTerm);
								}
							}
							
							if(j - 2 >= 0) // Entity Suffix with length = 2
							{
								strTerm = Lib.DuplicateSymbols(alToken.get(j - 2)) + Lib.DuplicateSymbols(alToken.get(j - 1));
								if(!strTerm.contains(Setting.strEntity_Start) && !strTerm.contains(Setting.strEntity_End))
								{
									pairEntitySuffix[1].addString(strTerm);
								}
							}
							
							if(j - 3 >= 0) // Entity Suffix with length = 3
							{
								strTerm = Lib.DuplicateSymbols(alToken.get(j - 3)) + Lib.DuplicateSymbols(alToken.get(j - 2)) + Lib.DuplicateSymbols(alToken.get(j - 1));
								if(!strTerm.contains(Setting.strEntity_Start) && !strTerm.contains(Setting.strEntity_End))
								{
									pairEntitySuffix[2].addString(strTerm);
								}
							}
							
							// ----------
							
							if(j + 1 <= alToken.size() - 1) // Common After with lenth = 1
							{
								strTerm = Lib.DuplicateSymbols(alToken.get(j + 1));
								if(!strTerm.contains(Setting.strEntity_Start) && !strTerm.contains(Setting.strEntity_End))
								{
									pairCommonAfter[0].addString(strTerm);
								}
							}
							
							if(j + 2 <= alToken.size() - 1) // Common After with lenth = 2
							{
								strTerm = Lib.DuplicateSymbols(alToken.get(j + 1)) + Lib.DuplicateSymbols(alToken.get(j + 2));
								if(!strTerm.contains(Setting.strEntity_Start) && !strTerm.contains(Setting.strEntity_End))
								{
									pairCommonAfter[1].addString(strTerm);
								}
							}
							
							if(j + 3 <= alToken.size() - 1) // Common After with lenth = 3
							{
								strTerm = Lib.DuplicateSymbols(alToken.get(j + 1)) + Lib.DuplicateSymbols(alToken.get(j + 2)) + Lib.DuplicateSymbols(alToken.get(j + 3));
								if(!strTerm.contains(Setting.strEntity_Start) && !strTerm.contains(Setting.strEntity_End))
								{
									pairCommonAfter[2].addString(strTerm);
								}
							}
						}
					}
					
					alToken.clear();
					
					if((i - iStart + 1) > 0 && ((i - iStart + 1) % 100000 == 0 || i == iEnd - 1))
					{
						Setting.MyLog.info("Thread " + strID + ": " + Lib.GetProgress(i - iStart + 1, iEnd - iStart + 1));
					}
				}
			}
			catch(Exception ex)
			{
				Setting.MyLog.warn("DictWorker " + strID + " interrupted");
				ex.printStackTrace();
			}
			
			Setting.MyLog.info("DictWorker " + strID + " end work");
		}
		
		private void updateTotalCount(String strTerm)
		{	
			if(!strTerm.contains(Setting.strEntity_Start) && !strTerm.contains(Setting.strEntity_End))
			{
				pairTermInfo.addString(strTerm);	
			}
		}
		
		private StrIntPair getInfo(String strDict, int iGramID)
		{
			switch(strDict.toLowerCase())
			{	
				case "commonbefore":
					return pairCommonBefore[iGramID];
					
				case "entityprefix":
					return pairEntityPrefix[iGramID];
					
				case "entitysuffix":
					return pairEntitySuffix[iGramID];
					
				case "commonafter":
					return pairCommonAfter[iGramID];
					
				default:
				case "terminfo":
					return pairTermInfo;
			}
		}
	}
}
