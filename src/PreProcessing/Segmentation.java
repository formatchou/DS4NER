package PreProcessing;
import Global.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;

public class Segmentation
{
	static String strInput = "Activity_Org.txt"; 
	static String strOutput_S = "Activity_S.txt";
	static String strType = "Training";			// Training, Seeds, or Testing
	static boolean bSplitBySpace = false;		// when config.Sentence_SplitBySpace_Min < len(sent) and this_value = True, then sentence will be splited by space
	static boolean bFilterNoneECJK = false;		// if a sub-sentence contains 50% (default) non-ECJK characters → filter out
	static boolean bFilterEllipsis = false;	// if a sub-sentence's first or last character is "..." or "…" or "⋯" or "᠁" → filter out
	
	static File fInput;
	static File fOutput_S;
	static char[] chSource;
	static char[] chTarget;
	static char[] chSplitSymbol;
	static char[] chGuillemet_Left;
	static char[] chGuillemet_Right;
	static Matcher matcher;
	static ArrayList<String> alInput;
	static ArrayList<String> alECJK = new ArrayList<String>();
	static ArrayList<String> alEllipsis = new ArrayList<String>();
	
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
		opts.addOption(Option.builder("strInput").desc("<Corpus\\Training or Testing\\>strInput.txt").required(true).hasArg().build());
		opts.addOption(Option.builder("strOutput_S").desc("<WorkFolder\\Training or Testing\\>strOutput_S.txt").required(true).hasArg().build());
		opts.addOption(Option.builder("strType").desc("Sent, Seeds, or Testing").required(true).hasArg().build());
		opts.addOption(Option.builder("bSplitBySpace").desc("True or False").required(true).hasArg().build());
		opts.addOption(Option.builder("bFilterNoneECJK").desc("True or False").required(true).hasArg().build());
		opts.addOption(Option.builder("bFilterEllipsis").desc("True or False").required(true).hasArg().build());
		
