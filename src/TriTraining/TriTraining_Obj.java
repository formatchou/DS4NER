package TriTraining;
import Global.*;
import Testing.TestingLib;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import org.apache.commons.io.FileUtils;

public class TriTraining_Obj
{
	// Constant
	private ArrayList<String> alL;
	private ArrayList<String> alL_Tags;
	private ArrayList<String> alU_F_Org;
	private float fEpsilon = (float) 1E-9;
	
	// Basic Info
	private int iClassifierID = 0;
	private String strClassifierID = "";
	private int iIter = 0;
	private String strIter = "";
	private String strIter_Prev = "";
	
	private float fErrorRate_Prev = 0.0f;
	private float fErrorRate_Curr = 0.0f;
	private int iInitialization = 0;		// for first round
	private int iUpperBound = 0;			// u
	private int iSample = 0;				// Si = SubSample(Li,u) when |Li| > u or Si = Li when Li < u
	
	// Log & output format
	private String strLog_ErrorRate = "";
	private String strLog_TrainingData = "";
	private String strLog_SubSample = "";
	private String strLog_UpperBound = "";
	private String strLog_PNRate = "";
	
	DecimalFormat df = new DecimalFormat("");
	DecimalFormat df2 = new DecimalFormat("#0.00");
	DecimalFormat df4 = new DecimalFormat("#0.0000");
	DecimalFormat df9 = new DecimalFormat("#0.000000000");
	
	public TriTraining_Obj(int iClassifierID) throws Exception
	{
		this.iClassifierID = iClassifierID + 1;
		this.strClassifierID = "H" + String.valueOf(iClassifierID + 1);
	}
	
	public void run(int iIter) throws Exception
	{
		// Set parameters
		this.iIter = iIter;
		this.strIter = "I" + String.valueOf(iIter);
		this.strIter_Prev = "I" + String.valueOf(iIter - 1);
		
		this.strLog_ErrorRate = "";
		this.strLog_TrainingData = "";
		this.strLog_SubSample = "";
		this.strLog_UpperBound = "";
		this.strLog_PNRate = "";
		
		// ----------

		calcErrorRate();
		prepareNewlyData();
		outputLog();
		trainModel();
	}
	
	// Retrain → return 1, else return 0
	public int isRetrain() throws Exception
	{
		if(this.iUpperBound > 0)
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}
	
	// Rename the last (iter. - 1) model (e.g.: H1_I3.model → H1.model, but the last iter. is 4)
	public void renameModel() throws Exception
	{
		File fModel_OLD = Lib.SubFileOrFolder(Setting.dirTriTraining_Model, this.strClassifierID + "_I" + (this.iIter - 1) + ".model");
		File fModel_New = Lib.SubFileOrFolder(Setting.dirTriTraining_Model, this.strClassifierID + ".model");
		fModel_OLD.renameTo(fModel_New);
	}
	
	// Remove work files during tri-training (e.g.: testing result files)
	public void removeFiles() throws Exception
	{	
		FileUtils.deleteDirectory(Setting.dirTriTraining_ErrorRate);
		
		ArrayList<File> alRemoveFiles = new ArrayList<File>();
		// tritraining\*.* except Log.txt
		alRemoveFiles.addAll(Lib.GetFilesByRegex(Setting.dirTriTraining, "[L|U][^Log].+[.txt]"));
		
		// tritraining\Training\*.* except H*_I*_F.txt
		alRemoveFiles.addAll(Lib.GetFilesByRegex(Setting.dirTriTraining_Training, "[^H_].+[.txt]"));
		
		// tritraining\Model\*.model except H*.model
		alRemoveFiles.addAll(Lib.GetFilesByRegex(Setting.dirTriTraining_Model, "H[0-9]+_I[0-9]+.model"));
		
		for(File f:alRemoveFiles)
		{
			f.delete();
		}
	}
	
