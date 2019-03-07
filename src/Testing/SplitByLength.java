package Testing;
import Global.*;
import java.io.File;
import java.util.ArrayList;
import org.apache.commons.cli.*;

public class SplitByLength
{
	static String strInput_S = "KKBOX_Artist_S.txt";
	static int iSplited = 10;
	static File fInput_S;
	
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
		opts.addOption(Option.builder("iSplited").desc("Splited Parts, this value > 0").required(true).hasArg().build());
		
		try
		{
			if(Setting.strType.toUpperCase().equals("IDE"))
			{
				fInput_S = Lib.SubFileOrFolder(Setting.dirTesting, strInput_S);
			}
			else
			{	
				DefaultParser parser = new DefaultParser();
				CommandLine cmd = parser.parse(opts, args);
				
				fInput_S = Lib.SubFileOrFolder(Setting.dirTesting, cmd.getOptionValue("strInput_S"));
				iSplited = Integer.valueOf(cmd.getOptionValue("iSplited"));
			}
			
			if(bParseArguments)
			{
				Setting.MyLog.info("fInput_S = " + fInput_S.getCanonicalPath());
				Setting.MyLog.info("iSplited = " + iSplited);	
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
		ArrayList<String>[] alOutput = new ArrayList[iSplited + 1];
		ArrayList<String> alEntity = new ArrayList<String>();
		boolean bOutput = false;
		int iLength = 0;
		
		for(int i = 0; i < iSplited + 1; i++)
		{
			alOutput[i] = new ArrayList<String>();
			
			for(String s:alInput_S)
			{
				alEntity = Lib.GetLabeledEntity(s, false);
				
				if(i == 0 && alEntity.size() == 0) // negative example
				{
					alOutput[0].add(s);
				}
				else if(i > 0) // postive example
				{
					bOutput = false;
					
					for(int j = 0 ; j < alEntity.size() ; j++)
					{
						iLength = Lib.GetTokenLength(alEntity.get(j));
						
						if(i < iSplited && i == iLength)
						{
							bOutput = true;
						}
						else if(i == iSplited && i <= iLength) // last splited output
						{
							bOutput = true;
						}
						else // remove tag
						{
							s = s.replace(Setting.strEntity_Start + alEntity.get(j) + Setting.strEntity_End, alEntity.get(j));
						}
					}
					
					if(bOutput)
					{
						alOutput[i].add(s);
					}
				}	
			}
			
			File fOutput = Lib.GetFileWithSuffix(fInput_S, "_" + String.format("%02d",i));
			Lib.SaveFile(alOutput[i], fOutput, false);
		}
	}
}
