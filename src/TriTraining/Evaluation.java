package TriTraining;
import Global.*;
import org.apache.commons.cli.*;

public class Evaluation
{
	static String strEvaluation = "Exact"; // Exact, Partial, All, or None
	static boolean bRemoveFiles = true;
	
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
		opts.addOption(Option.builder("strEvaluation").desc("Exact, Partial, All, or None").required(true).hasArg().build());
		opts.addOption(Option.builder("bRemoveFiles").desc("True or False").required(true).hasArg().build());
		
		try
		{
			if(Setting.strType.toUpperCase().equals("IDE"))
			{
				// Do Nothing
			}
			else
			{
				DefaultParser parser = new DefaultParser();
				CommandLine cmd = parser.parse(opts, args);
				
				strEvaluation = cmd.getOptionValue("strEvaluation");
				bRemoveFiles = Boolean.valueOf(cmd.getOptionValue("bRemoveFiles"));
			}
			
			if(bParseArguments)
			{
				Setting.MyLog.info("strEvaluation = " + strEvaluation);
				Setting.MyLog.info("bRemoveFiles = " + bRemoveFiles);
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
		
		TriTraining_Obj Tri = new TriTraining_Obj(1);
		
		switch(strEvaluation.toUpperCase())
		{
			case "EXACT":
				Tri.evaluation("EXACT", bRemoveFiles);
				break;
				
			case "PARTIAL":
				Tri.evaluation("PARTIAL", bRemoveFiles);
				break;
				
			case "ALL":
				Tri.evaluation("EXACT", bRemoveFiles);
				Tri.evaluation("PARTIAL", bRemoveFiles);
				break;
				
			default:
				break;
		}
	}
}