	// Evaluation (Exact or Partial)
	public void evaluation(String strType, boolean bRemoveFiles) throws Exception
	{
		File dirEvaluation = new File("");
		if(strType.toUpperCase().equals("EXACT"))
		{
			dirEvaluation = Setting.dirTriTraining_Evaluation_Exact;
		}
		else if(strType.toUpperCase().equals("PARTIAL"))
		{
			dirEvaluation = Setting.dirTriTraining_Evaluation_Partial;
		}
		dirEvaluation.mkdir();
		
		// Testing
		ArrayList<File> alTesting_Files = Lib.GetFilesByExtension(Setting.dirTesting, "_F.txt"); // C01_F.txt
		ArrayList<File> alModel_Files = Lib.GetFilesByRegex(Setting.dirTriTraining_Model, "H[0-9]+.model"); // H1.model
		
		for(File t:alTesting_Files)
		{
			// Testing
			String strCategory = t.getName().split("_")[0]; // e.g.: C01
			for(File m:alModel_Files)
			{
				// e.g.: C01_TR.H1
				File fTR = Lib.SubFileOrFolder(dirEvaluation, strCategory + "_TR." + m.getName().split("\\.")[0]);
				Lib.ExecCommand(Lib.CRF_Testing(m, t, fTR, 3));
			}
			
			// Merge several testing result files to single file 
			// e.g.: C01_TR.H1, C01_TR.H2, and C01_TR.H3
			ArrayList<File> alTesting_TR_Files = Lib.GetFilesByRegex(dirEvaluation, strCategory + "_TR.H[0-9]+");
			ArrayList<String> alTesting_TR_Tags = getTags(alTesting_TR_Files, "Evaluation");
			
			// Merge alTesting_Files and alTesting_TR_Tags to alTesting_TR
			ArrayList<String> alTesting = Lib.LoadArrayList(t, "UTF-8", false);
			
			// Output C01_TR.txt
			ArrayList<String> alTesting_TR = new ArrayList<String>();			
			int iIndex_Tags = 0;
			int iIndex_Label = 0;
			String strLabel = "";
			
			for(String s:alTesting)
			{
				if(s.length() > 0)
				{
					strLabel = alTesting_TR_Tags.get(iIndex_Tags).split("")[iIndex_Label];
					alTesting_TR.add(s.replace(" ", Setting.strSeparator_Tab) + Setting.strSeparator_Tab + strLabel);
					iIndex_Label ++;
				}
				else if(s.length() == 0)
				{
					alTesting_TR.add("");
					iIndex_Tags ++;
					iIndex_Label = 0;
				}
			}
			
			Lib.SaveFile(alTesting_TR, Lib.SubFileOrFolder(dirEvaluation, strCategory + "_TR.txt"), false);
		}
		
		// Exact/partial evaluation
		ArrayList<File> alFiles_TR = Lib.GetFilesByExtension(dirEvaluation, "_TR.txt");
		
		File fSummary = Lib.SubFileOrFolder(dirEvaluation, "Summary.txt");
		
		for(int i = 0 ; i < alFiles_TR.size() ; i++)
		{
			File fTestingResult = Lib.SubFileOrFolder(dirEvaluation, alFiles_TR.get(i).getName());
			File fDetail = Lib.SubFileOrFolder(dirEvaluation, Lib.GetPartialFilename(fTestingResult, "_TR.txt") + "_Detail.txt");
			String strName = Lib.GetPartialFilename(fTestingResult, "_TR.txt");
			
			if(strType.toUpperCase().equals("EXACT"))
			{
				TestingLib.Compare_Exact(fTestingResult, fDetail, fSummary, strName);
			}
			else if(strType.toUpperCase().equals("PARTIAL"))
			{
				TestingLib.Compare_Partial(fTestingResult, fDetail, fSummary, strName);
			}
			
			Setting.MyLog.info("Progress = " + (i + 1) + "/" + alFiles_TR.size() + " Done!");
		}
		
		if(strType.toUpperCase().equals("EXACT"))
		{
			TestingLib.Calc_Summary_Exact(fSummary);
		}
		else if(strType.toUpperCase().equals("PARTIAL"))
		{
			TestingLib.Calc_Summary_Partial(fSummary);
		}
		
		if(bRemoveFiles)
		{
			ArrayList<File> alRemoveFiles = Lib.GetFilesByRegex(dirEvaluation, ".+_TR.H.+");
			
			for(File f:alRemoveFiles)
			{
				f.delete();
			}
		}
	}
	
