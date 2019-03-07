package PrepareData;
import Global.*;
import java.io.*;
import java.util.ArrayList;
import org.apache.commons.cli.*;

public class RemoveLabeling
{
	static String strInput_L = "Baseline_L.txt";
	static String strRemove = "Baseline_Remove.txt";
	static String strOutput_L = "Baseline_L_Removed.txt";
	static boolean bFilterNegExamples = true;
	
	static File fInput_L;
	static File fRemove	;
	static File fOutput_L;
	static ArrayList<String> alInput_L = new ArrayList<String>();
	static ArrayList<String> alRemove = new ArrayList<String>();
	static ArrayList<String> alOutput_L = new ArrayList<String>();
	
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
		opts.addOption(Option.builder("strRemove").desc("<WorkFolder\\Training\\>strRemove.txt").required(true).hasArg().build());
		opts.addOption(Option.builder("strOutput_L").desc("<WorkFolder\\Training\\>strOutput_L.txt").required(true).hasArg().build());
		opts.addOption(Option.builder("bFilterNegExamples").desc("True or False").required(true).hasArg().build());
		
		try
		{
			if(Setting.strType.toUpperCase().equals("IDE"))
			{
				fInput_L = Lib.SubFileOrFolder(Setting.dirTraining, strInput_L);
				fRemove = Lib.SubFileOrFolder(Setting.dirTraining, strRemove);
				fOutput_L = Lib.SubFileOrFolder(Setting.dirTraining, strOutput_L);
			}
			else
			{
				DefaultParser parser = new DefaultParser();
				CommandLine cmd = parser.parse(opts, args);
				
				fInput_L = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strInput_L"));
				fRemove = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strRemove"));
				fOutput_L = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strOutput_L"));
				bFilterNegExamples = Boolean.valueOf(cmd.getOptionValue("bFilterNegExamples"));
			}
			
			if(bParseArguments)
			{
				Setting.MyLog.info("fInput_L = " + fInput_L.getCanonicalPath());
				Setting.MyLog.info("fRemove = " + fRemove.getCanonicalPath());
				Setting.MyLog.info("fOutput_L = " + fOutput_L.getCanonicalPath());
				Setting.MyLog.info("bFilterNegExamples = " + bFilterNegExamples);
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
		
		alInput_L = Lib.LoadArrayList(fInput_L, "UTF-8", false);
		alRemove = Lib.LoadArrayList(fRemove, "UTF-8", false);
				
		String strSentence = "";
		String strEntity = "";
		
		for(int i = 0 ; i < alInput_L.size() ; i++)
		{
			strSentence = alInput_L.get(i);
			
			for(int j = 0 ; j < alRemove.size() ; j++)
			{
				strEntity = Setting.strEntity_Start + alRemove.get(j) + Setting.strEntity_End;
				if(strSentence.contains(strEntity))
				{
					strSentence = strSentence.replace(strEntity, alRemove.get(j));
				}
			}
			
			if(bFilterNegExamples)
			{
				if(strSentence.contains(Setting.strEntity_Start))
				{
					alOutput_L.add(strSentence);
				}
			}
			else
			{
				alOutput_L.add(strSentence);
			}
		}
		
		Lib.SaveFile(alOutput_L, fOutput_L, false);
	}
}
