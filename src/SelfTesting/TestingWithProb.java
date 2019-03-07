package SelfTesting;
import Global.*;
import java.io.File;
import org.apache.commons.cli.*;

public class TestingWithProb
{
	static String strModel = "Corpus.model";
	static String strInput_NoTag_F = "Corpus_NoTag_F.txt";
	static String strOutput_NoTag_TR = "Corpus_NoTag_TR.txt";
	static File fModel;
	static File fInput_NoTag_F;
	static File fOutput_NoTag_TR;
	
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
		Setting.MyLog.info("Now Execuite: " + System.getProperty("sun.java.command"));
		
		boolean bParseArguments = true;
		HelpFormatter hf = new HelpFormatter();
		
		Options opts = new Options();
		opts.addOption(Option.builder("h").longOpt("help").desc("Shows argument examples").build());
		opts.addOption(Option.builder("Config").required(false).hasArg().build());
		opts.addOption(Option.builder("strModel").desc("<WorkFolder\\Training\\>strModel").required(true).hasArg().build());
		opts.addOption(Option.builder("strInput_NoTag_F").desc("<WorkFolder\\Training\\>strInput_NoTag_F.txt").required(true).hasArg().build());
		opts.addOption(Option.builder("strOutput_NoTag_TR").desc("<WorkFolder\\Training\\>strOutput_NoTag_TR.txt").required(true).hasArg().build());
		
		try
		{
			if(Setting.strType.toUpperCase().equals("IDE"))
			{
				fModel = Lib.SubFileOrFolder(Setting.dirTraining, strModel);
				fInput_NoTag_F = Lib.SubFileOrFolder(Setting.dirTraining, strInput_NoTag_F);
				fOutput_NoTag_TR = Lib.SubFileOrFolder(Setting.dirTraining, strOutput_NoTag_TR);
			}
			else
			{
				DefaultParser parser = new DefaultParser();
				CommandLine cmd = parser.parse(opts, args);
				
				fModel = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strModel"));
				fInput_NoTag_F = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strInput_NoTag_F"));
				fOutput_NoTag_TR = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strOutput_NoTag_TR"));
			}
			
			if(bParseArguments)
			{
				Setting.MyLog.info("fModel = " + fModel.getCanonicalPath());
				Setting.MyLog.info("fInput_NoTag_F = " + fInput_NoTag_F.getCanonicalPath());
				Setting.MyLog.info("fOutput_NoTag_TR = " + fOutput_NoTag_TR.getCanonicalPath());
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
		
		Lib.ExecCommand(Lib.CRF_Testing(fModel, fInput_NoTag_F, fOutput_NoTag_TR, 1));
	}
}