	// ----------
	
	// Calc. Error Rate
	private void calcErrorRate() throws Exception
	{
		Setting.MyLog.info("================ Calc. error rate start ===============");
		
		// Step 1: prepare testing results of L_NoAns_F
		File fL_NoAns_F = Lib.SubFileOrFolder(Setting.dirTriTraining, "L_NoAns_F.txt");
		for(int i = 0; i < Setting.iClassifier_Count ; i++)
		{
			File fModel_Prev = Lib.SubFileOrFolder(Setting.dirTriTraining_Model, "H" + String.valueOf(i + 1) + "_" + this.strIter_Prev + ".model");
			File fTR_ErrorRate = Lib.SubFileOrFolder(Setting.dirTriTraining_ErrorRate, "TR_H" + String.valueOf(i + 1) + "_" + this.strIter + ".txt");
			
			if(!fTR_ErrorRate.exists())
			{
				Lib.ExecCommand(Lib.CRF_Testing(fModel_Prev, fL_NoAns_F, fTR_ErrorRate, 3));
			}
		}
		
		// Step 2: Prepare alL, alL_Tags, all tags of testing results
		if(this.alL == null)
		{
			File fL = Lib.SubFileOrFolder(Setting.dirTriTraining, "L.txt");
			this.alL = Lib.LoadArrayList(fL, "UTF-8", false);
		}
		
		if(this.alL_Tags == null) // Prepare ans from L_F
		{
			File fL_F = Lib.SubFileOrFolder(Setting.dirTriTraining, "L_F.txt");
			this.alL_Tags = getTags(fL_F, "L");
		}
		
		File fErrorRate_TR = Lib.SubFileOrFolder(Setting.dirTriTraining_ErrorRate, "TR_" + this.strClassifierID + "_" + this.strIter + ".txt");
		ArrayList<File> alErrorRate_TR_File = Lib.GetFilesByRegex(Setting.dirTriTraining_ErrorRate, "TR_H[0-9]+_" + this.strIter + ".txt");
		alErrorRate_TR_File.remove(fErrorRate_TR);
		
		ArrayList<String> alErrorRate_Tags_Self = new ArrayList<String>();
		ArrayList<String> alErrorRate_Tags = new ArrayList<String>();
		if(this.iIter == 1) // first round
		{
			alErrorRate_Tags_Self = getTags(fErrorRate_TR, "TR");
			alErrorRate_Tags = getTags(alErrorRate_TR_File, "TRAINING");
		}
		else
		{
			alErrorRate_Tags = getTags(alErrorRate_TR_File, "TRAINING");
		}
		
		// Step 3: Calc. L^c, L^w, and Li^w
		if((alL_Tags.size() != alErrorRate_Tags.size())
				|| (this.iIter == 1 && (alErrorRate_Tags.size() != alErrorRate_Tags_Self.size())))
		{
			Setting.MyLog.warn("Calc. error rate fail since the sizes of testing results are not equal!");
			return;
		}
		
		int iTags_Common = 0; // L^c
		int iTags_Wrong = 0; // L^w
		int iTags_Wrong_Self = 0; // Li^w
		
		for(int i = 0 ; i < alErrorRate_Tags.size() ; i++)
		{	
			if(!alErrorRate_Tags.get(i).equals("No common tags")) // hj(x) = hk(x)
			{
				iTags_Common ++;
				
				if(!alErrorRate_Tags.get(i).equals(alL_Tags.get(i))) // hj(x) = hk(x), but hj(x) ≠ y
				{
					iTags_Wrong++;
				}
				
				if(this.iIter == 1 && !alErrorRate_Tags_Self.get(i).equals(alL_Tags.get(i))) // hi(x) ≠ y
				{
					iTags_Wrong_Self++;
				}
			}
		}
		
		// Step 4: Clac. error rate and initialization for first round
		this.fErrorRate_Prev = this.fErrorRate_Curr;
		this.fErrorRate_Curr = (iTags_Wrong)/(iTags_Common + fEpsilon);
		
		if(this.iIter == 1) // first round
		{	
			this.iInitialization = (int) Math.ceil((float) (iTags_Wrong_Self + 1) * iTags_Common / (iTags_Wrong + 1) - 1);
		}
		
		this.strLog_ErrorRate = String.valueOf(iTags_Wrong) + "/" + String.valueOf(iTags_Common) + " = " + df9.format(this.fErrorRate_Curr);
	}
	
