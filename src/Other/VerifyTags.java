package Other;
import Global.*;
import java.io.File;
import java.util.ArrayList;
import org.apache.commons.cli.*;

public class VerifyTags
{
	static String strInput = "Activity_TestingData_S.txt";
	static String strType = "Testing"; // Training, Testing
	static File fInput;
	
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
		opts.addOption(Option.builder("strInput").desc("<WorkFolder\\Training or Testing\\>strInput.txt").required(true).hasArg().build());
		opts.addOption(Option.builder("strType").desc("Training or Testing").required(true).hasArg().build());
		
		try
		{
			if(Setting.strType.toUpperCase().equals("IDE"))
			{
				switch(strType.toLowerCase())
				{
					case "training":
						fInput = Lib.SubFileOrFolder(Setting.dirTraining, strInput);
						break;
						
					case "testing":
						fInput = Lib.SubFileOrFolder(Setting.dirTesting, strInput);
						break;
				}
			}
			else
			{	
				DefaultParser parser = new DefaultParser();
				CommandLine cmd = parser.parse(opts, args);
				
				strType = cmd.getOptionValue("strType");
				switch(strType.toLowerCase())
				{
					case "training":
						fInput = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strInput"));
						break;
						
					case "testing":
						fInput = Lib.SubFileOrFolder(Setting.dirTesting, cmd.getOptionValue("strInput"));
						break;
				}
			}
			
			if(bParseArguments)
			{
				Setting.MyLog.info("fInput = " + fInput.getCanonicalPath());
				Setting.MyLog.info("strType = " + strType);
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
		
		ArrayList<String> alInput = Lib.LoadArrayList(fInput, "UTF-8", false);
		ArrayList<String> alEntity = new ArrayList<String>();
		ArrayList<String> alTokens = new ArrayList<String>();
		StrIntPair pairEntity = new StrIntPair();
		int iCount = 0;
		boolean bNested = false;
		
		for(int i = 0 ; i < alInput.size() ; i++)
		{
			if(alInput.get(i).length() > 0)
			{
				alTokens = Lib.SentenceToToken(alInput.get(i));
				iCount = 0;
				bNested = false;
				
				for(int j = 0 ; j < alTokens.size() ; j++)
				{
					if(alTokens.get(j).equals(Setting.strEntity_Start))
					{
						iCount ++;
						if(iCount > 1)
						{
							bNested = true;
							Setting.MyLog.warn("i = " + (i + 1) + "\t" + alInput.get(i));
						}
					}
					else if(alTokens.get(j).equals(Setting.strEntity_End))
					{
						iCount --;
						if(iCount < 0)
						{
							bNested = true;
							Setting.MyLog.warn("i = " + (i + 1) + "\t" + alInput.get(i));
						}
					}
				}
				
				if(!bNested)
				{
					alEntity = Lib.GetLabeledEntity(alInput.get(i), false);
					
					for(String s:alEntity)
					{
						pairEntity.addString(s);
					}
				}
			}
		}
		
		pairEntity.sortByInt(StrIntPair.SortBy.DESC);
		Lib.SaveFile(pairEntity, Lib.GetFileWithSuffix(fInput, "_EntityList"), "", "all", false);
	}
}
