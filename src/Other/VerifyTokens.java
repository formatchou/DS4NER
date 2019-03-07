package Other;
import Global.*;
import java.io.File;
import java.util.ArrayList;
import org.apache.commons.cli.*;

public class VerifyTokens
{
	static String strInput_1 = "Baseline_F.txt";
	static String strInput_2 = "Artist_F.txt";
	static String strType_1 = "Training"; // Training, Testing
	static String strType_2 = "Testing"; // Training, Testing
	static File fInput_1;
	static File fInput_2;
	
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
		opts.addOption(Option.builder("strInput_1").desc("<WorkFolder\\Training or Testing\\>strInput_1.txt").required(true).hasArg().build());
		opts.addOption(Option.builder("strInput_2").desc("<WorkFolder\\Training or Testing\\>strInput_2.txt").required(true).hasArg().build());
		opts.addOption(Option.builder("strType_1").desc("Training or Testing").required(true).hasArg().build());
		opts.addOption(Option.builder("strType_2").desc("Training or Testing").required(true).hasArg().build());
		
		try
		{
			if(Setting.strType.toUpperCase().equals("IDE"))
			{
				switch(strType_1.toLowerCase())
				{
					case "training":
						fInput_1 = Lib.SubFileOrFolder(Setting.dirTraining, strInput_1);
						break;
						
					case "testing":
						fInput_1 = Lib.SubFileOrFolder(Setting.dirTesting, strInput_1);
						break;
				}
				
				switch(strType_2.toLowerCase())
				{
					case "training":
						fInput_2 = Lib.SubFileOrFolder(Setting.dirTraining, strInput_2);
						break;
						
					case "testing":
						fInput_2 = Lib.SubFileOrFolder(Setting.dirTesting, strInput_2);
						break;
				}
			}
			else
			{	
				DefaultParser parser = new DefaultParser();
				CommandLine cmd = parser.parse(opts, args);
				
				strType_1 = cmd.getOptionValue("strType_1");
				strType_2 = cmd.getOptionValue("strType_2");
				
				switch(strType_1.toLowerCase())
				{
					case "training":
						fInput_1 = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strInput_1"));
						break;
						
					case "testing":
						fInput_1 = Lib.SubFileOrFolder(Setting.dirTesting, cmd.getOptionValue("strInput_1"));
						break;
				}
				
				switch(strType_2.toLowerCase())
				{
					case "training":
						fInput_2 = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strInput_2"));
						break;
						
					case "testing":
						fInput_2 = Lib.SubFileOrFolder(Setting.dirTesting, cmd.getOptionValue("strInput_2"));
						break;
				}
			}
			
			if(bParseArguments)
			{
				Setting.MyLog.info("fInput_1 = " + fInput_1.getCanonicalPath());
				Setting.MyLog.info("fInput_2 = " + fInput_2.getCanonicalPath());
				Setting.MyLog.info("strType_1 = " + strType_1);
				Setting.MyLog.info("strType_2 = " + strType_2);
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
		
		ArrayList<String> alInput_1 = Lib.LoadArrayList(fInput_1, "UTF-8", false);
		ArrayList<String> alInput_2 = Lib.LoadArrayList(fInput_2, "UTF-8", false);
		String strLine_1 = "";
		String strLine_2 = "";
		String strToken_1 = "";
		String strToken_2 = "";
		
		if(alInput_1.size() == alInput_2.size())
		{
			for(int i = 0 ; i < alInput_1.size() ; i++)
			{
				strLine_1 = alInput_1.get(i);
				strLine_2 = alInput_2.get(i);
				
				if(strLine_1.length() == strLine_2.length())
				{
					if(strLine_1.length() > 0)
					{
						strToken_1 = strLine_1.split(Setting.strSeparator_Tab)[0];
						strToken_2 = strLine_2.split(Setting.strSeparator_Tab)[0];
						
						if(!strToken_1.equals(strToken_2))
						{
							Setting.MyLog.warn("Two files are not match at index = " + (i + 1));
						}
					}
				}
				else
				{
					Setting.MyLog.warn("Two files are not match at index = " + (i + 1));
				}
			}
		}
		else
		{
			Setting.MyLog.warn("The sizes of two files are not equal.");
		}
	}
}