	// Prepare newly examples from U
	private void prepareNewlyData() throws Exception
	{
		Setting.MyLog.info("============== Prepare newly data start ===============");
		
		// Step 1: Calc. iUpperBound (u)
		int iSample_Prev = this.iSample;

		if(this.iIter == 1) // first round
		{
			// u  ← this.iInitialization
			this.iUpperBound = Math.round(Math.min(this.alL.size() + iSample_Prev, this.iInitialization) * Setting.fDelta);
			this.strLog_UpperBound = df.format(this.iUpperBound) + " = min(" + df.format(this.alL.size()) + " + " + df.format(iSample_Prev) + ", " + df.format(this.iInitialization) + ") * " + df2.format(Setting.fDelta);
		}
		else if(this.fErrorRate_Curr < this.fErrorRate_Prev) // t-th round (t > 1), and error rate reduce
		{
			this.iUpperBound = (int) Math.ceil((this.fErrorRate_Prev + this.fEpsilon)* iSample_Prev/(this.fErrorRate_Curr + this.fEpsilon) - 1);
			this.iUpperBound = Math.round(Math.min(this.alL.size() + iSample_Prev, this.iUpperBound));
			this.strLog_UpperBound = df.format(this.iUpperBound) + " = min(" + df.format(this.alL.size()) + " + " + df.format(iSample_Prev) + ", " + df.format(this.iUpperBound) + ")";
		}
		else // t-th round (t > 1), but error rate increase → retrain = false
		{
			this.iUpperBound = 0;
			this.strLog_TrainingData = df.format(this.alL.size() + this.iSample) + " = " + df.format(this.alL.size()) + " + " + df.format(this.iSample);
			this.strLog_UpperBound = "0 (Because retrain = false)";
		}
		
		if(this.isRetrain() > 0) // Retrain → return 1, else return 0
		{
			// Step 2: Prepare testing data from U_F
			File fU_F = Lib.SubFileOrFolder(Setting.dirTriTraining_Training, "U_For" + this.strClassifierID + "_" + this.strIter + "_F.txt");
			
			if(this.alU_F_Org == null)
			{
				File fU_F_Org = Lib.SubFileOrFolder(Setting.dirTriTraining, "U_F.txt");
				this.alU_F_Org = Lib.LoadArrayList(fU_F_Org, "UTF-8", false);
			}
			
			// Randomly pick un-testing data from U_F
			ArrayList<String> alU_F = new ArrayList<String>();
			IntPair pairIndex_U_F = getFeaturesMatrix();	// all index of U_F
			IntPair pairIndex_Picked = new IntPair();		// all index of picked un-testing data from U_F
			
			int iU_Pick_Max = 0; // allow pick how many data from U  
			if(pairIndex_U_F.size() > (int) (this.iUpperBound * Setting.fPickMultiple))
			{
				iU_Pick_Max = (int) (this.iUpperBound * Setting.fPickMultiple);
			}
			else
			{
				iU_Pick_Max = pairIndex_U_F.size();
			}
			
			pairIndex_U_F.sortRandom();
			for(int i = 0 ; i < iU_Pick_Max ; i++)
			{
				for(int j = pairIndex_U_F.get(i).getStart() ; j < pairIndex_U_F.get(i).getEnd() ; j++)
				{
					alU_F.add(alU_F_Org.get(j));
				}
				alU_F.add("");
				
				pairIndex_Picked.add(pairIndex_U_F.get(i));
			}
			Lib.SaveFile(alU_F, fU_F, false);
			
			// Step 3: prepare testing results of U_F
			for(int i = 0; i < Setting.iClassifier_Count ; i++)
			{
				if((i + 1) != this.iClassifierID)
				{
					File fModel_Prev = Lib.SubFileOrFolder(Setting.dirTriTraining_Model, "H" + String.valueOf(i + 1) + "_" + this.strIter_Prev + ".model");
					File fTraining_TR = Lib.SubFileOrFolder(Setting.dirTriTraining_Training, "TR_For" + this.strClassifierID + "_ByH" + String.valueOf(i + 1) + "_" + this.strIter + ".txt");
					Lib.ExecCommand(Lib.CRF_Testing(fModel_Prev, fU_F, fTraining_TR, 3));
				}
			}
			
			// Step 4: Merge testing results to tags
			ArrayList<File> alTraining_TR_File = Lib.GetFilesByRegex(Setting.dirTriTraining_Training, "TR_For"+ this.strClassifierID + "_ByH[0-9]+_" + this.strIter + ".txt");
			ArrayList<String> alTraining_TR_Tags = getTags(alTraining_TR_File, "TRAINING");
			
			// Remove tseting resul with "No common tags"
			for(int i = alTraining_TR_Tags.size() -1 ; i >= 0 ; i--)
			{
				if(alTraining_TR_Tags.get(i).equals("No common tags"))
				{
					alTraining_TR_Tags.remove(i);
//					pairIndex_Picked.remove(i);
					pairIndex_Picked.removeElement(i);
				}
			}
			
			// Step 5: prepare new training data and newly examples with tags (L_F + NewlyExamples)
			File fL_F = Lib.SubFileOrFolder(Setting.dirTriTraining, "L_F.txt");
			ArrayList<String> alTrainingData =  Lib.LoadArrayList(fL_F, "UTF-8", false); 
			ArrayList<String> alNewlyExamples = prepareNewlyExamples(alTraining_TR_Tags, pairIndex_Picked);
			alTrainingData.add("");
			alTrainingData.addAll(alNewlyExamples);
			
			// remove last blank line
			alTrainingData.remove(alTrainingData.size() - 1);
			File fTrainingData = Lib.SubFileOrFolder(Setting.dirTriTraining_Training, this.strClassifierID + "_" + this.strIter + "_F.txt");
			Lib.SaveFile(alTrainingData, fTrainingData, false);
		}
		else // ReTrain = false, copy prev training data and copy prev model
		{ 
			this.strLog_SubSample = "|Si| = |Si_Prev| = " + df.format(iSample_Prev);
			
			File fTrainingData_Prev = Lib.SubFileOrFolder(Setting.dirTriTraining_Training, this.strClassifierID + "_" + this.strIter_Prev + "_F.txt");
			File fTrainingData = Lib.SubFileOrFolder(Setting.dirTriTraining_Training, this.strClassifierID + "_" + this.strIter + "_F.txt");
			FileUtils.copyFile(fTrainingData_Prev, fTrainingData);
		}
	}
	
