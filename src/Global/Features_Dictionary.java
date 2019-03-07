package Global;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.*;
import org.apache.commons.lang3.StringUtils;

public class Features_Dictionary
{
	public HashSet<String> hsCommonBefore = new HashSet<String>();
	public HashSet<String> hsCommonAfter = new HashSet<String>();
	public HashSet<String> hsEntityPrefix = new HashSet<String>();
	public HashSet<String> hsEntitySuffix = new HashSet<String>();
	private HashSet<String> hsSymbols = new HashSet<String>();
	static ArrayList<String> alInput = new ArrayList<String>();
//	static ArrayList<String> alOutput = new ArrayList<String>();
	static ArrayList<String>[] alOutput;
	static int iThread = 1;
	
	public enum Dictionary
	{
		CommonBefore, CommonAfter, EntityPrefix, EntitySuffix, EngNum, Symbol
	}
	
	public Features_Dictionary(File dirWorkFolder) throws IOException
	{	
		ArrayList<String> alCommonBefore = removeExtraInfo(Lib.LoadArrayList(Lib.SubFileOrFolder(Setting.dirDictionary, "CommonBefore.txt"), "UTF-8",true));		
		ArrayList<String> alCommonAfter = removeExtraInfo(Lib.LoadArrayList(Lib.SubFileOrFolder(Setting.dirDictionary, "CommonAfter.txt"), "UTF-8", true));
		ArrayList<String> alEntityPrefix = removeExtraInfo(Lib.LoadArrayList(Lib.SubFileOrFolder(Setting.dirDictionary, "EntityPrefix.txt"), "UTF-8", true));
		ArrayList<String> alEntitySuffix = removeExtraInfo(Lib.LoadArrayList(Lib.SubFileOrFolder(Setting.dirDictionary, "EntitySuffix.txt"), "UTF-8", true));
		ArrayList<String> alSymbols = Lib.LoadArrayList(Lib.SubFileOrFolder(Setting.dirDictionary, "Symbols.txt"), "UTF-8", true);
		
		hsCommonBefore = new HashSet<String>(alCommonBefore);
		hsCommonAfter = new HashSet<String>(alCommonAfter);
		hsEntityPrefix = new HashSet<String>(alEntityPrefix);
		hsEntitySuffix = new HashSet<String>(alEntitySuffix);
		
		char[] chSymbols = (alSymbols.get(1) + alSymbols.get(2)).toCharArray();
		for(char c:chSymbols)
		{
			hsSymbols.add(String.valueOf(c));
		}
		
//		alCustomFeature = Lib.LoadFileContent(Lib.SubFileOrFolder(Setting.dirDictionary, "CustomFeature.txt"), "UTF-8",true);
	}
	
	public String getFeature_CommonBefore(String strInput)
	{
		if(strInput.length() > 0 && hsCommonBefore.contains(strInput.toUpperCase()))
		{
			return "1";
		}
		else
		{
			return "0";
		}
	}
	
	public String getFeature_CommonAfter(String strInput)
	{
		if(strInput.length() > 0 && hsCommonAfter.contains(strInput.toUpperCase()))
		{
			return "1";
		}
		else
		{
			return "0";
		}
	}
	
	public String getFeature_EntityPrefix(String strInput)
	{
		if(strInput.length() > 0 && hsEntityPrefix.contains(strInput.toUpperCase()))
		{
			return "1";
		}
		else
		{
			return "0";
		}
	}
	
	public String getFeature_EntitySuffix(String strInput)
	{
		if(strInput.length() > 0 && hsEntitySuffix.contains(strInput.toUpperCase()))
		{
			return "1";
		}
		else
		{
			return "0";
		}
	}
	
	public String getFeature_EngNum(String strInput)
	{
		if(strInput.length() > 0 && Setting.patternEngNum.matcher(strInput.toUpperCase()).matches())
		{
			return "1";
		}
		else
		{
			return "0";
		}
	}
		
