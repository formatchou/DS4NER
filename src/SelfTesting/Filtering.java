package SelfTesting;
import Global.*;
import java.io.File;
import java.util.ArrayList;
import org.apache.commons.cli.*;

public class Filtering
{
	static String strInput_L = "Corpus_L.txt";
	static String strInput_NoTag_TR = "Corpus_NoTag_TR.txt";
	static String strOutput_L = "Corpus_08_L.txt";
	static String strOutput_F = "Corpus_08_F.txt";
	static float fThreshold = 0.8f;
	static File fInput_L;
	static File fInput_NoTag_TR;
	static File fOutput_L;
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
		Setting.MyLog.info("Now Execuite: " + System.getProperty("sun.java.command"));
		
		boolean bParseArguments = true;
		HelpFormatter hf = new HelpFormatter();
		
		Options opts = new Options();
		opts.addOption(Option.builder("h").longOpt("help").desc("Shows argument examples").build());
		opts.addOption(Option.builder("Config").required(false).hasArg().build());
		opts.addOption(Option.builder("strInput_L").desc("<WorkFolder\\Training\\>strInput_L.txt").required(true).hasArg().build());
		opts.addOption(Option.builder("strInput_NoTag_TR").desc("<WorkFolder\\Training\\>strInput_NoTag_TR.txt").required(true).hasArg().build());
		opts.addOption(Option.builder("strOutput_L").desc("<WorkFolder\\Training\\>strOutput_L.txt").required(true).hasArg().build());
		opts.addOption(Option.builder("strOutput_F").desc("<WorkFolder\\Training\\>strOutput_F.txt").required(true).hasArg().build());
		opts.addOption(Option.builder("fThreshold").desc("Self-testing filter threshold, 0.0f-1.0f").required(true).hasArg().build());
		
		try
		{
			if(Setting.strType.toUpperCase().equals("IDE"))
			{
				fInput_L = Lib.SubFileOrFolder(Setting.dirTraining, strInput_L);
				fInput_NoTag_TR = Lib.SubFileOrFolder(Setting.dirTraining, strInput_NoTag_TR);
				fOutput_L = Lib.SubFileOrFolder(Setting.dirTraining, strOutput_L);
				fOutput_F = Lib.SubFileOrFolder(Setting.dirTraining, strOutput_F);
			}
			else
			{
				DefaultParser parser = new DefaultParser();
				CommandLine cmd = parser.parse(opts, args);
				
				fInput_L = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strInput_L"));
				fInput_NoTag_TR = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strInput_NoTag_TR"));
				fOutput_L = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strOutput_L"));
				fOutput_F = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strOutput_F"));
				fThreshold = Float.valueOf(cmd.getOptionValue("fThreshold"));
			}
			
			if(bParseArguments)
			{
				Setting.MyLog.info("fInput_L = " + fInput_L.getCanonicalPath());
				Setting.MyLog.info("fInput_NoTag_TR = " + fInput_NoTag_TR.getCanonicalPath());
				Setting.MyLog.info("fOutput_L = " + fOutput_L.getCanonicalPath());
				Setting.MyLog.info("fOutput_F = " + fOutput_F.getCanonicalPath());
				Setting.MyLog.info("fThreshold = " + fThreshold);
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
		
		ArrayList<String> alInput_L = Lib.LoadArrayList(fInput_L, "UTF-8", true);
		ArrayList<String> alInput_NoTag_TR = Lib.LoadArrayList(fInput_NoTag_TR, "UTF-8", true);
		ArrayList<String> alOutput_L = new ArrayList<String>();
		ArrayList<String> alOutput_F = new ArrayList<String>();
		ArrayList<Float> alProb = new ArrayList<Float>();
		
		// Find Sentence with Testing Prob. ≧ Threshold
		for(String s:alInput_NoTag_TR)
		{
			if(s.length() == 12 && s.startsWith("# 0 "))
			{	
				alProb.add(Float.valueOf(s.substring(4)));
				
				if(alProb.get(alProb.size() - 1) >= fThreshold)
				{
					alOutput_L.add(alInput_L.get(alProb.size() - 1));
				}
			}
		}
		
		Lib.SaveFile(alOutput_L, fOutput_L, false);
		
		// Features Extraction for fOutput, output = fOutput_F
		if(Setting.strFeature_Type.toLowerCase().equals("dictionary"))
		{
			Features_Dictionary features = new Features_Dictionary(Setting.dirWorkFolder);
			alOutput_F = features.Extraction(alOutput_L, true);
		}
		else
		{
			Features_Context features = new Features_Context();
			alOutput_F = features.Extraction(alOutput_L, true);
		}
		Lib.SaveFile(alOutput_F, fOutput_F, false);
	}
}
