package Testing;
import Global.*;
import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;
import org.apache.commons.cli.*;

public class SplitByLanguage
{
	static String strInput_S = "KKBOX_Artist_S.txt";
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
				
			}
			
			if(bParseArguments)
			{
				Setting.MyLog.info("fInput_S = " + fInput_S.getCanonicalPath());
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
		ArrayList<String> alOutput_En = new ArrayList<String>();
		ArrayList<String> alOutput_Zh = new ArrayList<String>();
		ArrayList<String> alOutput_Ja = new ArrayList<String>();
		ArrayList<String> alOutput_Ko = new ArrayList<String>();
		ArrayList<String> alOutput_Num = new ArrayList<String>();
		ArrayList<String> alOutput_Other = new ArrayList<String>();
		ArrayList<String> alOutput_NoEntity = new ArrayList<String>();
		ArrayList<String> alEntity = new ArrayList<String>();
		
		String strEn = "";
		String strZh = "";
		String strJa = "";
		String strKo = "";
		String strNum = "";
		String strOther = "";
		String strLanguage = "";
		
		for(String s:alInput_S)
		{
			strEn = s;
			strZh = s;
			strJa = s;
			strKo = s;
			strNum = s;
			strOther = s;
			alEntity = Lib.GetLabeledEntity(s, false);
			
			if(alEntity.size() == 0) // No Entity
			{
				alOutput_NoEntity.add(s);
			}
			else
			{
				for(int i = 0 ; i < alEntity.size() ; i++)
				{
					strLanguage = Lib.GetLanguageMajor(alEntity.get(i)).toLowerCase();
					
					if(strLanguage.equals("english"))
					{
						strZh = strZh.replace(Setting.strEntity_Start + alEntity.get(i) + Setting.strEntity_End, alEntity.get(i));
						strJa = strJa.replace(Setting.strEntity_Start + alEntity.get(i) + Setting.strEntity_End, alEntity.get(i));
						strKo = strKo.replace(Setting.strEntity_Start + alEntity.get(i) + Setting.strEntity_End, alEntity.get(i));
						strNum = strNum.replace(Setting.strEntity_Start + alEntity.get(i) + Setting.strEntity_End, alEntity.get(i));
						strOther = strOther.replace(Setting.strEntity_Start + alEntity.get(i) + Setting.strEntity_End, alEntity.get(i));
					}
					else if(strLanguage.equals("chinese"))
					{
						strEn = strEn.replace(Setting.strEntity_Start + alEntity.get(i) + Setting.strEntity_End, alEntity.get(i));
						strJa = strJa.replace(Setting.strEntity_Start + alEntity.get(i) + Setting.strEntity_End, alEntity.get(i));
						strKo = strKo.replace(Setting.strEntity_Start + alEntity.get(i) + Setting.strEntity_End, alEntity.get(i));
						strNum = strNum.replace(Setting.strEntity_Start + alEntity.get(i) + Setting.strEntity_End, alEntity.get(i));
						strOther = strOther.replace(Setting.strEntity_Start + alEntity.get(i) + Setting.strEntity_End, alEntity.get(i));
					}
					else if(strLanguage.equals("japanese"))
					{
						strEn = strEn.replace(Setting.strEntity_Start + alEntity.get(i) + Setting.strEntity_End, alEntity.get(i));
						strZh = strZh.replace(Setting.strEntity_Start + alEntity.get(i) + Setting.strEntity_End, alEntity.get(i));
						strKo = strKo.replace(Setting.strEntity_Start + alEntity.get(i) + Setting.strEntity_End, alEntity.get(i));
						strNum = strNum.replace(Setting.strEntity_Start + alEntity.get(i) + Setting.strEntity_End, alEntity.get(i));
						strOther = strOther.replace(Setting.strEntity_Start + alEntity.get(i) + Setting.strEntity_End, alEntity.get(i));
					}
					else if(strLanguage.equals("korean"))
					{
						strEn = strEn.replace(Setting.strEntity_Start + alEntity.get(i) + Setting.strEntity_End, alEntity.get(i));
						strZh = strZh.replace(Setting.strEntity_Start + alEntity.get(i) + Setting.strEntity_End, alEntity.get(i));
						strJa = strJa.replace(Setting.strEntity_Start + alEntity.get(i) + Setting.strEntity_End, alEntity.get(i));
						strNum = strNum.replace(Setting.strEntity_Start + alEntity.get(i) + Setting.strEntity_End, alEntity.get(i));
						strOther = strOther.replace(Setting.strEntity_Start + alEntity.get(i) + Setting.strEntity_End, alEntity.get(i));
					}
					else if(strLanguage.equals("number"))
					{
						strEn = strEn.replace(Setting.strEntity_Start + alEntity.get(i) + Setting.strEntity_End, alEntity.get(i));
						strZh = strZh.replace(Setting.strEntity_Start + alEntity.get(i) + Setting.strEntity_End, alEntity.get(i));
						strJa = strJa.replace(Setting.strEntity_Start + alEntity.get(i) + Setting.strEntity_End, alEntity.get(i));
						strKo = strKo.replace(Setting.strEntity_Start + alEntity.get(i) + Setting.strEntity_End, alEntity.get(i));
						strOther = strOther.replace(Setting.strEntity_Start + alEntity.get(i) + Setting.strEntity_End, alEntity.get(i));
					}
					else // Other
					{
						strEn = strEn.replace(Setting.strEntity_Start + alEntity.get(i) + Setting.strEntity_End, alEntity.get(i));
						strZh = strZh.replace(Setting.strEntity_Start + alEntity.get(i) + Setting.strEntity_End, alEntity.get(i));
						strJa = strJa.replace(Setting.strEntity_Start + alEntity.get(i) + Setting.strEntity_End, alEntity.get(i));
						strKo = strKo.replace(Setting.strEntity_Start + alEntity.get(i) + Setting.strEntity_End, alEntity.get(i));
						strNum = strNum.replace(Setting.strEntity_Start + alEntity.get(i) + Setting.strEntity_End, alEntity.get(i));
					}
				}
				
				if(strEn.contains(Setting.strEntity_Start)) // strInV contains entity in vocabulary
				{
					alOutput_En.add(strEn);
				}
				
				if(strZh.contains(Setting.strEntity_Start))
				{
					alOutput_Zh.add(strZh);
				}
				
				if(strJa.contains(Setting.strEntity_Start))
				{
					alOutput_Ja.add(strJa);
				}
				
				if(strKo.contains(Setting.strEntity_Start))
				{
					alOutput_Ko.add(strKo);
				}
				
				if(strNum.contains(Setting.strEntity_Start))
				{
					alOutput_Num.add(strNum);
				}
				
				if(strOther.contains(Setting.strEntity_Start))
				{
					alOutput_Other.add(strOther);
				}
			}	
		}
		
		File fOutput_En = Lib.GetFileWithSuffix(fInput_S, "_En");
		File fOutput_Zh = Lib.GetFileWithSuffix(fInput_S, "_Zh");
		File fOutput_Ja = Lib.GetFileWithSuffix(fInput_S, "_Ja");
		File fOutput_Ko = Lib.GetFileWithSuffix(fInput_S, "_Ko");
		File fOutput_Num = Lib.GetFileWithSuffix(fInput_S, "_Num");
		File fOutput_Other = Lib.GetFileWithSuffix(fInput_S, "_Other");
		File fOutput_NoEntity = Lib.GetFileWithSuffix(fInput_S, "_NoEntity");
		
		Lib.SaveFile(alOutput_En, fOutput_En, false);
		Lib.SaveFile(alOutput_Zh, fOutput_Zh, false);
		Lib.SaveFile(alOutput_Ja, fOutput_Ja, false);
		Lib.SaveFile(alOutput_Ko, fOutput_Ko, false);
		Lib.SaveFile(alOutput_Num, fOutput_Num, false);
		Lib.SaveFile(alOutput_Other, fOutput_Other, false);
		Lib.SaveFile(alOutput_NoEntity, fOutput_NoEntity, false);
	}
}
