package TriTraining;
import Global.*;
import java.io.*;
import java.util.ArrayList;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;

public class Initialization
{
	static String strInput_L = "A_S.txt";
	static String strInput_L_F = "A_F.txt";
	static String strInput_U = "U_S.txt";
	static String strSample = "Bootstrap"; // Bootstrap, Random70, DivideEqual
	static File fInput_L;
	static File fInput_L_F;
	static File fInput_U;
	static File fL;
	static File fL_F;
	static File fL_NoAns;
	static File fL_NoAns_F;
	static File fU;
	static File fU_F;
	
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
		opts.addOption(Option.builder("strInput_L_F").desc("<WorkFolder\\Training\\>strInput_L_F.txt").required(true).hasArg().build());
		opts.addOption(Option.builder("strInput_U").desc("<WorkFolder\\Training\\>strInput_U.txt").required(true).hasArg().build());
		opts.addOption(Option.builder("strSample").desc("Bootstrap or Random70 or DivideEqual").required(true).hasArg().build());
		
		try
		{
			if(Setting.strType.toUpperCase().equals("IDE"))
			{
				fInput_L = Lib.SubFileOrFolder(Setting.dirTraining, strInput_L);
				fInput_L_F = Lib.SubFileOrFolder(Setting.dirTraining, strInput_L_F);
				fInput_U = Lib.SubFileOrFolder(Setting.dirTraining, strInput_U);
			}
			else
			{
				DefaultParser parser = new DefaultParser();
				CommandLine cmd = parser.parse(opts, args);
				
				fInput_L = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strInput_L"));
				fInput_L_F = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strInput_L_F"));
				fInput_U = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strInput_U"));
				strSample = cmd.getOptionValue("strSample");
			}
			
			if(bParseArguments)
			{
				Setting.MyLog.info("fInput_L = " + fInput_L.getCanonicalPath());
				Setting.MyLog.info("fInput_L_F = " + fInput_L_F.getCanonicalPath());
				Setting.MyLog.info("fInput_U = " + fInput_U.getCanonicalPath());
				Setting.MyLog.info("strSample = " + strSample);
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
		
		// L			Labeled training data
		// L_F			Labeled training data with features matrix format
		// L_NoAns		Labeled training data but remove labeled Answers
		// L_NoAns_F	Labeled training data but remove labeled Answers with features matrix format
		// U			UnLabeled training data
		// U_F			UnLabeled training data with features matrix format
		
		Setting.dirTriTraining.mkdir();
		Setting.dirTriTraining_ErrorRate.mkdir();
		Setting.dirTriTraining_Model.mkdir();
		Setting.dirTriTraining_Training.mkdir();
		
		fL = Lib.SubFileOrFolder(Setting.dirTriTraining, "L.txt");
		fL_F = Lib.SubFileOrFolder(Setting.dirTriTraining, "L_F.txt");
		fL_NoAns = Lib.SubFileOrFolder(Setting.dirTriTraining, "L_NoAns.txt");
		fL_NoAns_F = Lib.SubFileOrFolder(Setting.dirTriTraining, "L_NoAns_F.txt");
		fU = Lib.SubFileOrFolder(Setting.dirTriTraining, "U.txt");
		fU_F = Lib.SubFileOrFolder(Setting.dirTriTraining, "U_F.txt");
		
		// Copy L and L_F
		FileUtils.copyFile(fInput_L, fL);
		FileUtils.copyFile(fInput_L_F, fL_F);
		
		// --------------------
		
		// L_NoAns
		Setting.MyLog.info("Start to prepare L_NoAns");
		
		ArrayList<String> alL = Lib.LoadArrayList(fL, "UTF-8", false);
		ArrayList<String> alL_NoAns = new ArrayList<String>();
		
		for(int i = 0 ; i < alL.size() ; i++)
		{
			alL_NoAns.add(alL.get(i).replace(Setting.strEntity_Start, "").replace(Setting.strEntity_End, ""));
			
			if(i > 0 && (i % 100000 == 0 || i == alL.size() - 1))
			{
				Setting.MyLog.info(Lib.GetProgress(i, alL.size()));
			}
		}
		
		Lib.SaveFile(alL_NoAns, fL_NoAns, false);
		
		// --------------------
		
		// L_NoAns_F
		Setting.MyLog.info("Start to prepare L_NoAns_F");
		
		ArrayList<String> alL_F = Lib.LoadArrayList(fL_F, "UTF-8", false);
		ArrayList<String> alL_NoAns_F = new ArrayList<String>();
		String strRow = "";
				
		for(int i = 0; i < alL_F.size() ; i++)
		{
			strRow = alL_F.get(i);
			
			if(strRow.length() > 0)
			{
				// Remove Last Separator and Answer (BIESO Tag)
				alL_NoAns_F.add(strRow.substring(0, strRow.length() - 2));
			}
			else
			{
				alL_NoAns_F.add("");
			}
			
			if(i > 0 && (i % 100000 == 0 || i == alL_F.size() - 1))
			{
				Setting.MyLog.info(Lib.GetProgress(i, alL_F.size()));
			}
		}
		
		Lib.SaveFile(alL_NoAns_F, fL_NoAns_F, false);
		
		// --------------------
		
		// U and U_F
		Setting.MyLog.info("Start to prepare U and U_F");
		FileUtils.copyFile(fInput_U, fU);
		
		ArrayList<String> alU = Lib.LoadArrayList(fU, "UTF-8", false);
		if(Setting.strFeature_Type.toLowerCase().equals("dictionary"))
		{
			Features_Dictionary features = new Features_Dictionary(Setting.dirWorkFolder);
			Lib.SaveFile(features.Extraction(alU, false), fU_F, false);
		}
		else
		{
			Features_Context features = new Features_Context();
			Lib.SaveFile(features.Extraction(alU, false), fU_F, false);
		}
		
		// --------------------
				
		// Sample Method: Bootstrap, Random70, DivideEqual
		Setting.MyLog.info("Start to sample training data to train I0 models");
		
		for(int i = 0; i < Setting.iClassifier_Count ; i++)
		{	
			File fTrainingData = Lib.SubFileOrFolder(Setting.dirTriTraining_Training, "H" + String.valueOf(i + 1) + "_I0_F.txt");
			File fModel = Lib.SubFileOrFolder(Setting.dirTriTraining_Model, "H" + String.valueOf(i + 1) + "_I0.model");
			SampleData(i, fL_F, fTrainingData);
			
			// train I0 model
			Lib.ExecCommand(Lib.CRF_Training(fTrainingData, fModel));
		}
	}
	
	public static void SampleData(int iClassifierID, File fInput, File fOutput) throws Exception
	{
		ArrayList<String> alInput = Lib.LoadArrayList(fInput, "UTF-8", true);
		ArrayList<String> alOutput = new ArrayList<String>();
		IntPair pairIndex = getFeaturesMatrix(alInput);
		
		// ----------
		
		int iPick = 0;
		int iPick_Max = 0;
		int iRandom = 0;
		
		switch(strSample.toUpperCase())
		{
			case "BOOTSTRAP":
				iPick_Max = pairIndex.size();
				
				while(iPick < iPick_Max)
				{
					iRandom = Lib.GetRandom(0, pairIndex.size() - 1);
					
					for(int i = pairIndex.get(iRandom).getStart(); i < pairIndex.get(iRandom).getEnd(); i++)
					{
						alOutput.add(alInput.get(i));
					}
					alOutput.add("");
					
					iPick++;
				}
				break;
				
			case "RANDOM70":
				iPick_Max = (int) Math.round(pairIndex.size() * 0.7);
				pairIndex.sortRandom();
				
				for(int i = 0 ; i < iPick_Max; i++)
				{
					for(int j = pairIndex.get(i).getStart(); j < pairIndex.get(i).getEnd(); j++)
					{
						alOutput.add(alInput.get(j));
					}
					alOutput.add("");
				}
				break;
				
			case "DIVIDEEQUAL":
				int iDivideEqual = (int) Math.round(pairIndex.size() / Setting.iClassifier_Count);
				int iIndexPair_Start = iDivideEqual * iClassifierID;
				int iIndexPairEnd = iDivideEqual * (iClassifierID + 1);
				
				if(iClassifierID == Setting.iClassifier_Count - 1)
				{
					iIndexPairEnd = pairIndex.size();
				}
				
				for(int i = iIndexPair_Start ; i < iIndexPairEnd; i++)
				{
					for(int j = pairIndex.get(i).getStart(); j < pairIndex.get(i).getEnd(); j++)
					{
						alOutput.add(alInput.get(j));
					}
					alOutput.add("");
				}
				break;
		}
		
		// remove last blank line
		alOutput.remove(alOutput.size() - 1);
		Lib.SaveFile(alOutput, fOutput, false);
	}
	
	private static IntPair getFeaturesMatrix(ArrayList<String> alInput) throws Exception
	{
		IntPair pairIndex = new IntPair();
		boolean bStart = false;
		
		int iID = 0;
		int iStart = 0;
		
		for(int i = 0; i < alInput.size(); i++)
		{
			if(alInput.get(i).length() > 0)
			{
				if(!bStart)
				{
					bStart = true;
					iStart = i;
				}
			}
			else if(alInput.get(i).length() == 0)
			{
				bStart = false;
				pairIndex.addPair(iID, iStart, i);
				iID++;
			}
			
			if(bStart && i == alInput.size() - 1)
			{
				pairIndex.addPair(iID, iStart, i);
			}
		}
		
		return pairIndex;
	}
}
