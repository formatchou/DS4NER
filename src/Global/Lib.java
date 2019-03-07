package Global;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class Lib
{
	public static ArrayList<String> LoadArrayList(File fInput, String strEncoding, boolean bSkipComment ) throws IOException
	{
		ArrayList<String> alContent = new ArrayList<String>();
		
		if(fInput.exists())
		{
			String strLine = "";
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fInput), strEncoding), 8192);
			
			while((strLine = br.readLine()) != null)
			{
				if(strLine.length() == 0)
				{
					alContent.add("");
				}
				else 
				{
					if (strLine.charAt(0) == 65279) // Remove UTF-8 BOM Char
					{
						strLine = strLine.substring(1, strLine.length());
					}
					else if(strLine.startsWith("﻿")) // Remove First Special Char
					{
						strLine = strLine.substring(1,strLine.length());
					}
					
					if(bSkipComment)
					{
						if(!strLine.startsWith("%%"))
						{
							alContent.add(strLine);
						}
					}
					else
					{
						alContent.add(strLine);
					}
				}
			}
			br.close();
		}
		else
		{
			Setting.MyLog.warn(fInput.getAbsolutePath() + " does not exist!");
		}
		System.gc();
		return alContent;
	}
		
	public static StrIntPair LoadStrFreqPair(File fInput, String strEncoding) throws IOException
	{
		ArrayList<String> alContent = new ArrayList<String>();
		alContent = LoadArrayList(fInput, strEncoding, true);
		
		StrIntPair pair = new StrIntPair();
		
		if(fInput.exists())
		{
			for(String s:alContent)
			{
				pair.loadStrFreqPair(new StrIntPair(s.split(Setting.strSeparator_Tab)[0], Integer.valueOf(s.split(Setting.strSeparator_Tab)[1])));
			}
		}
		else
		{
			Setting.MyLog.warn(fInput.getAbsolutePath() + " does not exist!");
		}
		
		System.gc();
		return pair;
	}
	
	public static ArrayList<String> GetFolderPathList(File dirInput) throws IOException
	{
		ArrayList<String> alFolderList = new ArrayList<String>();
		
		File[] fInput = dirInput.listFiles(new FileFilter()
		{
			public boolean accept(File dir)
			{
				return dir.isDirectory();
			}
		});
		
		for(int i = 0; i < fInput.length; i++)
		{			
			alFolderList.add(fInput[i].getPath());
		}
		Collections.sort(alFolderList);
		return alFolderList;
	}

	public static ArrayList<String> GetFolderNameList(File dirInput) throws IOException
	{
		ArrayList<String> alFolderList = new ArrayList<String>();
		
		File[] fInput = dirInput.listFiles(new FileFilter()
		{
			public boolean accept(File dir)
			{
				return dir.isDirectory();
			}
		});
		
		for(int i = 0; i < fInput.length; i++)
		{			
			alFolderList.add(fInput[i].getName());
		}
		Collections.sort(alFolderList);
		return alFolderList;
	}
	
	public static ArrayList<File> GetFilesByRegex(File dirInput, final String strRegex) throws IOException
	{
		ArrayList<File> alFile = new ArrayList<File>();
		
		File[] fInput = dirInput.listFiles(new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				return name.matches("(?i)" + strRegex);
			}
		});
		
		for(int i = 0; i < fInput.length; i++)
		{
			alFile.add(new File(fInput[i].getPath()));
		}
		Collections.sort(alFile);
		
		return alFile;
	}
	
	public static ArrayList<File> GetFilesByExtension(File dirInput, final String strExtension) throws IOException
	{
		ArrayList<File> alFile = new ArrayList<File>();
		
		File[] fInput = dirInput.listFiles(new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				return name.toLowerCase().endsWith(strExtension.toLowerCase());
			}
		});
		
		for(int i = 0; i < fInput.length; i++)
		{
			alFile.add(new File(fInput[i].getPath()));
		}
		Collections.sort(alFile);
		
		return alFile;
	}
	
	public static File GetFileWithSuffix(File fInput, final String strSuffix) throws IOException
	{	
		String strFilename = fInput.getName();
		strFilename = strFilename.substring(0, strFilename.lastIndexOf(".")) + strSuffix + strFilename.substring(strFilename.lastIndexOf("."));
		
		File fOutput = new File(fInput.getParent() + File.separator + strFilename);
		return fOutput;
	}
	
	public static String GetPartialFilename(File fInput, String strRemove) throws Exception
	{
		return fInput.getName().substring(0, fInput.getName().lastIndexOf(strRemove));
	}
	
	public static String GetPartialFilename(String strFilename, String strRemove) throws Exception
	{
		return strFilename.substring(0, strFilename.lastIndexOf(strRemove));
	}
	
	public static int GetTokenLength(String strInput)
	{
		ArrayList<String> alTemp = SentenceToToken(strInput);
		return alTemp.size();
	}
	
	public static ArrayList<String> GetLabeledEntity(String strInput, boolean bTagged)
	{
		ArrayList<String> alEntity = new ArrayList<String>();
		int iIndex_Start = 0;
		int iIndex_End = 0;
		
		while(iIndex_Start != -1 && iIndex_End != -1)
		{
			if(iIndex_Start == 0 && iIndex_End == 0)
			{
				iIndex_Start = strInput.indexOf(Setting.strEntity_Start, 0);
				iIndex_End = strInput.indexOf(Setting.strEntity_End, 0);
			}
			else
			{
				iIndex_Start = strInput.indexOf(Setting.strEntity_Start, iIndex_Start + 1);
				iIndex_End = strInput.indexOf(Setting.strEntity_End, iIndex_End + 1);
			}
			
			if(iIndex_Start != -1 && iIndex_End != -1)
			{
				if(bTagged)
				{
					alEntity.add(strInput.substring(iIndex_Start, iIndex_End + Setting.strEntity_End.length()));	
				}
				else
				{
					alEntity.add(strInput.substring(iIndex_Start, iIndex_End).replace(Setting.strEntity_Start, "").replace(Setting.strEntity_End, ""));
				}
			}
		}
		
		return alEntity;
	}
	
	public static String GetProgress(int iNumerator, int iDenominator) throws Exception
	{
		DecimalFormat df = new DecimalFormat("#0.00");
		float fProgress = (float) (iNumerator + 1) * 100 / iDenominator;
		
		return "Progress = " + (iNumerator + 1) + "/" + String.valueOf(iDenominator)
				+ " (" + df.format(fProgress) + "%) done!";
	}
		
	public static void SaveFile(String strInput, File fFileName, boolean bAppend) throws IOException
	{
		SaveFile(strInput, fFileName, "", bAppend);
	}
	
	public static void SaveFile(String strInput, File fFileName, String strComment, boolean bAppend) throws IOException
	{
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fFileName, bAppend), "UTF-8"));
		
		if(strComment.length() > 0)
		{
			bw.write("%% " + strComment + Setting.NewLine);
		}
		
		bw.write(strInput + Setting.NewLine);
		bw.flush();
		bw.close();
	}
	
	public static void SaveFile(ArrayList<String> alContent, File fFileName, boolean bAppend) throws IOException
	{
		SaveFile(alContent, fFileName, "", bAppend);
	}
	
	public static void SaveFile(ArrayList<String> alContent, File fFileName, String strComment, boolean bAppend) throws IOException
	{
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fFileName, bAppend), "UTF-8"));
		
		if(strComment.length() > 0)
		{
			bw.write("%% " + strComment + Setting.NewLine);
		}
		
		if(alContent.size() > 0)
		{
			for(String item:alContent)
			{
				try
				{
					bw.write(item.toString() + Setting.NewLine);
				}
				catch (Exception e)
				{
					
				}
				
			}
		}
		
		bw.flush();
		bw.close();
	}
	
	public static void SaveFile(StrIntPair pair, File fFileName, String strComment, String strType, boolean bAppend) throws IOException
	{
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fFileName, bAppend), "UTF-8"));
		
		if(strComment.length() > 0)
		{
			bw.write("%% " + strComment + Setting.NewLine);
		}
		
		if(pair.size() > 0)
		{
			for(StrIntPair p : pair)
			{
				switch(strType.toLowerCase())
				{
					default:
					case "all":
						bw.write(p.getStrIntPair() + Setting.NewLine);
						break;
						
					case "str":
						bw.write(p.getString() + Setting.NewLine);
						break;
				}
			}
		}
		
		bw.flush();
		bw.close();
	}
	
	public static void SaveFile(StrFloatPair pair, File fFileName, String strComment, String strType, boolean bAppend) throws IOException
	{
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fFileName, bAppend), "UTF-8"));
		
		if(strComment.length() > 0)
		{
			bw.write("%% " + strComment + Setting.NewLine);
		}
		
		if(pair.size() > 0)
		{
			for(StrFloatPair p : pair)
			{
				switch(strType.toLowerCase())
				{
					default:
					case "all":
						bw.write(p.getStrFloatPair() + Setting.NewLine);
						break;
						
					case "str":
						bw.write(p.getString() + Setting.NewLine);
						break;
				}
			}
		}
		
		bw.flush();
		bw.close();
	}
	
	public static void SaveFile(HMCS hmcs, File fFileName, String strMethod, boolean bHead, boolean bAppend) throws IOException
	{
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fFileName, bAppend), "UTF-8"));
		
		if(bHead)
		{
			bw.write("%% (" + strMethod + ") Term	Occurrence	Total	Confidence	Support	HMCS" + "\t" + hmcs.getParameters() + Setting.NewLine);
		}
		
		if(hmcs.size() > 0)
		{
			for(HMCS h:hmcs)
			{
				bw.write(h.outputTerm() + Setting.NewLine);
			}
		}
		
		bw.flush();
		bw.close();
	}
	
	public static File SubFileOrFolder(File fParentFolder, String strSubFolder) throws IOException
	{
		File fSubFolder = new File(fParentFolder.getCanonicalPath() + File.separator + strSubFolder);
		return fSubFolder;
	}
	
	public static void MakeWorkFolder() throws Exception
	{
		if(!Setting.dirWorkFolder.exists())
		{
			Setting.dirWorkFolder.mkdir();
			Setting.dirDictionary.mkdir();
			Setting.dirTraining.mkdir();
			Setting.dirTesting.mkdir();
			
			FileUtils.copyFileToDirectory(new File("config.ini"), Setting.dirWorkFolder);
			FileUtils.copyDirectory(new File("dictionary"), Lib.SubFileOrFolder(Setting.dirWorkFolder, "Dictionary"));
		}
	}
	
	public static ArrayList<String> SymbolReplace(String strID, ArrayList<String> alInput) throws Exception
	{
		ArrayList<String> alOutput = new ArrayList<String>();
		ArrayList<String> alSymbols = Lib.LoadArrayList(Lib.SubFileOrFolder(Setting.dirDictionary, "Symbols.txt"), "UTF-8", true);
		
		String strSentence = "";
		char[] chHalf = alSymbols.get(0).toCharArray();
		char[] chFull = alSymbols.get(1).toCharArray();
		
		for(int i = 0; i < alInput.size(); i++)
		{
			// half-shaped/full-shape space → "﹍"
			// if a space (or spaces) between two non-English/non-numeric characters → remove space
			// replace half symbol → full symbol, except "." and "‧"
			strSentence = alInput.get(i).replaceAll("[ 　]+", "﹍").trim();
			
			for(int j = 0 ; j < chHalf.length ; j++)
			{
				strSentence = strSentence.replace(chHalf[j], chFull[j]);
			}
			
			alOutput.add(strSentence);
			
			if(i > 0 && (i % 100000 == 0 || i == alInput.size() - 1))
			{
				if(strID.length() > 0)
				{
					Setting.MyLog.info("Thread " + strID + ": " + Lib.GetProgress(i, alInput.size()));
				}
				else
				{
					Setting.MyLog.info(Lib.GetProgress(i, alInput.size()));
				}
			}
		}
		
		return alOutput;
	}
	
	public static ArrayList<String> FilterNegExamples(ArrayList<String> alInput) throws Exception
	{
		ArrayList<String> alOutput = new ArrayList<String>();
		
		for(int i = 0; i < alInput.size(); i++)
		{
			if(alInput.get(i).contains(Setting.strEntity_Start) && alInput.get(i).contains(Setting.strEntity_End))
			{
				alOutput.add(alInput.get(i));
			}
		}
		
		return alOutput;
	}
	
	public static ArrayList<String> SentenceToToken(String strInput)
	{
		ArrayList<String> alOutput = new ArrayList<String>();
		
		char[] ch = strInput.toCharArray();
		String strCurr = "";
		String strToken = "";
		
		for(char c:ch)
		{
			strCurr = String.valueOf(c);
			
			if(CheckCharLanguage(c, "EN") || CheckCharLanguage(c, "Number") || CheckCharLanguage(c, "Other"))
			{
				if(strToken.length() == 0)
				{
					strToken = strCurr;
				}
				else
				{
					if(Setting.patternEngNum.matcher(strToken).matches())
					{
						if(Setting.patternEngNum.matcher(strToken + strCurr).matches())
						{
							strToken += strCurr;
						}
						else
						{
							alOutput.add(strToken);
							strToken = strCurr;
						}
					}
					else if(Setting.patternSymbol.matcher(strToken).matches())
					{
						// duplicate symbols: the last char of strToken == strCurr
						if(strToken.substring(strToken.length() - 1).equals(strCurr))
						{
							strToken += strCurr;
						}
						else // strToken and strCurr are different Symbols
						{
							alOutput.add(strToken);
							strToken = strCurr;
						}
					}
					else
					{
						alOutput.add(strToken);
						strToken = strCurr;
					}
				}
				
				// for Entity start/end tags
				if(strToken.toUpperCase().startsWith(Setting.strEntity_Start))
				{
					alOutput.add(Setting.strEntity_Start);
					strToken = strToken.substring(Setting.strEntity_Start.length());
				}
				else if(strToken.toUpperCase().startsWith(Setting.strEntity_End))
				{
					alOutput.add(Setting.strEntity_End);
					strToken = strToken.substring(Setting.strEntity_End.length());
				}
				 
				if(strToken.toUpperCase().endsWith(Setting.strEntity_Start))
				{
					alOutput.add(strToken.substring(0, strToken.length() - Setting.strEntity_Start.length()));
					alOutput.add(Setting.strEntity_Start);
					strToken = "";
				}
				else if(strToken.toUpperCase().endsWith(Setting.strEntity_End))
				{
					alOutput.add(strToken.substring(0, strToken.length() - Setting.strEntity_End.length()));
					alOutput.add(Setting.strEntity_End);
					strToken = "";
				}
			}
			else
			{
				if(strToken.length() > 0) // normal token
				{
					alOutput.add(strToken);
					strToken = "";
				}
				alOutput.add(strCurr);
			}
		}
		
		if(strToken.length() > 0)
		{
			alOutput.add(strToken);
		}
		
		// Post check for special cases
		String strPrev = "";
		String strNext = ""; 
		
		for(int i = 0 ; i < alOutput.size() ; i++)
		{
			strCurr = alOutput.get(i);
			
			if(i - 1 >= 0 && !alOutput.get(i - 1).toUpperCase().equals(Setting.strEntity_Start) && !alOutput.get(i - 1).toUpperCase().equals(Setting.strEntity_End))
			{
				strPrev = alOutput.get(i - 1);
			}
			else
			{
				strPrev = "";
			}
			
			if(i + 1 < alOutput.size() && !alOutput.get(i + 1).toUpperCase().equals(Setting.strEntity_Start) && !alOutput.get(i + 1).toUpperCase().equals(Setting.strEntity_End))
			{
				strNext = alOutput.get(i + 1);
			}
			else
			{
				strNext = "";
			}
			
			if(strCurr.equals("'") && strNext.toLowerCase().equals("s")) // e.g.: Mary's Book
			{	
				
				alOutput.set(i, strCurr + strNext);
				alOutput.set(i + 1, "");
			}
			else if(strCurr.equals(".") || strCurr.equals("．") || strCurr.equals("－") || strCurr.equals("＆")) // e.g.: F-15 or B&G
			{
				if(Setting.patternEngNum.matcher(strPrev).matches() && Setting.patternEngNum.matcher(strNext).matches())
				{
					alOutput.set(i, strPrev + strCurr + strNext);
					alOutput.set(i - 1, "");
					alOutput.set(i + 1, "");
				}
			}
			else if(strCurr.equals("＠")) // email
			{	
				if(Setting.patternEMail.matcher(strPrev + strCurr.replace("＠", "@") + strNext).matches())
				{
					alOutput.set(i, strPrev + strCurr + strNext);
					alOutput.set(i - 1, "");
					alOutput.set(i + 1, "");
				}
			}
			else if(strCurr.equals("：")) // url
			{
				String strTemp = strPrev + strCurr.replace("：", ":") + strNext;
				if(Setting.patternUrl.matcher(strTemp).matches())
				{
					String strUrl = strPrev + strCurr + strNext;
					alOutput.set(i - 1, "");
					alOutput.set(i + 1, "");
					
					for(int j = i + 2; j < alOutput.size(); j++)
					{
						strTemp += alOutput.get(j).replace("＆", "&").replace("＿", "_").replace("＝", "=").replace("？", "?");
						
						if(alOutput.get(j).replace("？", "?").equals("?") || Setting.patternUrl.matcher(strTemp).matches())
						{
							strUrl += alOutput.get(j);
							alOutput.set(j, "");
						}
						else
						{
							break;
						}
					}
					
					alOutput.set(i, strUrl);
				}
			}
		}
		
		for(int i = alOutput.size() - 1 ; i >= 0 ; i--)
		{
			if(alOutput.get(i).length() == 0)
			{
				alOutput.remove(i);
			}
		}
		
//		for(int i = 0 ; i < alOutput.size() ; i++)
//		{
//			if(bDuplicateSymbols)
//			{
//				alOutput.set(i, DuplicateSymbols(alOutput.get(i)));
//			}
//			
//			if(bUpperCase)
//			{
//				alOutput.set(i, alOutput.get(i).toUpperCase());
//			}
//		}
		
		return alOutput;
	}
	
	public static ArrayList<String> SentenceToToken_OLD(String strInput)
	{
		ArrayList<String> alOutput = new ArrayList<String>();
		char[] chInput = strInput.toCharArray();
		String strChar = "";
		String strToken = "";			
		int iIndex = 0;
		
		while(iIndex < chInput.length)
		{
			strChar = String.valueOf(chInput[iIndex]);
			strToken += strChar;
			
			if(IsToken(strToken))
			{
				if(strToken.startsWith(Setting.strEntity_Start)) // e.g.: <NE>Token
				{
					alOutput.add(Setting.strEntity_Start);
					if(strToken.replace(Setting.strEntity_Start, "").length() > 0)
					{
						alOutput.add(strToken.replace(Setting.strEntity_Start, ""));
					}
					strToken = "";
				}
				else if(strToken.startsWith(Setting.strEntity_End)) // e.g.: </NE>Token
				{
					alOutput.add(Setting.strEntity_End);
					if(strToken.replace(Setting.strEntity_End, "").length() > 0)
					{
						alOutput.add(strToken.replace(Setting.strEntity_End, ""));
					}
					strToken = "";
				}
				else if(strToken.endsWith(Setting.strEntity_Start)) // e.g.: Token<NE>
				{
					if(strToken.replace(Setting.strEntity_Start, "").length() > 0)
					{
						alOutput.add(strToken.replace(Setting.strEntity_Start, ""));
					}
					alOutput.add(Setting.strEntity_Start);
					strToken = "";
				}
				if(strToken.endsWith(Setting.strEntity_End)) // e.g.: Token</NE>
				{	
					if(strToken.replace(Setting.strEntity_End, "").length() > 0)
					{
						alOutput.add(strToken.replace(Setting.strEntity_End, ""));
					}
					alOutput.add(Setting.strEntity_End);
					strToken = "";
				}
				if(strToken.endsWith(Setting.strSeparator)) // e.g.: Token</Split>
				{	
					if(strToken.replace(Setting.strSeparator, "").length() > 0)
					{
						alOutput.add(strToken.replace(Setting.strSeparator, ""));
					}
					alOutput.add(Setting.strSeparator);
					strToken = "";
				}
				else if(iIndex == chInput.length - 1) // strToken is a html tag or strChar is the last char
				{
					alOutput.add(strToken);
					strToken = "";
				}
			}
			else
			{
				if(strToken.length() == 1)
				{
					alOutput.add(strToken);
				}
				else // [Token] + strChar, strChar is not a token
				{
					alOutput.add(strToken.substring(0, strToken.length() - 1));
					alOutput.add(strChar);
				}					
				strToken = "";
			}
			
			iIndex++;
		}
		
		for(int i = alOutput.size() - 1; i >= 0; i--)
		{
			if(alOutput.get(i).length() == 0)
			{
				alOutput.remove(i);
			}	
		}
		
		return alOutput;
	}
	
