package Baseline;
import Global.*;
import java.io.File;
import java.util.ArrayList;
import org.apache.commons.cli.*;

public class RemoveTags
{
	static String strInput_S = "KKBOX_Artist_S.txt";
	static String strOutput_S = "Baseline_S.txt";
	static File fInput_S;
	static File fOutput_S;
	
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
		opts.addOption(Option.builder("strInput_S").desc("<WorkFolder\\Testing\\>strInput_S.txt").required(true).hasArg().build());
		opts.addOption(Option.builder("strOutput_S").desc("<WorkFolder\\Training\\>strOutput_S.txt").required(true).hasArg().build());
		
		try
		{
			if(Setting.strType.toUpperCase().equals("IDE"))
			{
				fInput_S = Lib.SubFileOrFolder(Setting.dirTesting, strInput_S);
				fOutput_S = Lib.SubFileOrFolder(Setting.dirTraining, strOutput_S);
			}
			else
			{
				DefaultParser parser = new DefaultParser();
				CommandLine cmd = parser.parse(opts, args);
				
				fInput_S = Lib.SubFileOrFolder(Setting.dirTesting, cmd.getOptionValue("strInput_S"));
				fOutput_S = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strOutput_S"));
			}
			
			if(bParseArguments)
			{
				Setting.MyLog.info("fInput_S = " + fInput_S.getCanonicalPath());
				Setting.MyLog.info("fOutput_S = " + fOutput_S.getCanonicalPath());
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
		
		ArrayList<String> alInput_S = Lib.LoadArrayList(fInput_S, "UTF-8", false);
		ArrayList<String> alOutput_S = new ArrayList<String>();
		
		for(String s:alInput_S)
		{
			alOutput_S.add(s.replace(Setting.strEntity_Start, "").replace(Setting.strEntity_End, ""));
		}
		
		Lib.SaveFile(alOutput_S, fOutput_S, false);
	}
}
