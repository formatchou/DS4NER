package TriTraining;
import Global.*;
import org.apache.commons.cli.*;

public class TrainModel
{
	static int iIter = 1;
	static String strEvaluation = "None"; // Exact, Partial, All, or None
	static boolean bRemoveFiles = false;
	 
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
		opts.addOption(Option.builder("iIter").desc("Start iteration, 1").required(true).hasArg().build());
		opts.addOption(Option.builder("strEvaluation").desc("Exact, Partial, All, or None").required(true).hasArg().build());
		opts.addOption(Option.builder("bRemoveFiles").desc("True or False").required(true).hasArg().build());
		
		try
		{
			if(Setting.strType.toUpperCase().equals("IDE"))
			{
				// do nothing
			}
			else
			{
				DefaultParser parser = new DefaultParser();
				CommandLine cmd = parser.parse(opts, args);
				
				iIter = Integer.valueOf(cmd.getOptionValue("iIter"));
				strEvaluation = cmd.getOptionValue("strEvaluation");
				bRemoveFiles = Boolean.valueOf(cmd.getOptionValue("bRemoveFiles"));
			}
			
			if(bParseArguments)
			{
				Setting.MyLog.info("iIter = " + iIter);
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
		
		TriTraining_Obj[] Tri = (TriTraining_Obj[]) new TriTraining_Obj[Setting.iClassifier_Count];
		
		// Initialization tri-training Object
		for(int i = 0; i < Setting.iClassifier_Count; i++)
		{
			Tri[i] = new TriTraining_Obj(i);
		}
		
		int iRetrain = Setting.iClassifier_Count;
		while(iRetrain > 0) // iRetrain = 0 → all classifiers does not retrain → stop tri-trainng
		{
			for(int i = 0; i < Setting.iClassifier_Count; i++)
			{
				Setting.MyLog.info("Run tri-training: iteration = " + iIter + " classifiter = " + (i + 1));
				Tri[i].run(iIter);
			}
			
			iRetrain = 0;
			for(int i = 0; i < Setting.iClassifier_Count; i++)
			{
				iRetrain += Tri[i].isRetrain(); // retrain → return 1, else return 0
			}
			
			iIter++;
		}
		
		// Evaluation
		Setting.MyLog.info("Tri-Training done, start to evaluation.");
		for(int i = 0 ; i < Setting.iClassifier_Count ; i++)
		{
			Tri[i].renameModel();
		}
		
		switch(strEvaluation.toUpperCase())
		{
			case "EXACT":
				Tri[0].evaluation("EXACT", bRemoveFiles);
				break;
				
			case "PARTIAL":
				Tri[0].evaluation("PARTIAL", bRemoveFiles);
				break;
				
			case "ALL":
				Tri[0].evaluation("EXACT", bRemoveFiles);
				Tri[0].evaluation("PARTIAL", bRemoveFiles);
				break;
				
			default:
				break;
		}
		
		// Remove work files
		if(bRemoveFiles)
		{
			Tri[0].removeFiles();
		}
		
		for(int i = 0; i < Setting.iClassifier_Count; i++)
		{
			Tri[i] = null;
		}
		
		System.gc();
	}
}