	public String getFeature_Symbol(String strInput)
	{		
		if(strInput.length() == 0)
		{
			return "0";
		}
		else if(strInput.length() == 1)
		{
			if(hsSymbols.contains(strInput.toUpperCase()))
			{
				return "1";
			}
			else
			{
				return "0";
			}
		}
		else
		{	
			if(strInput.length() == StringUtils.countMatches(strInput.toUpperCase(), strInput.toUpperCase().substring(0, 1)))
			{
				if(hsSymbols.contains(strInput.toUpperCase().substring(0, 1)))
				{
					return "1";
				}
				else
				{
					return "0";
				}
			}
			else
			{
				return "0";
			}
		}
	}
	
	public String getFeature_Space(String strInput)
	{
		if(strInput.equals("﹍"))
		{
			return "1";
		}
		else
		{
			return "0";
		}
	}
	
//	public String getFeature_CustomFeature(String strInput)
//	{
//		if(strInput.length() > 0 && hsCustomFeature.contains(strInput))
//		{
//			return "1";
//		}
//		else
//		{
//			return "0";
//		}
//	}
	
	private static ArrayList<String> removeExtraInfo(ArrayList<String> alInput)
	{
		ArrayList<String> alOutput = new ArrayList<String>();
		
		for(String s:alInput)
		{
			alOutput.add(s.split(Setting.strSeparator_Tab)[0]);
		}
		
		return alOutput;
	}
	
	public ArrayList<String> Extraction(ArrayList<String> alInput, boolean bLabel) throws Exception
	{
		this.alInput = (ArrayList<String>) alInput.clone();
		alOutput = new ArrayList[iThread];
		
		ExecutorService es = Executors.newFixedThreadPool(iThread);
		
		for(int i = 0; i < iThread; i++)
		{
			alOutput[i] = new ArrayList<String>();
			es.execute(new FeaturesWorker(i, bLabel));
		}
		
		Thread.sleep(3000);
		es.shutdown();
		
		while(true)
		{
			Thread.sleep(1000);
			if(Thread.activeCount() == 1) // all threads destroyed
			{
				for(int i = 1; i < iThread; i++)
				{
					alOutput[0].addAll(alOutput[i]);
				}
				
				break;
			}
		}
		
		// remove last blank line
		if(alOutput[0].size() > 1)
		{
			alOutput[0].remove(alOutput[0].size() - 1);
		}
		return alOutput[0];
	}
	
	public class FeaturesWorker implements Runnable
	{
		private String strID;
		private int iID;
		private int iStart;
		private int iEnd;
		private boolean bLabel;
		
		public FeaturesWorker(int id, boolean bLabel)
		{
			this.strID = String.valueOf(id);
			this.iID = id;
			this.iStart = id * alInput.size() / iThread;
			this.iEnd = (id + 1) * alInput.size() / iThread - 1;
			this.bLabel = bLabel;
			
			if(id == iThread - 1)
			{
				this.iEnd = alInput.size() - 1;
			}
		}
		
