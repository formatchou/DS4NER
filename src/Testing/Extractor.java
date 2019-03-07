package Testing;
import Global.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;

public class Extractor
{
	static String strModel = "Corpus.model";
	static String strInput = "UnLabeledCorpus.txt";
	static String strOutput = "UnLabeledCorpus_Output.txt";
	static File fModel;
	static File fInput;
	static File fOutput;
	
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
		opts.addOption(Option.builder("strModel").desc("<WorkFolder\\Training\\>strModel").required(false).hasArg().build());
		opts.addOption(Option.builder("strInput").desc("<Corpus\\Testing\\>strInput.txt").required(true).hasArg().build());
		opts.addOption(Option.builder("strOutput").desc("<Corpus\\Testing\\>strOutput.txt").required(true).hasArg().build());
		
		try
		{
			if(Setting.strType.toUpperCase().equals("IDE"))
			{	
				fModel = Lib.SubFileOrFolder(Setting.dirTraining, strModel);
				fInput = Lib.SubFileOrFolder(Setting.dirCorpusTesting, strInput);
				fOutput = Lib.SubFileOrFolder(Setting.dirCorpusTesting, strOutput);
			}
			else
			{
				DefaultParser parser = new DefaultParser();
				CommandLine cmd = parser.parse(opts, args);
				
				fModel = Lib.SubFileOrFolder(Setting.dirTraining, cmd.getOptionValue("strModel"));
				fInput = Lib.SubFileOrFolder(Setting.dirCorpusTesting, cmd.getOptionValue("strInput"));
				fOutput = Lib.SubFileOrFolder(Setting.dirCorpusTesting, cmd.getOptionValue("strOutput"));
			}
			
			if(bParseArguments)
			{
				Setting.MyLog.info("fModel = " + fModel.getCanonicalPath());
				Setting.MyLog.info("fInput = " + fInput.getCanonicalPath());
				Setting.MyLog.info("fOutput = " + fOutput.getCanonicalPath());
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
		
		SimpleDateFormat sdf = new SimpleDateFormat("HHmmssSSS");
		File dirTemp = new File("Temp_" + sdf.format(new java.util.Date()));
		File fInput_S = Lib.SubFileOrFolder(dirTemp, "Temp_S.txt");
		File fInput_F = Lib.SubFileOrFolder(dirTemp, "Temp_F.txt");
		File fInput_TR = Lib.SubFileOrFolder(dirTemp, "Temp_TR.txt");
		dirTemp.mkdir();
		
		// ----------
		
		ArrayList<String> alInput_S;
		ArrayList<String> alInput_F;
		
		boolean bSplitBySpace = false;
		boolean bFilterNoneECJK = false;
		boolean bFilterEllipsis = false;
		boolean bOutputReport = false;
		
		// PreProcessing
		PreProcessing.Segmentation.Run(fInput, fInput_S, "extractor", bSplitBySpace, bFilterNoneECJK, bFilterEllipsis, bOutputReport);
		alInput_S = Lib.LoadArrayList(fInput_S, "UTF-8", false);
		
		// Generate Feature Matrix
		if(Setting.strFeature_Type.toLowerCase().equals("dictionary"))
		{
			Features_Dictionary features = new Features_Dictionary(Setting.dirWorkFolder);
			alInput_F = features.Extraction(alInput_S, true);
		}
		else
		{
			Features_Context features = new Features_Context();
			alInput_F = features.Extraction(alInput_S, true);
		}
		Lib.SaveFile(alInput_F, fInput_F, false);
		
		// Testing
		Lib.ExecCommand(Lib.CRF_Testing(fModel, fInput_F, fInput_TR, 0));
		
		// Extract Entity
		Lib.SaveFile(ExtractEntity(fInput_TR), fOutput, false);
		
		// Remove Temp Files
		FileUtils.deleteDirectory(dirTemp);
	}
	
	private static ArrayList<String> ExtractEntity(File fTR) throws Exception
	{	
		ArrayList<String> alTR = Lib.LoadArrayList(fTR, "UTF-8", false);
		ArrayList<String> alOutput = new ArrayList<String>();
		String strOutput = "";
		String strRow = "";
		
		for(int i = 0 ; i < alTR.size() ; i++)
		{
			strRow = alTR.get(i);
			
			if(strRow.length() > 0)
			{
				String strToken = strRow.split(Setting.strCRFTesting_Separator)[0];
				String strTR = strRow.substring(strRow.length() - 1, strRow.length());
				
				if(!strToken.equals(Setting.strSeparator_Extractor))
				{
					if(strTR.equals("S"))
					{
						strOutput += Setting.strEntity_Start + strToken + Setting.strEntity_End;
					}
					else if(strTR.equals("B"))
					{
						strOutput += Setting.strEntity_Start + strToken;
					}
					else if(strTR.equals("I"))
					{
						strOutput += strToken;
						
					}
					else if(strTR.equals("E"))
					{
						strOutput += strToken + Setting.strEntity_End;
					}
					else // O
					{
						strOutput += strToken;					
					}
				}
				else if(strOutput.length() > 0)
				{
					alOutput.add(strOutput.replace("﹍", " "));
					strOutput = "";
				}
			}
		}
		
		return alOutput;
	}
}
