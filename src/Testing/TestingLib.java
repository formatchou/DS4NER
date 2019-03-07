package Testing;
import Global.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestingLib
{	
	public static void Compare_Exact(File fTestingResult, File fDetail, File fSummary, String strName) throws Exception
	{
		DecimalFormat df4 = new DecimalFormat("#0.0000");
		
		ArrayList<String> alTestingResult = Lib.LoadArrayList(fTestingResult, "UTF-8", false);
		ArrayList<String> alDetail = new ArrayList<String>();
		ArrayList<String> alSummary = new ArrayList<String>();
		String strToken = "";
		String strAns = "";
		String strTR = "";
		
		int iCorrection = 0;
		int iExtraction = 0;
		int iEntity = 0;
		int iIndexAns_Start = -1;
		int iIndexAns_End = -1;
		int iIndexTR_Start = -1;
		int iIndexTR_End = -1;
		
		for(int i = 0 ; i < alTestingResult.size() ; i++)
		{
			String strRow = alTestingResult.get(i);
			
			if(strRow.length() > 0)
			{	
				try
				{
					strToken = strRow.split(Setting.strCRFTesting_Separator)[0];
					strAns = strRow.split(Setting.strCRFTesting_Separator)[strRow.split(Setting.strCRFTesting_Separator).length - 2];
					strTR = strRow.split(Setting.strCRFTesting_Separator)[strRow.split(Setting.strCRFTesting_Separator).length - 1];
				}
				catch (Exception e)
				{
					Setting.MyLog.debug("Something wrong at line: " + (i + 1 ) + "\t" + alTestingResult.get(i));
				}
				
				strRow = strToken + Setting.strSeparator_Tab + strAns + Setting.strSeparator_Tab + strTR;
				
				// Answer
				if(strAns.equals("B"))
				{
					iIndexAns_Start = i;
				}
				else if(strAns.equals("E"))
				{
					iIndexAns_End = i;
					iEntity++;
				}
				else if(strAns.equals("S"))
				{
					iEntity++;
				}
				
				// TR
				if(strTR.equals("B"))
				{
					iIndexTR_Start = i;
				}
				else if(strTR.equals("E"))
				{
					iIndexTR_End = i;
					iExtraction++;
				}
				else if(strTR.equals("S"))
				{
					iExtraction++;
				}
				
				// Add Compare Results
				if(strAns.equals(strTR))
				{
					if(strAns.equals("E")) // Ans = TR = E
					{
						if(iIndexAns_Start == iIndexTR_Start) // Start and End Index Both Equal
						{
							iCorrection++;
							strRow = strRow + Setting.strSeparator_Tab + "TP";
						}
						else // Start not Equal but End Index Equal
						{
							strRow = strRow + Setting.strSeparator_Tab + "Fail";
						}
						
						iIndexAns_Start = -1;
						iIndexAns_End = -1;
						iIndexTR_Start = -1;
						iIndexTR_End = -1;
					}
					else if(strAns.equals("S")) // Ans = TR = S
					{
						iCorrection++;
						strRow = strRow + Setting.strSeparator_Tab + "TP";
					}
				}
				else 
				{
					if(strAns.equals("E"))
					{
						strRow = strRow + Setting.strSeparator_Tab + "Fail";
						
						iIndexAns_Start = -1;
						iIndexAns_End = -1;
					}
					else if(strAns.equals("S"))
					{
						strRow = strRow + Setting.strSeparator_Tab + "Fail";
					}
					
					if(strTR.equals("E"))
					{
						iIndexTR_Start = -1;
						iIndexTR_End = -1;
					}
				}
				
				alDetail.add(strRow);
			}
			else if(alTestingResult.get(i).length() > 0)
			{
				Setting.MyLog.info("alTestingResult Size > 0 : " + (i+1) + Setting.strSeparator_Tab + alTestingResult.get(i));
				break;
			}
			else
			{
				alDetail.add("");
			}
		}
		
		Lib.SaveFile(alDetail, fDetail, false);
		
		// Estimate Precision, Recall and F-Measure
		if(fSummary.exists())
		{
			alSummary = Lib.LoadArrayList(fSummary, "UTF-8", false);
		}
		else
		{
			alSummary.add("Name	Entities	Extractions	Corrections	Precision	Recall	F-Measure");
		}
		
		float fPrecision = 0.000000001f;
		float fRecall = 0.000000001f;
		float fFMeasure = 0.0f;
		
		if(iExtraction > 0)
		{
			fPrecision = (float) iCorrection / iExtraction;
		}
		
		if(iEntity > 0)
		{
			fRecall = (float) iCorrection / iEntity;
		}
		
		if((fPrecision + fRecall) > 0.0f)
		{
			fFMeasure = 2 * fPrecision * fRecall / (fPrecision + fRecall);
		}
		
		alSummary.add(strName + Setting.strSeparator_Tab
				+ String.valueOf(iEntity) + Setting.strSeparator_Tab
				+ String.valueOf(iExtraction) + Setting.strSeparator_Tab
				+ String.valueOf(iCorrection) + Setting.strSeparator_Tab
				+ df4.format(fPrecision) + Setting.strSeparator_Tab
				+ df4.format(fRecall) + Setting.strSeparator_Tab
				+ df4.format(fFMeasure));
		
		Lib.SaveFile(alSummary, fSummary, false);
	}
	
	public static void Compare_Partial(File fTestingResult, File fDetail, File fSummary, String strName) throws Exception
	{
		DecimalFormat df2 = new DecimalFormat("#0.00");
		DecimalFormat df4 = new DecimalFormat("#0.0000");
		
		ArrayList<String> alTestingResult = Lib.LoadArrayList(fTestingResult, "UTF-8", false);
		ArrayList<String> alDetail = new ArrayList<String>();
		ArrayList<String> alSummary = new ArrayList<String>();
		String strToken = "";
		String strAns = "";
		String strTR = "";
		
		// Precision
		boolean bPrecisionStart = false;
		float fPrecisionScore = 0.0f;
		int iPrecisionOverlap = 0;
		int iPrecisionEntity_Length = 0; // Length of Current TestingResult Entity
		int iPrecisionEntity_Total = 0; // |TestingResult Entity|
		
		// Recall
		boolean bRecallStart = false;
		float fRecallScore = 0.0f;
		int iRecallOverlap = 0;
		int iRecallEntity_Length = 0; // Length of Current Answer Entity
		int iRecallEntity_Total = 0; // |Answer Entity|
		
		for(int i = 0 ; i < alTestingResult.size() ; i++)
		{
			String strRow = alTestingResult.get(i);
			
			if(strRow.length() > 0)
			{
				try
				{
					strToken = strRow.split(Setting.strCRFTesting_Separator)[0];
					strAns = strRow.split(Setting.strCRFTesting_Separator)[strRow.split(Setting.strCRFTesting_Separator).length - 2];
					strTR = strRow.split(Setting.strCRFTesting_Separator)[strRow.split(Setting.strCRFTesting_Separator).length - 1];
				}
				catch (Exception e)
				{
					Setting.MyLog.debug("Something wrong at line: " + (i + 1 ) + "\t" + alTestingResult.get(i));
				}
				
				strRow = strToken + Setting.strSeparator_Tab + strAns + Setting.strSeparator_Tab + strTR;
				
				// Calc. Precision
				if(strTR.equals("B"))
				{
					bPrecisionStart = true;
					iPrecisionEntity_Length++;
					
					if(!strAns.equals("O"))
					{
						iPrecisionOverlap++;
					}
				}
				else if(strTR.equals("I"))
				{
					iPrecisionEntity_Length++;
					
					if(!strAns.equals("O"))
					{
						iPrecisionOverlap++;
					}
				}
				else if(strTR.equals("E"))
				{
					bPrecisionStart = false;
					iPrecisionEntity_Length++;
					iPrecisionEntity_Total++;
					
					if(!strAns.equals("O"))
					{
						iPrecisionOverlap++;
					}
					
					float fScore = (float) iPrecisionOverlap / iPrecisionEntity_Length;
					fPrecisionScore += fScore;
					strRow = strRow + Setting.strSeparator_Tab + "PScore = " + df2.format(fScore);
					
					iPrecisionOverlap = 0;
					iPrecisionEntity_Length = 0;
				}
				else if(strTR.equals("S"))
				{
					bPrecisionStart = false;
					iPrecisionEntity_Total++;
					
					if(!strAns.equals("O"))
					{
						fPrecisionScore += 1.0f;
						strRow = strRow + Setting.strSeparator_Tab + "PScore = 1.0";
					}
					else
					{
						strRow = strRow + Setting.strSeparator_Tab + "PScore = 0.0";
					}
				}
				
				// Calc Recall
				if(strAns.equals("B"))
				{
					bRecallStart = true;
					iRecallEntity_Length ++;
					
					if(!strTR.equals("O"))
					{
						iRecallOverlap++;
					}
				}
				else if(strAns.equals("I"))
				{
					iRecallEntity_Length ++;
					
					if(!strTR.equals("O"))
					{
						iRecallOverlap++;
					}
				}
				else if(strAns.equals("E"))
				{
					bRecallStart = false;
					iRecallEntity_Length++;
					iRecallEntity_Total++;
					
					if(!strTR.equals("O"))
					{
						iRecallOverlap++;
					}
					
					float fScore = (float) iRecallOverlap/iRecallEntity_Length;
					fRecallScore += fScore;
					strRow = strRow + Setting.strSeparator_Tab + "RScore = " + df2.format(fScore);
					
					iRecallOverlap = 0;
					iRecallEntity_Length = 0;
				}
				else if(strAns.equals("S"))
				{
					bRecallStart = false;
					iRecallEntity_Total ++;
					
					if(!strTR.equals("O"))
					{	
						fRecallScore += 1.0f;
						strRow = strRow + Setting.strSeparator_Tab + "RScore = 1.0";
					}
					else
					{
						strRow = strRow + Setting.strSeparator_Tab + "RScore = 0.0";	
					}
				}
				
				alDetail.add(strRow);
			}
			else if(alTestingResult.get(i).length() > 0)
			{
				Setting.MyLog.info("alTestingResult Size > 0 : " + (i + 1) + Setting.strSeparator_Tab + alTestingResult.get(i));
				break;
			}
			else
			{
				alDetail.add("");
			}
		}
		
		Lib.SaveFile(alDetail, fDetail, false);
		
		// Estimate Precision, Recall and F-Measure
		if(fSummary.exists())
		{
			alSummary = Lib.LoadArrayList(fSummary, "UTF-8", false);
		}
		else
		{
			alSummary.add("Name	Entities	Extractions	PrecisionScore	RecallScore	Precision	Recall	F-Measure");
		}
		
		float fPrecision = 0.000000001f;
		float fRecall = 0.000000001f;
		float fFMeasure = 0.0f;
		
		if(iPrecisionEntity_Total > 0)
		{
			fPrecision = (float) fPrecisionScore / iPrecisionEntity_Total;
		}
		
		if(iRecallEntity_Total > 0)
		{
			fRecall = (float) fRecallScore / iRecallEntity_Total;
		}
		
		if((fPrecision + fRecall) > 0.0f)
		{
			fFMeasure = 2 * fPrecision * fRecall / (fPrecision + fRecall);
		}
		
		alSummary.add(strName + Setting.strSeparator_Tab
				+ df2.format(iRecallEntity_Total) + Setting.strSeparator_Tab
				+ df2.format(iPrecisionEntity_Total) + Setting.strSeparator_Tab
				+ df2.format(fPrecisionScore) + Setting.strSeparator_Tab
				+ df2.format(fRecallScore) + Setting.strSeparator_Tab
				+ df4.format(fPrecision) + Setting.strSeparator_Tab
				+ df4.format(fRecall) + Setting.strSeparator_Tab
				+ df4.format(fFMeasure));
		
		Lib.SaveFile(alSummary, fSummary, false);
	}

	public static void Calc_Summary_Exact(File fSummary) throws Exception
	{
		ArrayList<String> alSummary = Lib.LoadArrayList(fSummary, "UTF-8", false);
		DecimalFormat df4 = new DecimalFormat("#0.0000");
		int iCorrection = 0;
		int iExtraction = 0;
		int iEntity = 0;
		float fPrecision = 0.000000001f;
		float fRecall = 0.000000001f;
		float fFMeasure = 0.0f;
		
		for(int i = 1; i < alSummary.size(); i++)
		{
			String strRow = alSummary.get(i);
			iEntity += Integer.valueOf(strRow.split(Setting.strSeparator_Tab)[1]);
			iExtraction += Integer.valueOf(strRow.split(Setting.strSeparator_Tab)[2]);
			iCorrection += Integer.valueOf(strRow.split(Setting.strSeparator_Tab)[3]);
		}
		
		if(iExtraction > 0)
		{
			fPrecision = (float) iCorrection / iExtraction;
		}
		
		if(iEntity > 0)
		{
			fRecall = (float) iCorrection / iEntity;
		}
		
		if((fPrecision + fRecall) > 0.0f)
		{
			fFMeasure = 2 * fPrecision * fRecall / (fPrecision + fRecall);
		}
		
		alSummary.add("Summary" + Setting.strSeparator_Tab
				+ String.valueOf(iEntity) + Setting.strSeparator_Tab
				+ String.valueOf(iExtraction) + Setting.strSeparator_Tab
				+ String.valueOf(iCorrection) + Setting.strSeparator_Tab
				+ df4.format(fPrecision) + Setting.strSeparator_Tab
				+ df4.format(fRecall) + Setting.strSeparator_Tab
				+ df4.format(fFMeasure));
		
		Lib.SaveFile(alSummary, fSummary, false);
	}
	
	public static void Calc_Summary_Partial(File fSummary) throws Exception
	{
		ArrayList<String> alSummary = Lib.LoadArrayList(fSummary, "UTF-8", false);
		DecimalFormat df2 = new DecimalFormat("#0.00");
		DecimalFormat df4 = new DecimalFormat("#0.0000");
		
		float fPrecisionScore = 0.0f;
		int iPrecisionEntity_Total = 0; // |TestingResult Entity|
		float fRecallScore = 0.0f;
		int iRecallEntity_Total = 0; // |Answer Entity|
		float fPrecision = 0.000000001f;
		float fRecall = 0.000000001f;
		float fFMeasure = 0.0f;
		
		for(int i = 1; i < alSummary.size(); i++)
		{
			String strRow = alSummary.get(i);
			
			iRecallEntity_Total += Float.valueOf(strRow.split(Setting.strSeparator_Tab)[1]);
			iPrecisionEntity_Total += Float.valueOf(strRow.split(Setting.strSeparator_Tab)[2]);
			fPrecisionScore += Float.valueOf(strRow.split(Setting.strSeparator_Tab)[3]);
			fRecallScore += Float.valueOf(strRow.split(Setting.strSeparator_Tab)[4]);
		}
		
		if(iPrecisionEntity_Total > 0)
		{
			fPrecision = (float) fPrecisionScore / iPrecisionEntity_Total;
		}
		
		if(iRecallEntity_Total > 0)
		{
			fRecall = (float) fRecallScore / iRecallEntity_Total;
		}
		
		if((fPrecision + fRecall) > 0.0f)
		{
			fFMeasure = 2 * fPrecision * fRecall / (fPrecision + fRecall);
		}
		
		alSummary.add("Summary" + Setting.strSeparator_Tab
				+ df2.format(iRecallEntity_Total) + Setting.strSeparator_Tab
				+ df2.format(iPrecisionEntity_Total) + Setting.strSeparator_Tab
				+ df2.format(fPrecisionScore) + Setting.strSeparator_Tab
				+ df2.format(fRecallScore) + Setting.strSeparator_Tab
				+ df4.format(fPrecision) + Setting.strSeparator_Tab
				+ df4.format(fRecall) + Setting.strSeparator_Tab
				+ df4.format(fFMeasure));
		
		Lib.SaveFile(alSummary, fSummary, false);
	}
}
