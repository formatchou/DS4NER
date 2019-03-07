package Testing;
import Global.*;
import java.io.File;
import java.util.ArrayList;
import org.apache.commons.cli.*;

public class Evaluation
{
	static String strModel = "Corpus.model";
	static String strMethod = "Exact"; // Exact or Partial
	static String strOutput_Dir = "Evaluation_Exact";
	static File fModel;
	static File fOutput_Dir;
	
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
		opts.addOption(Option.builder("strModel").desc("<WorkFolder\\Training\\>strModel").required(true).hasArg().build());
		opts.addOption(Option.builder("strMethod").desc("Exact or Partial").required(true).hasArg().build());
		opts.addOption(Option.builder("strOutput_Dir").desc("<WorkFolder\\>strOutput_Dir (directory)").required(true).hasArg().build());
		
		try
		{
			if(Setting.strType.toUpperCase().equals("IDE"))
			{
				fModel = Lib.SubFileOrFolder(Setting.dirTraining, strModel);
				fOutput_Dir = Lib.SubFileOrFolder(Setting.dirWorkFolder, strOutput_Dir);
			}
			else
			{	
				DefaultParser parser = new DefaultParser();
				CommandLine cmd = parser.parse(opts, args);
				
				fModel = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strModel"));
				strMethod = cmd.getOptionValue("strMethod");
				fOutput_Dir = Lib.SubFileOrFolder(Setting.dirWorkFolder, cmd.getOptionValue("strOutput_Dir"));
			}
			
			if(bParseArguments)
			{
				Setting.MyLog.info("fModel = " + fModel.getCanonicalPath());
				Setting.MyLog.info("strMethod = " + strMethod);
				Setting.MyLog.info("fOutput_Dir = " + fOutput_Dir.getCanonicalPath());	
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
		
		ArrayList<File> alFiles_Testing; 
		
		// Testing
		alFiles_Testing = Lib.GetFilesByExtension(Setting.dirTesting, "_F.txt");
		
		for(File f:alFiles_Testing)
		{
			File fTesting = Lib.SubFileOrFolder(Setting.dirTesting, f.getName());
			File fTR = Lib.SubFileOrFolder(Setting.dirTesting, Lib.GetPartialFilename(fTesting, "_F.txt") + "_TR.txt");
			
			Lib.ExecCommand(Lib.CRF_Testing(fModel, fTesting, fTR, 0));
		}
		
		// Evaluation
		ArrayList<File> alFiles_TR = Lib.GetFilesByExtension(Setting.dirTesting, "_TR.txt");
		
		File fSummary = Lib.SubFileOrFolder(fOutput_Dir, "Summary.txt");
		fOutput_Dir.mkdir();
		
		for(int i = 0 ; i < alFiles_TR.size() ; i++)
		{
			File fTestingResult = Lib.SubFileOrFolder(Setting.dirTesting, alFiles_TR.get(i).getName());
			File fDetail = Lib.SubFileOrFolder(fOutput_Dir, Lib.GetPartialFilename(fTestingResult, "_TR.txt") + "_Detail.txt");
			String strName = Lib.GetPartialFilename(fTestingResult, "_TR.txt");
			
			switch(strMethod.toUpperCase())
			{
				case "EXACT":
					TestingLib.Compare_Exact(fTestingResult, fDetail, fSummary, strName);
					break;
					
				case "PARTIAL":
					TestingLib.Compare_Partial(fTestingResult, fDetail, fSummary, strName);
					break;
			}
			
			Setting.MyLog.info("Progress = " + (i + 1) + "/" + alFiles_TR.size() + " Done!");
		}
		
		switch(strMethod.toUpperCase())
		{
			case "EXACT":
				TestingLib.Calc_Summary_Exact(fSummary);
				break;
				
			case "PARTIAL":
				TestingLib.Calc_Summary_Partial(fSummary);;
				break;
		}
	}
}