	// Output Tri-Training log
	private void outputLog() throws Exception
	{
		Setting.MyLog.info("=================== Output log start ==================");
		
		File fLog = Lib.SubFileOrFolder(Setting.dirTriTraining, "Log.txt");
		
		if(!fLog.exists())
		{
			String strInfo = "Global parameters";
			strInfo += Setting.strSeparator_Tab + "Co-labeling θ = " + df2.format(Setting.fCoLabeling_Threshold);
			strInfo += Setting.strSeparator_Tab + "First round Δ = " + df2.format(Setting.fDelta);
			strInfo += Setting.strSeparator_Tab + "Pick Multiple = " + df2.format(Setting.fPickMultiple);
			if(Setting.fPostiveRate == -1)
			{
				strInfo += Setting.strSeparator_Tab + "Pos./Neg. examples rate = Random";
			}
			else
			{
				strInfo += Setting.strSeparator_Tab + "Postive example (%) = " + df2.format(Setting.fPostiveRate);
			}
			strInfo += Setting.strSeparator_Tab + "Selection method = Random";
			Lib.SaveFile(strInfo, fLog, false);
			
			String strTitle = "Iteration";
			strTitle += Setting.strSeparator_Tab + "Classifier ID";
			strTitle += Setting.strSeparator_Tab + "Error Rate";
			strTitle += Setting.strSeparator_Tab + "|Training Data| = |L| + |Si|";
			strTitle += Setting.strSeparator_Tab + "|Si| = SubSample(Li,u) or |Li|";
			strTitle += Setting.strSeparator_Tab + "u = min(|L| + |Si_Prev|, u) * Δ";
			strTitle += Setting.strSeparator_Tab + "Pos./Neg. examples in Si";
			strTitle += Setting.strSeparator_Tab + "ReTrain?";
			Lib.SaveFile(strTitle, fLog, true);
		}
		
		// ----------
		
		String strLog = String.valueOf(this.iIter);
		strLog += Setting.strSeparator_Tab + String.valueOf(this.iClassifierID);
		strLog += Setting.strSeparator_Tab + this.strLog_ErrorRate;		// Error Rate
		strLog += Setting.strSeparator_Tab + this.strLog_TrainingData;	// |Training Data| = |L| + |Si|
		strLog += Setting.strSeparator_Tab + this.strLog_SubSample;		// |Si| = SubSample(Li,u) or |Li|
		strLog += Setting.strSeparator_Tab + this.strLog_UpperBound;	// u = min(|L| + |Si_Prev|, u) * Δ
		strLog += Setting.strSeparator_Tab + this.strLog_PNRate;		// Pos./Neg. Examples in Si
		if(this.isRetrain() > 0) // Retrain → return 1, else return 0
		{
			strLog += Setting.strSeparator_Tab + "True";
		}
		else
		{
			strLog += Setting.strSeparator_Tab + "False";
		}
		
		Lib.SaveFile(strLog, fLog, true);
	}
	
