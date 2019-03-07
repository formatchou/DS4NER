package Global;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.*;

public class Features_Context
{
	static ArrayList<String> alInput = new ArrayList<String>();
	static ArrayList<String>[] alOutput;
	static int iThread = 1;
	
	public Features_Context() throws IOException
	{
		
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
				String strC0 = "";
				
				String strToken_Prev = "";
				String strToken_Next = "";
				String strLabel = "";
				int iIndex = 0;
				boolean bEntity = false;
				
				for(int i = iStart; i <= iEnd; i++)
				{
					alToken = Lib.SentenceToToken(alInput.get(i));
					
					// Feature Extraction
					for(int j = 0; j < alToken.size(); j++)
					{
						strC0 = alToken.get(j);
						
						// Prev Token
						if(j - 1 >= 0)
						{
							strToken_Prev = alToken.get(j - 1);
						}
						
						// Next Token
						if(j + 1 < alToken.size())
						{	
							strToken_Next = alToken.get(j + 1);
						}
						
						// ----------
						
						if(strC0.equals(Setting.strEntity_Start))
						{
							bEntity = true;
						}
						else if(strC0.equals(Setting.strEntity_End))
						{
							bEntity = false;
						}
						else
						{
							strFeature = strC0;											// C0  = Current token
							
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
						strToken_Prev = "";
						strToken_Next = "";
						strLabel = "";
						iIndex = 0;
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
	}
}