//	public static ArrayList<String> Sentence2Tokens(String strInput)
//	{
//		ArrayList<String> alTokens = new ArrayList<String>();
//		
//		return alTokens;
//	}
	
	public static boolean IsToken(String strInput)
	{
		Matcher matcherEngNum = Setting.patternEngNum.matcher(strInput);
		Matcher matcherSymbol = Setting.patternSymbol.matcher(strInput);
		
		if(matcherEngNum.matches())
		{
			return true;
		}
		else if(matcherSymbol.matches())
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public static String Tokens2Sentence(ArrayList<String> alTokens)
	{
		String strSentence = "";
		
		for(String s:alTokens)
		{
			strSentence += s + Setting.strSeparator_Sentence;
		}
		
		return strSentence.trim();
	}
	
	public static String CRF_Training(File fTrainingData, File fModel) throws Exception
	{
		String strCmd = Setting.strCRFTraining;
		strCmd = strCmd.replace("[TrainingData]", fTrainingData.getCanonicalPath());
		strCmd = strCmd.replace("[Model]", fModel.getCanonicalPath());
		
		return strCmd;
	}
	
	public static String CRF_Testing(File fModel, File fTestingData, File fTestingResult, int iTopNprobability) throws Exception
	{
		String strCmd = Setting.strCRFTesting;
		strCmd = strCmd.replace("[Model]", fModel.getCanonicalPath());
		strCmd = strCmd.replace("[TestingData]", fTestingData.getCanonicalPath());
		strCmd = strCmd.replace("[TestingResult]", fTestingResult.getCanonicalPath());
		
		if(iTopNprobability == 0)
		{
			strCmd = strCmd.replace("[-n num] ", ""); // Testing and Output top 1 Probability
		}
		else
		{
			// Testing and Output top n Probability
			strCmd = strCmd.replace("[-n num]", "-n " + String.valueOf(iTopNprobability));
		}
		
		return strCmd;
	}
	
	public static boolean ExecCommand(String strCmd)
	{
		boolean bSuccess = true;
		
		try
		{	
			Setting.MyLog.info("Exec. CMD: " + strCmd);
			
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec(strCmd);
			BufferedReader br = new BufferedReader (new InputStreamReader(proc.getInputStream()));
			String strOutput = "";
			
			while((strOutput = br.readLine()) != null)
			{
				Setting.MyLog.info("CMD Output: " + strOutput);
				
				if(strOutput.toUpperCase().contains("[ERROR]") || strOutput.toUpperCase().contains("[WARN]"))
				{
					bSuccess = false;
				}
			}
			
			if(proc.waitFor() != 0)
			{
				bSuccess = false;
			}
		}
		catch(Exception e)
		{	
			Setting.MyLog.warn(e.getMessage());
			bSuccess = false;
		}
		
		Setting.MyLog.info("Exec. Success? " + bSuccess);
		Setting.MyLog.info("--------------------");
		return bSuccess;
	}
	
	public static boolean IsSentenceContainSeed_X(String strSentence, String strSeed, String strType)
	{
		boolean bResult = false;
		
		strSentence = strSentence.replaceAll(Setting.strEntity_Start + "[0-9]{1,2}" + Setting.strEntity_End, "");
		
		if(strSentence.contains(strSeed))
		{
			
			if(strType.toLowerCase().equals("none"))
			{
				
			}
			else if(strType.toLowerCase().equals("guillemet"))
			{
				
			}
		}
		
		// remove labeled entity (entities)
		strSentence = strSentence.replaceAll(Setting.strEntity_Start + "[0-9]{1,2}" + Setting.strEntity_End, "");
		int iSize_1 = Lib.SentenceToToken(strSentence).size();
		
		if(strSentence.contains(strSeed))
		{
			strSentence = strSentence.replace(strSeed, Setting.strEntity_Start + strSeed + Setting.strEntity_End);
			int iSize_2 = Lib.SentenceToToken(strSentence).size();
			
			if((iSize_2 - iSize_1) == 2)
			{
				bResult = true;
			}
		}
		
		return bResult;
	}
	
	public static boolean IsNoise(String strInput)
	{
		StrIntPair pair = CheckLanguage(strInput);
		int iCount = 0;
		
		for(StrIntPair p:pair)
		{
			if(p.getString().toLowerCase().equals("number") || p.getString().toLowerCase().equals("other"))
			{
				iCount += p.getInt();
			}
		}
		
		float fPercentage = (float) iCount / strInput.length();
				
		if(fPercentage >= Setting.fNoneCJK_Threshold)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public static String GetLanguageMajor(String strInput)
	{	
		return CheckLanguage(strInput).get(0).getString();
	}
	
	public static StrIntPair CheckLanguage(String strInput)
	{	
		StrIntPair pair = new StrIntPair();
				
		char[] ch = strInput.toCharArray();
		for(int i = 0; i < ch.length; i++)
		{
			if(CheckCharLanguage(ch[i], "English"))
			{
				pair.addString("English");
			}
			else if(CheckCharLanguage(ch[i], "Chinese"))
			{
				pair.addString("Chinese");
			}
			else if(CheckCharLanguage(ch[i], "Japanese"))
			{
				pair.addString("Japanese");
			}
			else if(CheckCharLanguage(ch[i], "Korean"))
			{
				pair.addString("Korean");
			}
			else if(CheckCharLanguage(ch[i], "Number"))
			{
				pair.addString("Number");
			}
			else
			{
				pair.addString("Other");
			}
		}
		
		pair.sortByInt(StrIntPair.SortBy.DESC);
		
		return pair;
	}
	
	public static boolean CheckCharLanguage(char c, String strType)
	{
		switch(strType.toLowerCase())
		{
			case "english":
			case "en":
				return Setting.patternEnglish.matcher(String.valueOf(c)).find();
				
			case "chinese":
			case "zh":
				return Setting.patternChinese.matcher(String.valueOf(c)).find();
				
			case "japanese":
			case "jp":
			case "ja":
				return Setting.patternJapanese.matcher(String.valueOf(c)).find();
				
			case "korean":
			case "ko":
			case "kr":
				return Setting.patternKorean.matcher(String.valueOf(c)).find();
			
			case "number":
				return Setting.patternNumber.matcher(String.valueOf(c)).find();
				
			default:
			case "other":
				if(!Setting.patternEnglish.matcher(String.valueOf(c)).find()
						&& !Setting.patternChinese.matcher(String.valueOf(c)).find()
						&& !Setting.patternJapanese.matcher(String.valueOf(c)).find()
						&& !Setting.patternKorean.matcher(String.valueOf(c)).find()
						&& !Setting.patternNumber.matcher(String.valueOf(c)).find())
				{
					return true;
				}
				else
				{
					return false;
				}
		}
	}
	
	public static String DuplicateSymbols(String strInput)
	{
		if(strInput.length() > 1 && Setting.patternSymbol.matcher(strInput).matches())
		{	
			if(strInput.length() == StringUtils.countMatches(strInput, strInput.substring(0, 1)))
			{
				return strInput.substring(0, 1).toUpperCase();
			}
			else
			{
				return strInput.toUpperCase();
			}
		}
		else
		{
			return strInput.toUpperCase();
		}
	}
	
	public static int GetRandom(int iMin, int iMax)
	{
		int iRandom = (int) (Math.random() * (iMax - iMin + 1)) + iMin;
		return iRandom;
	}
}
