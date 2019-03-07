package Baseline;
import Global.*;
import Testing.TestingLib; 
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;

public class Evaluation
{
	static String strTesting_S = "KKBOX_Artist_S.txt";
	static String strBaseline_L = "Baseline_L.txt";
	static String strMethod = "Exact"; // Exact or Partial
	static File fTesting_S;
	static File fBaseline_L;
	static File fOutput;
	static DecimalFormat df2 = new DecimalFormat("#0.00");
	static DecimalFormat df4 = new DecimalFormat("#0.0000");
	
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
		opts.addOption(Option.builder("strTesting_S").desc("<WorkFolder\\Testing\\>strTesting_S.txt").required(true).hasArg().build());
		opts.addOption(Option.builder("strBaseline_L").desc("<WorkFolder\\Training\\>strBaseline_L.txt").required(true).hasArg().build());
		opts.addOption(Option.builder("strMethod").desc("Exact or Partial").required(true).hasArg().build());
		
		try
		{
			if(Setting.strType.toUpperCase().equals("IDE"))
			{
				fTesting_S = Lib.SubFileOrFolder(Setting.dirTesting, strTesting_S);
				fBaseline_L = Lib.SubFileOrFolder(Setting.dirTraining, strBaseline_L);
				
				if(strMethod.toLowerCase().equals("exact"))
				{
					fOutput = Lib.SubFileOrFolder(Setting.dirBaseline, "Baseline_Exact.txt");
				}
				else if(strMethod.toLowerCase().equals("partial"))
				{
					fOutput = Lib.SubFileOrFolder(Setting.dirBaseline, "Baseline_Partial.txt");
				}
			}
			else
			{	
				DefaultParser parser = new DefaultParser();
				CommandLine cmd = parser.parse(opts, args);
				
				fTesting_S = Lib.SubFileOrFolder(Setting.dirTesting, cmd.getOptionValue("strTesting_S"));
				fBaseline_L = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strBaseline_L"));
				strMethod = cmd.getOptionValue("strMethod");
				
				if(strMethod.toLowerCase().equals("exact"))
				{
					fOutput = Lib.SubFileOrFolder(Setting.dirBaseline, "Baseline_Exact.txt");
				}
				else if(strMethod.toLowerCase().equals("partial"))
				{
					fOutput = Lib.SubFileOrFolder(Setting.dirBaseline, "Baseline_Partial.txt");
				}
			}
			
			if(bParseArguments)
			{
				Setting.MyLog.info("fTesting_S = " + fTesting_S.getCanonicalPath());
				Setting.MyLog.info("fBaseline_L = " + fBaseline_L.getCanonicalPath());
				Setting.MyLog.info("strMethod = " + strMethod);
				Setting.MyLog.info("fOutput = " + fOutput.getCanonicalPath());
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
		
		ArrayList<String> alTesting_S = Lib.LoadArrayList(fTesting_S, "UTF-8", false);
		ArrayList<String> alBaseline_L = Lib.LoadArrayList(fBaseline_L, "UTF-8", false);
		ArrayList<String> alOutput = new ArrayList<String>();
		Setting.dirBaseline.mkdir();
		
		if(alTesting_S.size() == alBaseline_L.size())
		{	
			for(int i = 0 ; i < alTesting_S.size() ; i++)
			{
				alOutput.add("Testing: " + alTesting_S.get(i));
				alOutput.add("Baseline: " + alBaseline_L.get(i));
				if(strMethod.toLowerCase().equals("exact"))
				{
					alOutput.add(CalcPerformance_Exact(alTesting_S.get(i), alBaseline_L.get(i)));
				}
				else if(strMethod.toLowerCase().equals("partial"))
				{
					alOutput.add(CalcPerformance_Partial(alTesting_S.get(i), alBaseline_L.get(i)));
				}
				
				alOutput.add("");
				
				if(i > 0 && (i % 100000 == 0 || i == alTesting_S.size() - 1))
				{
					Setting.MyLog.info(Lib.GetProgress(i, alTesting_S.size()));
				}
			}
			
			if(strMethod.toLowerCase().equals("exact"))
			{
				alOutput.add("Entities	Extractions	Corrections	Precision	Recall	F-Measure");
				alOutput.add(CalcPerformance_Summary_Exact(alOutput));
			}
			else if(strMethod.toLowerCase().equals("partial"))
			{
				alOutput.add("Entities	Extractions	PrecisionScore	RecallScore	Precision	Recall	F-Measure");
				alOutput.add(CalcPerformance_Summary_Partial(alOutput));
			}
		}
		else
		{
			Setting.MyLog.warn("The sentences of testing data and baseline are not equal!");
		}
		
		Lib.SaveFile(alOutput, fOutput, false);
	}
	
	private static String CalcPerformance_Exact(String strTesting, String strBaseline) throws Exception
	{
		ArrayList<Integer> alTesting_Index_Start = calcIndex(strTesting, "Start");
		ArrayList<Integer> alTesting_Index_End = calcIndex(strTesting, "End");
		ArrayList<Integer> alBaseline_Index_Start = calcIndex(strBaseline, "Start");
		ArrayList<Integer> alBaseline_Index_End = calcIndex(strBaseline, "End");
		
		int iEntity = alTesting_Index_Start.size();
		int iExtraction = alBaseline_Index_Start.size();
		int iCorrection = 0;
		int iIndex_Baseline;
		
		for(int i = 0 ; i < alTesting_Index_Start.size() ; i++)
		{
			iIndex_Baseline = alBaseline_Index_Start.indexOf(alTesting_Index_Start.get(i));
			
			if(iIndex_Baseline != -1 && alBaseline_Index_End.get(iIndex_Baseline) == alTesting_Index_End.get(i))
			{
				iCorrection ++;
			}
		}
		
		String strPerformance = "Entities = " + String.valueOf(iEntity) + Setting.strSeparator_Tab;
				strPerformance += "Extractions = " + String.valueOf(iExtraction) + Setting.strSeparator_Tab;
				strPerformance += "Corrections = " + String.valueOf(iCorrection);
		return strPerformance;
	}
	
	private static String CalcPerformance_Partial(String strTesting, String strBaseline) throws Exception
	{
		ArrayList<Integer> alTesting_Index_Start = calcIndex(strTesting, "Start");
		ArrayList<Integer> alTesting_Index_End = calcIndex(strTesting, "End");
		ArrayList<Integer> alBaseline_Index_Start = calcIndex(strBaseline, "Start");
		ArrayList<Integer> alBaseline_Index_End = calcIndex(strBaseline, "End");
		HashSet<Integer> hsTesting_All = new HashSet<Integer>();
		HashSet<Integer> hsTesting = new HashSet<Integer>();
		HashSet<Integer> hsBaseline_All = new HashSet<Integer>();
		HashSet<Integer> hsBaseline = new HashSet<Integer>();
		
		int iEntity = alTesting_Index_Start.size();
		int iExtraction = alBaseline_Index_Start.size();
		int iPrecisionMatch = 0;
		int iRecallMatch = 0;
		float fPrecisionScore = 0.000000001f;
		float fRecallScore = 0.000000001f;
		
		for(int i = 0 ; i < alTesting_Index_Start.size() ; i++)
		{
			for(int j = alTesting_Index_Start.get(i); j <= alTesting_Index_End.get(i); j++)
			{
				hsTesting_All.add(j);
			}
		}
		
		for(int i = 0 ; i < alBaseline_Index_Start.size() ; i++)
		{
			for(int j = alBaseline_Index_Start.get(i); j <= alBaseline_Index_End.get(i); j++)
			{
				hsBaseline_All.add(j);
			}
		}
		
		// Precision
		for(int i = 0 ; i < alBaseline_Index_Start.size() ; i++)
		{
			for(int j = alBaseline_Index_Start.get(i); j <= alBaseline_Index_End.get(i); j++)
			{
				hsBaseline.add(j);
			}
			
			iPrecisionMatch = 0;
			for(int b:hsBaseline)
			{
				if(hsTesting_All.contains(b))
				{
					iPrecisionMatch++;
				}
			}
			
			if(iPrecisionMatch > 0)
			{
				fPrecisionScore += (float) iPrecisionMatch / hsBaseline.size();
			}
		}
		
		// Recall
		for(int i = 0 ; i < alTesting_Index_Start.size() ; i++)
		{
			for(int j = alTesting_Index_Start.get(i); j <= alTesting_Index_End.get(i); j++)
			{
				hsTesting.add(j);
			}
			
			iRecallMatch = 0;
			for(int t:hsTesting)
			{
				if(hsBaseline_All.contains(t))
				{
					iRecallMatch++;
				}
			}
			
			if(iRecallMatch > 0)
			{
				fRecallScore += (float) iRecallMatch / hsTesting.size();
			}
		}
		
		String strPerformance = "Entities = " + String.valueOf(iEntity) + Setting.strSeparator_Tab;
				strPerformance += "Extractions = " + String.valueOf(iExtraction) + Setting.strSeparator_Tab;
				strPerformance += "PrecisionScore = " + df4.format(fPrecisionScore) + Setting.strSeparator_Tab;
				strPerformance += "RecallScore = " + df4.format(fRecallScore);
		return strPerformance;
	}
	
	private static String CalcPerformance_Summary_Exact(ArrayList<String> alInput) throws Exception
	{	
		int iEntity = 0;
		int iExtraction = 0;
		int iCorrection = 0;
		float fPrecision = 0.000000001f;
		float fRecall = 0.000000001f;
		float fFMeasure = 0.0f;
		
		for(int i = 0 ; i < alInput.size() - 1 ; i++)
		{
			if(alInput.get(i).length() > 0 && alInput.get(i).startsWith("Entities = "))
			{
				iEntity += Integer.valueOf(alInput.get(i).split(Setting.strSeparator_Tab)[0].replace("Entities = ", ""));
				iExtraction += Integer.valueOf(alInput.get(i).split(Setting.strSeparator_Tab)[1].replace("Extractions = ", ""));
				iCorrection += Integer.valueOf(alInput.get(i).split(Setting.strSeparator_Tab)[2].replace("Corrections = ", ""));
			}
		}
		
		if(iExtraction > 0)
		{
			fPrecision = (float) iCorrection / iExtraction;
		}
		
		if(iEntity > 0)
		{
			fRecall = (float) iCorrection / iEntity;
		}
		
		if((fPrecision + fRecall) > 0.0f)
		{
			fFMeasure = 2 * fPrecision * fRecall / (fPrecision + fRecall);
		}
		
		String strSummary = "Entities = " + String.valueOf(iEntity) + Setting.strSeparator_Tab;
				strSummary += "Extractions = " + String.valueOf(iExtraction) + Setting.strSeparator_Tab;
				strSummary += "Corrections = " + String.valueOf(iCorrection) + Setting.strSeparator_Tab;
				strSummary += "Precision = " + df4.format(fPrecision) + Setting.strSeparator_Tab;
				strSummary += "Recall = " + df4.format(fRecall) + Setting.strSeparator_Tab;
				strSummary += "F-Measure = " + df4.format(fFMeasure);
		return strSummary;
	}
	
	private static String CalcPerformance_Summary_Partial(ArrayList<String> alInput) throws Exception
	{
		int iEntity = 0;
		int iExtraction = 0;
		float fPrecisionScore = 0.0f;
		float fRecallScore = 0.0f;
		float fPrecision = 0.000000001f;
		float fRecall = 0.000000001f;
		float fFMeasure = 0.0f;
				
		for(int i = 0 ; i < alInput.size() - 1 ; i++)
		{
			if(alInput.get(i).length() > 0 && alInput.get(i).startsWith("Entities = "))
			{
				iEntity += Integer.valueOf(alInput.get(i).split(Setting.strSeparator_Tab)[0].replace("Entities = ", ""));
				iExtraction += Integer.valueOf(alInput.get(i).split(Setting.strSeparator_Tab)[1].replace("Extractions = ", ""));
				fPrecisionScore += Float.valueOf(alInput.get(i).split(Setting.strSeparator_Tab)[2].replace("PrecisionScore = ", ""));
				fRecallScore += Float.valueOf(alInput.get(i).split(Setting.strSeparator_Tab)[3].replace("RecallScore = ", ""));
			}
		}
		
		if(iEntity > 0)
		{
			fPrecision = (float) fPrecisionScore / iExtraction;
		}
		
		if(iExtraction > 0)
		{
			fRecall = (float) fRecallScore / iEntity;
		}
		
		if((fPrecision + fRecall) > 0.0f)
		{
			fFMeasure = 2 * fPrecision * fRecall / (fPrecision + fRecall);
		}
		
		String strSummary = "Entities = " + String.valueOf(iEntity) + Setting.strSeparator_Tab;
				strSummary += "Extractions = " + String.valueOf(iExtraction) + Setting.strSeparator_Tab;
				strSummary += "PrecisionScore = " + df2.format(fPrecisionScore) + Setting.strSeparator_Tab;
				strSummary += "RecallScore = " + df2.format(fRecallScore) + Setting.strSeparator_Tab;
				strSummary += "Precision = " + df2.format(fPrecision) + Setting.strSeparator_Tab;
				strSummary += "Recall = " + df4.format(fRecall) + Setting.strSeparator_Tab;
				strSummary += "F-Measure = " + df4.format(fFMeasure);
		return strSummary;
	}
	
	private static ArrayList<Integer> calcIndex(String strInput, String strType) throws Exception
	{
		StringBuilder sbRow = new StringBuilder(strInput);
		ArrayList<Integer> alStart = new ArrayList<Integer>();
		ArrayList<Integer> alEnd = new ArrayList<Integer>();
		int iStart = 0;
		int iEnd = 0;
		
		while (iStart != - 1 && iEnd != - 1)
		{
			if(iStart == 0)
			{
				iStart = sbRow.indexOf(Setting.strEntity_Start, iStart);
			}
			else
			{
				iStart = sbRow.indexOf(Setting.strEntity_Start, iStart + 1);
			}
			
			if(iStart != - 1)
			{
				sbRow.delete(iStart, iStart + Setting.strEntity_Start.length());
			}
			
			// -----
			
			if(iEnd == 0)
			{
				iEnd = sbRow.indexOf(Setting.strEntity_End, iEnd);
			}
			else
			{
				iEnd = sbRow.indexOf(Setting.strEntity_End, iEnd + 1);
			}
			
			if(iEnd != - 1)
			{	
				sbRow.delete(iEnd, iEnd + Setting.strEntity_End.length());
			}
			
			// -----
			
			if(iStart != - 1 && iEnd != - 1 && iStart < iEnd)
			{
				alStart.add(iStart);
				alEnd.add(iEnd - 1);
			}
		}
		
		if(strType.toLowerCase().equals("start"))
		{
			return alStart;
		}
		else
		{
			return alEnd;
		}
	}
}