		try
		{
			if(Setting.strType.toUpperCase().equals("IDE"))
			{
				switch(strType.toLowerCase())
				{
					case "training":
						fInput = Lib.SubFileOrFolder(Setting.dirCorpusTraining, strInput);
						fOutput_S = Lib.SubFileOrFolder(Setting.dirTraining, strOutput_S);
						break;
						
					case "seeds":
						fInput = Lib.SubFileOrFolder(Setting.dirCorpusTraining, strInput);
						fOutput_S = Lib.SubFileOrFolder(Setting.dirTraining, strOutput_S);
						break;
						
					case "testing":
						fInput = Lib.SubFileOrFolder(Setting.dirCorpusTesting, strInput);
						fOutput_S = Lib.SubFileOrFolder(Setting.dirTesting, strOutput_S);
						break;
				}
			}
			else
			{
				DefaultParser parser = new DefaultParser();
				CommandLine cmd = parser.parse(opts, args);
				strType = cmd.getOptionValue("strType");
				switch(strType.toLowerCase())
				{
					case "training":
						fInput = Lib.SubFileOrFolder(Setting.dirCorpusTraining, cmd.getOptionValue("strInput"));
						fOutput_S = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strOutput_S"));
						break;
						
					case "seeds":
						fInput = Lib.SubFileOrFolder(Setting.dirCorpusTraining, cmd.getOptionValue("strInput"));
						fOutput_S = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strOutput_S"));
						break;
						
					case "testing":
						fInput = Lib.SubFileOrFolder(Setting.dirCorpusTesting, cmd.getOptionValue("strInput"));
						fOutput_S = Lib.SubFileOrFolder(Setting.dirTesting, cmd.getOptionValue("strOutput_S"));
						break;
				}
				
				bSplitBySpace = Boolean.valueOf(cmd.getOptionValue("bSplitBySpace"));
				bFilterNoneECJK = Boolean.valueOf(cmd.getOptionValue("bFilterNoneECJK"));
				bFilterEllipsis = Boolean.valueOf(cmd.getOptionValue("bFilterEllipsis"));
			}
			
			if(bParseArguments)
			{
				Setting.MyLog.info("fInput = " + fInput.getCanonicalPath());
				Setting.MyLog.info("fOutput_S = " + fOutput_S.getCanonicalPath());
				Setting.MyLog.info("strType = " + strType);
				Setting.MyLog.info("bSplitBySpace = " + bSplitBySpace);
				Setting.MyLog.info("bFilterNoneECJK = " + bFilterNoneECJK);
				Setting.MyLog.info("bFilterEllipsis = " + bFilterEllipsis);
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
		
		Run(fInput, fOutput_S, strType, bSplitBySpace, bFilterNoneECJK, bFilterEllipsis, true);
	}
	
	public static void Run(File fInput, File fOutput_S	, String strType, boolean bSplitBySpace, boolean bFilterNoneECJK, boolean bFilterEllipsis, boolean bOutputReport) throws Exception	
	{
		boolean bSplit = true;
		boolean bFilter = true;
		boolean bDeDuplicate = true;
		boolean bSortByLength = false;
		
		switch(strType.toLowerCase())
		{
			case "training":				
				bSplit = true;
				bFilter = true;
				bDeDuplicate = true;
				bSortByLength = false;
				break;
				
			case "seeds":
				bSplit = false;
				bFilter = false;
				bDeDuplicate = true;
				bSortByLength = true;
				break;
				
			case "testing":
				bSplit = true;
				bFilter = false;
				bDeDuplicate = false;
				bSortByLength = false;
				break;
				
			case "extractor":
				bSplit = true;
				bFilter = false;
				bDeDuplicate = false;
				bSortByLength = false;
				break;
		}
		
		Setting.MyLog.info("bSplit = " + bSplit);
		Setting.MyLog.info("bFilter = " + bFilter);
		Setting.MyLog.info("bDeDuplicate = " + bDeDuplicate);
		Setting.MyLog.info("bSortByLength = " + bSortByLength);
		Lib.MakeWorkFolder();
		
		// ----------
		
		alInput = Lib.LoadArrayList(fInput, "UTF-8", false);
		ArrayList<String> alSplit = new ArrayList<String>();
		ArrayList<String> alDeDuplicate = new ArrayList<String>();
		ArrayList<String> alSymbols = Lib.LoadArrayList(Lib.SubFileOrFolder(Setting.dirDictionary, "Symbols.txt"), "UTF-8", true);
		HashSet<String> hsDuplicate = new HashSet<String>();
		StrIntPair pairSeeds = new StrIntPair();
		String strLine = "";
		String strGuillemet_Left = "(「‘"; 
		String strGuillemet_Right = ")」’";
		
		chSource = alSymbols.get(0).toCharArray();
		chTarget = alSymbols.get(1).toCharArray();
		chSplitSymbol = Setting.patternSplitSymbol.pattern().substring(1, Setting.patternSplitSymbol.pattern().length() - 2).toCharArray();
		chGuillemet_Left = strGuillemet_Left.toCharArray();
		chGuillemet_Right = strGuillemet_Right.toCharArray();
				
		// ----------
		
		if(strType.equals("extractor"))
		{
			for(int i = 0 ; i < alInput.size() ; i++)
			{
				alInput.set(i, alInput.get(i) + Setting.strSeparator_Extractor);
			}
		}
		
		for(int i = 0; i < alInput.size(); i++)
		{	
			strLine = SymbolReplace(alInput.get(i));
			
			if(strLine.length() > 0)
			{
				if(bSplit)
				{
					alSplit.addAll(Split(strLine, bFilter, bSplitBySpace, bFilterNoneECJK, bFilterEllipsis));
				}
				else
				{
					alSplit.add(strLine);
				}
				
				if(i > 0 && (i % 100000 == 0 || i == alInput.size() - 1))
				{
					Setting.MyLog.info(Lib.GetProgress(i, alInput.size()));
				}
			}
		}
		
		// bDeDuplicate
		if(bDeDuplicate)
		{
			for(String s:alSplit)
			{
				if(!hsDuplicate.contains(s))
				{
					hsDuplicate.add(s);
					alDeDuplicate.add(s);
				}
			}
		}
		else
		{
			alDeDuplicate = alSplit;
		}
		
		if(bSortByLength) // for seeds
		{
			for(String s:alDeDuplicate)
			{
				// seed can not contain <NE> or </NE>
				if(!s.toUpperCase().contains(Setting.strEntity_Start) && !s.toUpperCase().contains(Setting.strEntity_End))
				{
					pairSeeds.addString(s);
				}
			}
			
			pairSeeds.sortByTokenLength(StrIntPair.SortBy.DESC);
			Lib.SaveFile(pairSeeds, fOutput_S, "", "str", false);
		}
		else
		{
			Lib.SaveFile(alDeDuplicate, fOutput_S, false);
			
		}
		
		if(bOutputReport)
		{
			OutputReport(alDeDuplicate);
		}
	}
	
	private static String SymbolReplace(String strInput) throws Exception
	{
		// remove invalid symbol 「 」and tab 「\t」
		// replace half-shaped/full-shape space to "﹍", and merge several spaces to single space
		// remove first/last char when it is "﹍"
		String strTemp_1 = strInput.replace("	", "").replace(Setting.strSeparator_Tab, " ").replaceAll("[	 　]+", "﹍").trim();
		String strTemp_2 = StringUtils.strip(strTemp_1, "﹍");
		String strTemp_3 = "";
		
		// full-shape English letters and numbers → half-shaped
		char[] ch = strTemp_2.toCharArray();
		int iTemp = 0;
		for(int i = 0; i < ch.length; i++)
		{
			iTemp = (int) ch[i];
			if(iTemp >= 65281 && iTemp <= 65374)
			{
				strTemp_3 += (char) (iTemp - 65248);
			}
			else
			{
				strTemp_3 += (char) iTemp;
			}
		}
		
		for(int i = 0; i < chSource.length; i++)
		{
			strTemp_3 = strTemp_3.replace(chSource[i], chTarget[i]);
		}
		
		// not a html tag then 「<」or「＜」→ 「（」
		ArrayList<String> alTokens = Lib.SentenceToToken(strTemp_3);
		 
		for(int i = 0 ; i < alTokens.size() ; i++)
		{
			if(alTokens.get(i).length() == 1 && (alTokens.get(i).equals("<") || alTokens.get(i).equals("＜")))
			{
				alTokens.set(i, "（");
			}
			else if(alTokens.get(i).length() == 1 && (alTokens.get(i).equals(">") || alTokens.get(i).equals("＞")))
			{
				alTokens.set(i, "）");
			}
		}
		
		return Lib.Tokens2Sentence(alTokens);
	}
	
	private static ArrayList<String> Split(String strInput, boolean bFilter, boolean bSplitBySpace, boolean bFilterNoneECJK, boolean bFilterEllipsis)
	{
		ArrayList<String> alToken = Lib.SentenceToToken(strInput);
		ArrayList<String> alResult = new ArrayList<String>();
		String strToken = "";
		int iEntity = 0;
		
		for(int i = 0 ; i < alToken.size() ; i++)
		{
			strToken = alToken.get(i);
			
			if(strToken.equals(Setting.strEntity_Start))
			{
				iEntity++;
			}
			else if(strToken.equals(Setting.strEntity_End))
			{
				iEntity--;
			}
			
			// split by symbol
			matcher = Setting.patternSplitSymbol.matcher(strToken);
			if(iEntity == 0 && matcher.matches())
			{
				if((i + 1) < alToken.size())
				{
					if(!Setting.patternSplitSymbol.matcher(alToken.get(i + 1)).matches())
					{
						alToken.set(i, strToken + Setting.strSeparator);
					}
				}
				else
				{
					alToken.set(i, strToken + Setting.strSeparator);
				}
			}
			
			// Split by space
			if(bSplitBySpace)
			{	
				if(strToken.equals("﹍") && (i - 1) >= 0 && (i + 1) < alToken.size())
				{
					matcher = Setting.patternSplitSpace.matcher(alToken.get(i - 1) + alToken.get(i) + alToken.get(i + 1));
					if(iEntity == 0 && matcher.matches())
					{	
						alToken.set(i, strToken + Setting.strSeparator);
					}
				}
			}
			
			// Split by "...", "…", "⋯", or "᠁"
			if(bFilterEllipsis && (strToken.endsWith("...") || strToken.equals("…") || strToken.equals("⋯") || strToken.equals("᠁")))
			{
				if(iEntity == 0)
				{
					alToken.set(i, strToken + Setting.strSeparator);
				}
			}
		}
		
		// ----------		
		
		String[] strSegmentation = Lib.Tokens2Sentence(alToken).split(Setting.strSeparator);
		
		for(String s:strSegmentation)
		{
			s = StringUtils.strip(s, "﹍");
			
			// filter out too short/too long segment			
			if(bFilter && s.length() > 0)
			{
				if(Lib.GetTokenLength(s) < Setting.iSubSentence_Length_Min || Setting.iSubSentence_Length_Max < Lib.GetTokenLength(s))
				{
					s = "";
				}
			}
			
			if(bFilterNoneECJK && s.length() > 0)
			{
				if(Lib.IsNoise(s))
				{
					alECJK.add(s);
					s = "";
				}
			}
			
			// In a segment, the first (or last character) is “…” or “...” → discard this segment
			if(bFilterEllipsis && s.length() > 0)
			{
				if(s.startsWith("...") || s.startsWith("…") || s.startsWith("⋯") || s.startsWith("᠁") 
						|| s.endsWith("...") || s.endsWith("…") || s.endsWith("⋯") || s.endsWith("᠁"))
				{
					alEllipsis.add(s);
					s = "";
				}	
			}
			
			if(s.length() > 0)
			{
				alResult.add(s);
			}
		}
		
		return alResult;
	}
	
	private static void OutputReport(ArrayList<String> alData) throws Exception
	{
		DecimalFormat df2 = new DecimalFormat("#0.00");
		ArrayList<String> alReport = new ArrayList<String>();
		ArrayList<String> alMax = new ArrayList<String>();
		ArrayList<String> alMin = new ArrayList<String>();
		
		int iLength = 0;
		int iLength_Sum = 0;
		int iLength_Max = 0;
		int iLength_Min = 999999999;
		
		for(String s:alData)
		{
			iLength = Lib.GetTokenLength(s);
			iLength_Sum += iLength;
			
			if(iLength > iLength_Max)
			{
				iLength_Max = iLength;
				alMax.clear();
				alMax.add(s);
			}
			else if(iLength == iLength_Max)
			{
				alMax.add(s);
			}
			
			if(iLength < iLength_Min)
			{
				iLength_Min = iLength;
				alMin.clear();
				alMin.add(s);
			}
			else if(iLength == iLength_Min)
			{
				alMin.add(s);
			}
		}
		
		alReport.add("Sentences Size = " + alData.size());
		alReport.add("Sentences Token Length Avg = " + df2.format((float) iLength_Sum/ alData.size()));
		alReport.add("Sentences Token Length Max = " + String.valueOf(iLength_Max));
		for(String s:alMax)
		{
			alReport.add(s);
		}
		
		alReport.add("Sentences Token Length Min = " + String.valueOf(iLength_Min));
		for(String s:alMin)
		{
			alReport.add(s);
		}
		
		File fOutput_Report = Lib.GetFileWithSuffix(fOutput_S, "_Report");
		Lib.SaveFile(alReport, fOutput_Report, false);
		
		// Output NoneECJK/FilterEllipsis Report
		if(bFilterNoneECJK)
		{
			File fECJK = Lib.GetFileWithSuffix(fOutput_S, "_ECJK");
			Lib.SaveFile(alECJK, fECJK, false);
		}
		
		if(bFilterEllipsis)
		{
			File fEllipsis = Lib.GetFileWithSuffix(fOutput_S, "_Ellipsis");
			Lib.SaveFile(alEllipsis, fEllipsis, false);
		}
	}
}
