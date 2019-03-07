package Testing;
import Global.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import org.apache.commons.cli.*;

public class SplitByVocabulary
{
	static String strInput_S = "KKBOX_Artist_S.txt";
	static String strSeeds_S= "Artist_Seeds_Org_New_S.txt";
	static File fInput_S;
	static File fSeeds_S;
	
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
		opts.addOption(Option.builder("strSeeds_S").desc("<WorkFolder\\Training\\>strSeeds_S.txt").required(true).hasArg().build());
		
		try
		{
			if(Setting.strType.toUpperCase().equals("IDE"))
			{
				fInput_S = Lib.SubFileOrFolder(Setting.dirTesting, strInput_S);
				fSeeds_S = Lib.SubFileOrFolder(Setting.dirTraining, strSeeds_S);
			}
			else
			{	
				DefaultParser parser = new DefaultParser();
				CommandLine cmd = parser.parse(opts, args);
				
				fInput_S = Lib.SubFileOrFolder(Setting.dirTesting, cmd.getOptionValue("strInput_S"));
				fSeeds_S = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strSeeds_S"));
				
			}
			
			if(bParseArguments)
			{
				Setting.MyLog.info("fInput_S = " + fInput_S.getCanonicalPath());
				Setting.MyLog.info("fSeeds_S = " + fSeeds_S.getCanonicalPath());
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
		ArrayList<String> alSeeds_S = Lib.LoadArrayList(fSeeds_S, "UTF-8", false);
		ArrayList<String> alOutput_InV = new ArrayList<String>();
		ArrayList<String> alOutput_OOV = new ArrayList<String>();
		ArrayList<String> alOutput_NoEntity = new ArrayList<String>();
		ArrayList<String> alEntity = new ArrayList<String>();
		
		HashSet<String> hsSeeds = new HashSet<String>();
		for(String s:alSeeds_S)
		{
			hsSeeds.add(s);
		}
		
		String strInV = "";
		String strOOV = "";
		
		for(String s:alInput_S)
		{
			strInV = s;
			strOOV = s;
			alEntity = Lib.GetLabeledEntity(s, false);
			
			if(alEntity.size() == 0) // No Entity
			{
				alOutput_NoEntity.add(s);
			}
			else
			{
				for(int i = 0 ; i < alEntity.size() ; i++)
				{	
					if(hsSeeds.contains(alEntity.get(i))) // In Vocabulary
					{
						strOOV = strOOV.replace(Setting.strEntity_Start + alEntity.get(i) + Setting.strEntity_End, alEntity.get(i));
					}
					else // Out-of-Vocabulary
					{
						strInV = strInV.replace(Setting.strEntity_Start + alEntity.get(i) + Setting.strEntity_End, alEntity.get(i));		
					}
				}
				
				if(strInV.contains(Setting.strEntity_Start)) // strInV contains entity in vocabulary
				{
					alOutput_InV.add(strInV);
				}
				
				if(strOOV.contains(Setting.strEntity_Start)) // strOOV contains entity out-of-vocabulary
				{
					alOutput_OOV.add(strOOV);
				}
			}	
		}
		
		File fOutput_InV = Lib.GetFileWithSuffix(fInput_S, "_InV");
		File fOutput_OOV = Lib.GetFileWithSuffix(fInput_S, "_OOV");
		File fOutput_NoEntity = Lib.GetFileWithSuffix(fInput_S, "_NoEntity");
		
		Lib.SaveFile(alOutput_InV, fOutput_InV, false);
		Lib.SaveFile(alOutput_OOV, fOutput_OOV, false);
		Lib.SaveFile(alOutput_NoEntity, fOutput_NoEntity, false);
	}
}
