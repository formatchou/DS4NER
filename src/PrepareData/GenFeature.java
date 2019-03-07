package PrepareData;
import Global.*;
import java.io.File;
import java.util.*;
import org.apache.commons.cli.*;

public class GenFeature
{
	static String strInput_L = "LOC_L_ECJK_Ellipsis.txt"; // LabeledCorpus
	static String strOutput_F = "LOC_F.txt"; // LabeledCorpus_F
	static String strType = "Training"; // Training, Testing, Baseline, or Extractor
	static boolean bPreProcessing = false;
	static File fInput_L;
	static File fOutput_F;
	
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
		opts.addOption(Option.builder("strInput_L").desc("<WorkFolder\\Training or Testing\\>strInput_L.txt").required(true).hasArg().build());
		opts.addOption(Option.builder("strOutput_F").desc("<WorkFolder\\Training or Testing\\>strOutput_F.txt").required(true).hasArg().build());
		opts.addOption(Option.builder("strType").desc("Training, Testing, Baseline, or Extractor").required(true).hasArg().build());
		opts.addOption(Option.builder("bPreProcessing").desc("True or False").required(true).hasArg().build());
		
		try
		{
			if(Setting.strType.toUpperCase().equals("IDE"))
			{
				switch(strType.toLowerCase())
				{
					case "training":
						fInput_L = Lib.SubFileOrFolder(Setting.dirTraining, strInput_L);
						fOutput_F = Lib.SubFileOrFolder(Setting.dirTraining, strOutput_F);
						break;
						
					case "testing":
						if(bPreProcessing)
						{
							fInput_L = Lib.SubFileOrFolder(Setting.dirCorpusTesting, strInput_L);
						}
						else
						{
							fInput_L = Lib.SubFileOrFolder(Setting.dirTesting, strInput_L);
						}
						fOutput_F = Lib.SubFileOrFolder(Setting.dirTesting, strOutput_F);
						break;
						
					case "baseline":
						fInput_L = Lib.SubFileOrFolder(Setting.dirTraining, strInput_L);
						fOutput_F = Lib.SubFileOrFolder(Setting.dirTraining, strOutput_F);
						break;
				}
			}
			else
			{
				DefaultParser parser = new DefaultParser();
				CommandLine cmd = parser.parse(opts, args);
				
				strInput_L = cmd.getOptionValue("strInput_L");
				strType = cmd.getOptionValue("strType");
				bPreProcessing = Boolean.valueOf(cmd.getOptionValue("bPreProcessing"));
				switch(strType.toLowerCase())
				{
					case "training":
						fInput_L = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strInput_L"));
						fOutput_F = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strOutput_F"));
						break;
						
					case "testing":
						if(bPreProcessing)
						{
							fInput_L = Lib.SubFileOrFolder(Setting.dirCorpusTesting, cmd.getOptionValue("strInput_L"));
						}
						else
						{
							fInput_L = Lib.SubFileOrFolder(Setting.dirTesting, cmd.getOptionValue("strInput_L"));
						}
						fOutput_F = Lib.SubFileOrFolder(Setting.dirTesting, cmd.getOptionValue("strOutput_F"));
						break;
						
					case "baseline":
						fInput_L = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strInput_L"));
						fOutput_F = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strOutput_F"));
						break;
				}
			}
			
			if(bParseArguments)
			{
				Setting.MyLog.info("fInput_L = " + fInput_L.getCanonicalPath());
				Setting.MyLog.info("fOutput_F = " + fOutput_F.getCanonicalPath());
				Setting.MyLog.info("strType = " + strType);
				Setting.MyLog.info("bPreProcessing = " + bPreProcessing);
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
		
		// PreProcessing
		if(bPreProcessing)
		{
			File fInput = fInput_L;
			fInput_L = Lib.SubFileOrFolder(Setting.dirTesting, strInput_L.replaceAll("(?i).txt", "_S.txt"));
			PreProcessing.Segmentation.Run(fInput, fInput_L, strType, false, false, false, false);
		}
		
		// ----------
		
		ArrayList<String> alInput_L = Lib.LoadArrayList(fInput_L, "UTF-8", false);
		ArrayList<String> alOutput_F = new ArrayList<String>(); 
		if(Setting.strFeature_Type.toLowerCase().equals("dictionary"))
		{
			Features_Dictionary features = new Features_Dictionary(Setting.dirWorkFolder);
			alOutput_F = features.Extraction(alInput_L, true);
		}
		else
		{
			Features_Context features = new Features_Context();
			alOutput_F = features.Extraction(alInput_L, true);
		}
		Lib.SaveFile(alOutput_F, fOutput_F, false);
	}
}