		public void run()
		{
			Setting.MyLog.info("Thread " + strID + " begins work (start index = " + iStart + "\t, end index = " + iEnd + ")");
			
			try
			{
				ArrayList<String> alToken;
				String strFeature = "";
				String strToken_1 = "";
				String strToken_2 = "";
				String strToken_3 = "";
				String strToken_Prev = "";
				String strToken_Next = "";
				String strLabel = "";
				boolean bEntity = false;
				int iIndex = 0;
				
				for(int i = iStart; i <= iEnd; i++)
				{
					alToken = Lib.SentenceToToken(alInput.get(i));
					
					// Feature Extraction
					for(int j = 0; j < alToken.size(); j++)
					{
						// Prev Token and Next Token
						if(j - 1 >= 0)
						{
							strToken_Prev = alToken.get(j - 1);
						}
						
						if(j + 1 < alToken.size())
						{	
							strToken_Next = alToken.get(j + 1);
						}
						
						// ------
						
						strToken_1 = duplicateSymbols(alToken.get(j));
						
						if(strToken_1.equals(Setting.strEntity_Start))
						{
							bEntity = true;
							
						}
						else if(strToken_1.equals(Setting.strEntity_End))
						{
							bEntity = false;
						}
						else
						{
							// Token 2
							iIndex = j + 1;
							while(iIndex < alToken.size() && strToken_2.length() == 0 && strToken_3.length() == 0)
							{
								if(strToken_2.length() == 0 && !alToken.get(iIndex).equals(Setting.strEntity_Start) && !alToken.get(iIndex).equals(Setting.strEntity_End))
								{
									strToken_2 = strToken_1 + duplicateSymbols(alToken.get(iIndex));
								}
								
								iIndex ++;
							}
							
							// Token 3
							while(iIndex < alToken.size() && strToken_3.length() == 0)
							{
								if(!alToken.get(iIndex).equals(Setting.strEntity_Start) && !alToken.get(iIndex).equals(Setting.strEntity_End))
								{
									strToken_3 = strToken_2 + duplicateSymbols(alToken.get(iIndex));
								}
								
								iIndex ++;
							}
							
							strFeature = alToken.get(j)
								+ Setting.strCRFTraining_Separator + getFeature_CommonBefore(strToken_1)	// CommonBefore_1
								+ Setting.strCRFTraining_Separator + getFeature_CommonBefore(strToken_2)	// CommonBefore_2
								+ Setting.strCRFTraining_Separator + getFeature_CommonBefore(strToken_3)	// CommonBefore_3
								+ Setting.strCRFTraining_Separator + getFeature_EntityPrefix(strToken_1)	// EntityPrefix_1
								+ Setting.strCRFTraining_Separator + getFeature_EntityPrefix(strToken_2)	// EntityPrefix_2
								+ Setting.strCRFTraining_Separator + getFeature_EntityPrefix(strToken_3)	// EntityPrefix_3
								+ Setting.strCRFTraining_Separator + getFeature_EntitySuffix(strToken_1)	// EntitySuffix_1
								+ Setting.strCRFTraining_Separator + getFeature_EntitySuffix(strToken_2)	// EntitySuffix_2
								+ Setting.strCRFTraining_Separator + getFeature_EntitySuffix(strToken_3)	// EntitySuffix_3
								+ Setting.strCRFTraining_Separator + getFeature_CommonAfter(strToken_1)		// CommonAfter_1
								+ Setting.strCRFTraining_Separator + getFeature_CommonAfter(strToken_2)		// CommonAfter_2
								+ Setting.strCRFTraining_Separator + getFeature_CommonAfter(strToken_3)		// CommonAfter_3
								+ Setting.strCRFTraining_Separator + getFeature_EngNum(strToken_1)			// English + Number
								+ Setting.strCRFTraining_Separator + getFeature_Symbol(strToken_1);			// Symbol
//								+ Setting.strCRFTraining_Separator + getFeature_Space(strToken_1);			// Space

							
							if(this.bLabel)
							{
								if(strToken_Prev.equals(Setting.strEntity_Start) && strToken_Next.equals(Setting.strEntity_End))
								{
									strLabel = "S";
									bEntity = false;
								}
								else if(strToken_Prev.equals(Setting.strEntity_Start)) 
								{
									strLabel = "B";
								}
								else if(strToken_Next.equals(Setting.strEntity_End)) 
								{
									strLabel = "E";
									bEntity = false;
								}
								else if(bEntity) 
								{
									strLabel = "I";
								}
								else
								{
									strLabel = "O";
								}
								
								strFeature += Setting.strCRFTraining_Separator + strLabel;
							}
							alOutput[iID].add(strFeature);
						}
						
						// ----------
						strFeature = "";
						strToken_1 = "";
						strToken_2 = "";
						strToken_3 = "";
						strToken_Prev = "";
						strToken_Next = "";
						strLabel = "";
					}
					
					alOutput[iID].add("");
					
					if(i > 0 && (i % 100000 == 0 || i == alInput.size() - 1))
					{
						Setting.MyLog.info("Thread " + this.strID + ": " + Lib.GetProgress(i, alInput.size()));
					}
				}
			}
			catch(Exception ex)
			{
				Setting.MyLog.warn("Thread " + strID + " interrupted");
				ex.printStackTrace();
			}
			
			Setting.MyLog.info("Thread " + strID + " end work");
		}
		
		private String duplicateSymbols(String strInput)
		{
			if(strInput.length() > 1 && Setting.patternSymbol.matcher(strInput).matches())
			{	
				if(strInput.length() == StringUtils.countMatches(strInput, strInput.substring(0, 1)))
				{
					return strInput.substring(0, 1);
				}
				else
				{
					return strInput;
				}
			}
			else
			{
				return strInput;
			}
		}
	}
}