	// Build currect iteration model 
	private void trainModel() throws Exception
	{
		Setting.MyLog.info("================== Train model start ==================");
		
		if(this.isRetrain() > 0) // Retrain → return 1, else return 0
		{
			File fTrainingData = Lib.SubFileOrFolder(Setting.dirTriTraining_Training, this.strClassifierID + "_" + this.strIter + "_F.txt");
			File fModel = Lib.SubFileOrFolder(Setting.dirTriTraining_Model, this.strClassifierID + "_" + this.strIter + ".model");
			Lib.ExecCommand(Lib.CRF_Training(fTrainingData, fModel));
		}
		else // ReTrain = false, copy prev model as currect model
		{
			File fModel_Prev = Lib.SubFileOrFolder(Setting.dirTriTraining_Model, this.strClassifierID + "_" + this.strIter_Prev + ".model");
			File fModel = Lib.SubFileOrFolder(Setting.dirTriTraining_Model, this.strClassifierID + "_" + this.strIter + ".model");
			FileUtils.copyFile(fModel_Prev, fModel);
		}
	}
	
	// ----------
	
	// Get tags from L_F or Self's testing result
	private ArrayList<String> getTags(File fInput, String strType) throws Exception
	{	
		ArrayList<String> alInput = Lib.LoadArrayList(fInput, "UTF-8", false);
		ArrayList<String> alOutput = new ArrayList<String>();
		String strTags = "";
		alInput.add("");
		
		switch(strType.toUpperCase())
		{
			case "L": // for L_F
				for(String s:alInput)
				{
					if(s.length() > 0)
					{	
						strTags += s.split(Setting.strCRFTraining_Separator)[s.split(Setting.strCRFTraining_Separator).length - 1];
					}
					else if(strTags.length() > 0)
					{
						alOutput.add(strTags);
						strTags = "";
					}
				}
				break;
				
			case "TR": // for testing result
				boolean bStart = false;
				for(String s:alInput)
				{
					if(s.length() > 0)
					{
						if(s.startsWith("# 0 "))
						{
							bStart = true;
						}
						else if(bStart)
						{	
							strTags += s.split(Setting.strCRFTesting_Separator)[s.split(Setting.strCRFTesting_Separator).length - 1];
						}
					}
					else if(strTags.length() > 0)
					{
						bStart = false;
						alOutput.add(strTags);
						strTags = "";
					}
				}
				
				break;
		}
		
		return alOutput;
	}
	
