package Training;
import Global.*;
import java.io.File;
import org.apache.commons.cli.*;

public class CRF
{
	static String strInput_F = "LOC_F.txt";
	static String strModel = "LOC.model";	
	static File fInput_F;
	static File fModel;
	
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
		opts.addOption(Option.builder("strInput_F").desc("<WorkFolder\\Training\\>strInput_F.txt").required(true).hasArg().build());
		opts.addOption(Option.builder("strModel").desc("<WorkFolder\\Training\\>strModel").required(true).hasArg().build());
		
		try
		{
			if(Setting.strType.toUpperCase().equals("IDE"))
			{
				fInput_F = Lib.SubFileOrFolder(Setting.dirTraining, strInput_F);
				fModel = Lib.SubFileOrFolder(Setting.dirTraining, strModel);
			}
			else
			{
				DefaultParser parser = new DefaultParser();
				CommandLine cmd = parser.parse(opts, args);
				
				fInput_F = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strInput_F"));
				fModel = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strModel"));
			}
			
			if(bParseArguments)
			{
				Setting.MyLog.info("fInput_F = " + fInput_F.getCanonicalPath());
				Setting.MyLog.info("fModel = " + fModel.getCanonicalPath());
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
		
		Lib.ExecCommand(Lib.CRF_Training(fInput_F, fModel));
	}
}
