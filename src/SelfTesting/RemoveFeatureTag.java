package SelfTesting;
import Global.*;
import java.io.File;
import java.util.ArrayList;
import org.apache.commons.cli.*;

public class RemoveFeatureTag
{
	static String strInput_F = "Corpus_F.txt";
	static String strOutput_NoTag_F = "Corpus_NoTag_F.txt";
	static File fInput_F;
	static File fOutput_NoTag_F;
	
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
		opts.addOption(Option.builder("strInput_F").desc("<WorkFolder\\Training\\>strInput_F.txt").required(true).hasArg().build());
		opts.addOption(Option.builder("strOutput_NoTag_F").desc("<WorkFolder\\Training\\>strOutput_NoTag_F.txt").required(true).hasArg().build());
		
		try
		{
			if(Setting.strType.toUpperCase().equals("IDE"))
			{
				fInput_F = Lib.SubFileOrFolder(Setting.dirTraining, strInput_F);
				fOutput_NoTag_F = Lib.SubFileOrFolder(Setting.dirTraining, strOutput_NoTag_F);
			}
			else
			{
				DefaultParser parser = new DefaultParser();
				CommandLine cmd = parser.parse(opts, args);
				
				fInput_F = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strInput_F"));
				fOutput_NoTag_F = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strOutput_NoTag_F"));
			}
			
			if(bParseArguments)
			{
				Setting.MyLog.info("fInput_F = " + fInput_F.getCanonicalPath());
				Setting.MyLog.info("fOutput_NoTag_F = " + fOutput_NoTag_F.getCanonicalPath());
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
		
		ArrayList<String> alInput_F = Lib.LoadArrayList(fInput_F, "UTF-8", false);
		ArrayList<String> alOutput_NoTag_F = new ArrayList<String>();
		
		for(int i = 0; i < alInput_F.size() ; i++)
		{
			String strRow = alInput_F.get(i);
			
			if(strRow.length() > 0)
			{
				// Remove Last Separator and Answer (BIESO Tag)
				alOutput_NoTag_F.add(strRow.substring(0, strRow.length() - 2));
			}
			else
			{
				alOutput_NoTag_F.add("");
			}
			
			if(i > 0 && (i % 100000 == 0 || i == alInput_F.size() - 1))
			{
				Setting.MyLog.info(Lib.GetProgress(i, alInput_F.size()));
			}
		}
		
		Lib.SaveFile(alOutput_NoTag_F, fOutput_NoTag_F, false);
	}
}