	// Merge several testing results to single testing result tags 
	private ArrayList<String> getTags(ArrayList<File> alFiles, String strType) throws Exception
	{
		ArrayList<String>[] alErrorRate_TR = new ArrayList[alFiles.size()];
		StrFloatPair pairTR = new StrFloatPair();
		ArrayList<String> alOutput = new ArrayList<String>();
		
		for(int i = 0; i < alFiles.size(); i++)
		{
			alErrorRate_TR[i] = Lib.LoadArrayList(alFiles.get(i), "UTF-8", false);
			alErrorRate_TR[i].add("");
		}

		// ----------
		
		String strTags = "";
		float fProb = 0.0f;
		List<String> alTemp;
		IntPair pairIndex = getTRIndex(alErrorRate_TR[0]);
		
		for(int i = 0; i < pairIndex.size(); i++) // for all index = all sentences
		{	
			for(int j = 0; j < alErrorRate_TR.length; j++) // for all classifiers
			{
				alTemp = alErrorRate_TR[j].subList(pairIndex.get(i).getStart(), pairIndex.get(i).getEnd() + 1);
				
				for(String s:alTemp)
				{
					if(s.length() > 0)
					{
						if(s.startsWith("# ") && s.length() == 12)
						{
							fProb = Float.valueOf(s.split(" ")[2]);
						}
						else
						{
							strTags += s.split(Setting.strCRFTesting_Separator)[s.split(Setting.strCRFTesting_Separator).length - 1];
						}
					}
					else
					{
						pairTR.addPair(strTags, fProb);
						strTags = "";
						fProb = 0.0f;
					}
				}
			}
			
			pairTR.sortByFloat(StrFloatPair.SortBy.DESC);
			
			if(strType.toUpperCase().equals("TRAINING"))
			{
				if(pairTR.get(0).getFloat() >= Setting.fCoLabeling_Threshold * (Setting.iClassifier_Count - 1))
				{
					alOutput.add(pairTR.get(0).getString());
				}
				else
				{
					alOutput.add("No common tags");
				}
			}
			else // evaluation
			{
				alOutput.add(pairTR.get(0).getString());
			}
			
			pairTR.reset();
		}
		
		return alOutput;
	}
	
	// Input = testing result with top n probabilities, output = the start/end index of each sentence block
	private IntPair getTRIndex(ArrayList<String> alInput) throws Exception
	{
		IntPair pairIndex = new IntPair();
		boolean bEnd = false;
		
		int iID = 0;
		int iStart = 0;
		
		for(int i = 0; i < alInput.size(); i++)
		{	
			if(alInput.get(i).startsWith("# 0 "))
			{
				iStart = i;
			}
			else if(alInput.get(i).startsWith("# " + String.valueOf(Setting.iTesting_TopN - 1) + " "))
			{
				bEnd = true;
			}
			else if(bEnd && alInput.get(i).length() == 0)
			{
				bEnd = false;
				pairIndex.addPair(iID, iStart, i);
				iID ++;
			}
		}
		
		return pairIndex;
	}
	
	// Input = features matrix, output = the start/end index of each sentence block
	private IntPair getFeaturesMatrix() throws Exception
	{
		IntPair pairIndex = new IntPair();
		boolean bStart = false;
		
		int iID = 0;
		int iStart = 0;
		
		for(int i = 0; i < this.alU_F_Org.size(); i++)
		{
			if(this.alU_F_Org.get(i).length() > 0)
			{
				if(!bStart)
				{
					bStart = true;
					iStart = i;
				}
			}
			else if(this.alU_F_Org.get(i).length() == 0)
			{
				bStart = false;
				pairIndex.addPair(iID, iStart, i);
				iID++;
			}
			
			if(bStart && i == this.alU_F_Org.size() - 1)
			{
				pairIndex.addPair(iID, iStart, i);
			}
		}
		
		return pairIndex;
	}
	
	// Prepare newly examples with tags
	private ArrayList<String> prepareNewlyExamples(ArrayList<String> alTraining_TR_Tags, IntPair pairIndex_Picked) throws Exception
	{
		ArrayList<String> alOutput = new ArrayList<String>();
		ArrayList<String> alU_UsedID = new ArrayList<String>();
		char[] chTags;
		int iIndexTags = 0;
		int iPos = 0; // postive example
		int iNeg = 0; // negative example
		int iPos_Max = 0; // postive example max
		int iNeg_Max = 0; // negative example max
		boolean bAllowPick = false;
		
		// the percentage of postive examples in newly examples, 1.0f = 100% postive examples, -1.0f = Random
		if(Setting.fPostiveRate == -1.0f)
		{
			iPos_Max = this.iUpperBound;
			iNeg_Max = this.iUpperBound;
		}
		else
		{
			iPos_Max = Math.round(this.iUpperBound * Setting.fPostiveRate);
			iNeg_Max = this.iUpperBound - iPos_Max;
		}
		
		for(int i = 0 ; i < pairIndex_Picked.size() ; i++)
		{
			chTags = alTraining_TR_Tags.get(i).toCharArray();
			iIndexTags = 0;
			
			// if tags contain "B" or "S" → postive example, otherwise → negative example
			if(alTraining_TR_Tags.get(i).contains("B") || alTraining_TR_Tags.get(i).contains("S"))
			{
				if(iPos < iPos_Max)
				{
					iPos ++;
					bAllowPick = true;
				}
			}
			else
			{
				if(iNeg < iNeg_Max)
				{
					iNeg ++;
					bAllowPick = true;
				}
			}
			
			if(bAllowPick)
			{
				for(int j = pairIndex_Picked.get(i).getStart() ; j < pairIndex_Picked.get(i).getEnd() ; j++)
				{
					try
					{
						alOutput.add(alU_F_Org.get(j) + Setting.strCRFTraining_Separator + chTags[iIndexTags]);
					}
					catch (Exception e)
					{
						Setting.MyLog.warn("e = " + e.toString());
						Setting.MyLog.warn("Start = " + pairIndex_Picked.get(i).getStart() + "\tEnd = " + pairIndex_Picked.get(i).getEnd() + "\tj = " + j);
						Setting.MyLog.warn("alU_F_Org.get(j) = 「" + alU_F_Org.get(j) + "」");
						Setting.MyLog.warn("alTraining_TR_Tags.get(i) = 「" + alTraining_TR_Tags.get(i) + "」");
						Setting.MyLog.warn("chTags[iIndexTags] = 「" + chTags[iIndexTags] + "」");
						Setting.MyLog.warn("");
					}
					
					iIndexTags ++;
					
					if(j == pairIndex_Picked.get(i).getEnd() - 1)
					{	
						alOutput.add("");
						alU_UsedID.add(String.valueOf(pairIndex_Picked.get(i).getID()));
					}
				}
				
				if((iPos + iNeg) == this.iUpperBound)
				{
					break;
				}
			}
			
			bAllowPick = false;
		}
		
		this.iSample = (iPos + iNeg);
		
		if(this.iSample == this.iUpperBound)
		{	
			this.strLog_SubSample = "|Si| = SubSample(Li,u) = " + df.format(this.iSample);
		}
		else
		{
			this.strLog_SubSample = "|Si| = |Li| = " + df.format(this.iSample);
		}
		
		this.strLog_PNRate = df.format(iPos) + "/" + df.format(iNeg) + " = " + df2.format((float) iPos / (iPos + iNeg)) + "/" + df2.format((float)iNeg / (iPos + iNeg));
		
		this.strLog_TrainingData = df.format(this.alL.size() + this.iSample) + " = " + df.format(this.alL.size()) + " + " + df.format(this.iSample);
//		File fU_UsedID = Lib.SubFileOrFolder(Setting.dirTriTraining, "U_" + this.strClassifierID + "_UsedID.txt");
//		Lib.SaveFile(alU_UsedID, fU_UsedID, false);
		return alOutput;
	}
}