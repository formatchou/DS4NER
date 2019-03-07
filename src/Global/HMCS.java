package Global;
import java.util.*;
import java.text.DecimalFormat;

// Harmonic Mean of Confidence and Support (HMCS)
public class HMCS extends ArrayList<HMCS>
{	
	DecimalFormat df = new DecimalFormat("#0.000000");
	private static int occcur_min = 1;		// the min #occurrence of dictionary term
	private static float w = 0.5f;		// the weight of Confidence (0.0f-1.0f), the weight of Support is (1.0f-this_value)
	private String term;					// dictionary term
	private int occur;						// #occurrence of dictionary term
	private int global_occur_max;			// the max #occurrence of dictionary terms
	private int global_occur_min;			// the min #occurrence of dictionary terms
	private int total;						// #occurence of dictionary term in corpus
	private float conf;					// Confidence = occur/total
	private float supp;					// Support = log(occur)
	private float hmcs;					// Harmonic Mean of Confidence and Support (HMCS)
	private float threshold;				// Cumulative Score Threshold Percentage for Support/Confidence/HMCS, 0.0f-1.0f
	private HashMap<String, Integer> hmTerm;
	private String strTab = "\t";
	
	// --------------------
	
	private HMCS(String strTerm)
	{
		this.term = strTerm;
		this.occur = 1;
		this.total = 1;
		this.hmTerm = new HashMap<String, Integer>();
	}
	
	private HMCS(String strTerm, int iOccur)
	{
		this.term = strTerm;
		this.occur = iOccur;
		this.total = iOccur;
		this.hmTerm = new HashMap<String, Integer>();
	}
	
	// --------------------
	
	public enum SortBy
	{
		ASC, DESC
	}
	
	public HMCS()
	{
		this.hmTerm = new HashMap<String, Integer>();
	}
	
	public HMCS(float fThreshold)
	{
		this.threshold = fThreshold;
		this.hmTerm = new HashMap<String, Integer>();
	}
	
	public void addTerm(String strTerm)
	{	
		if(this.isEmpty() || !this.contains(strTerm))
		{	
			this.add(new HMCS(strTerm));
			this.hmTerm.put(strTerm, this.size() - 1);
		}
		else
		{
			int iIndex = this.hmTerm.get(strTerm);
			this.get(iIndex).occur += 1;
			this.get(iIndex).total += 1;
		}
	}
	
	public void addTerm(String strTerm, int iOccur)
	{
		if(this.isEmpty() || !this.contains(strTerm))
		{	
			this.add(new HMCS(strTerm, iOccur));
			this.hmTerm.put(strTerm, this.size() - 1);
		}
		else
		{
			int iIndex = this.hmTerm.get(strTerm);
			this.get(iIndex).occur += iOccur;
			this.get(iIndex).total += iOccur;
		}
	}
	
	public void setTotal(int iTotal)
	{	
		this.total = iTotal;
		this.conf = (float) occur / total;
		this.hmcs = this.conf * this.supp / (HMCS.w * this.conf + (1 - HMCS.w) * this.supp);
	}
	
	public void calcSupport()
	{
		this.global_occur_max = 2;
		this.global_occur_min = 2;
		
		// filter occur. < occur_min and find the max/min occur.
		for(int i = this.size() - 1; i >= 0; i--)
		{
			if(this.get(i).occur <= HMCS.occcur_min)
			{
				this.remove(i);
			}
			else
			{
				if(this.get(i).occur > this.global_occur_max)
				{
					this.global_occur_max = this.get(i).occur;
				}
				
				if(this.get(i).occur < this.global_occur_min)
				{
					this.global_occur_min = this.get(i).occur;
				}
			}
		}
		
		// update re-scaled (normalized) support
		for(HMCS h:this)
		{
			// Re-scaled support = lower + (upper - lower) * (log(occur.) - log(min-1))/(log(max) - log(min-1))
			// upper/lower are upper/lower bound after rescale, upper = 1 and lower = 0
			// max = the max #occurence of dictionary terms
			// min = the min #occurence of dictionary terms
			h.supp = (float) ((Math.log10(h.occur) - Math.log10(this.global_occur_min - 1))/(Math.log10(this.global_occur_max) - Math.log10(this.global_occur_min - 1)));
		}		
	}
	
	// --------------------
	
	public void filterBySupport()
	{
		this.sortBySupport(HMCS.SortBy.DESC);
		float fSum = 0.0f;
		float fThreshold = this.getFilterThreshold("Supp");
		float fSupp = 0.0f;
		
		for(int i = 0; i < this.size() - 1; i++)
		{
			fSum += this.get(i).supp;
			
			if(fSum > fThreshold)
			{	
				fSupp = this.get(i).supp;
				break;
			}
		}
		
		for(int i = this.size() - 1; i >= 0; i--)
		{
			if(this.get(i).supp < fSupp)
			{
				this.remove(i);
			}
		}
	}
	
	public void filterByConfidence()
	{
		this.sortByConfidence(HMCS.SortBy.DESC);
		float fSum = 0.0f;
		float fThreshold = this.getFilterThreshold("Conf");
		float fConf = 0.0f;
		
		for(int i = 0; i < this.size() - 1; i++)
		{
			fSum += this.get(i).conf;
			
			if(fSum > fThreshold)
			{	
				fConf = this.get(i).conf;
				break;
			}
		}
		
		for(int i = this.size() - 1; i >= 0; i--)
		{
			if(this.get(i).conf < fConf)
			{
				this.remove(i);
			}
		}
	}
	
	public void filterByHMCS()
	{
		this.sortByHMCS(HMCS.SortBy.DESC);
		float fSum = 0.0f;
		float fThreshold = this.getFilterThreshold("HMCS");
		float fHMCS = 0.0f;
		
		for(int i = 0; i < this.size() - 1; i++)
		{
			fSum += this.get(i).hmcs;
			
			if(fSum > fThreshold)
			{	
				fHMCS = this.get(i).hmcs;
				break;
			}
		}
		
		for(int i = this.size() - 1; i >= 0; i--)
		{
			if(this.get(i).hmcs < fHMCS)
			{
				this.remove(i);
			}
		}
	}
		
	// get the threshold * support/confidence/HMCS summation to filter terms
	private float getFilterThreshold(String strMethod)
	{
		float fSum = 0.0f;
		float fThreshold = 0.0f;
		
		switch(strMethod.toUpperCase())
		{
			case "SUPP":
				for(int i = 0; i < this.size(); i++)
				{
					fSum += this.get(i).supp;
				}
				
				fThreshold = fSum * threshold;
				
				break;
				
			case "CONF":
				for(int i = 0; i < this.size(); i++)
				{
					fSum += this.get(i).conf;
				}
				fThreshold = fSum * threshold;
				break;
				
			case "HMCS":
				for(int i = 0; i < this.size(); i++)
				{
					fSum += this.get(i).hmcs;
				}
				fThreshold = fSum * threshold;
				break;
		}
		
		return fThreshold;	
	}
	
	// --------------------
	
	public String getTerm()
	{	
		return this.term;	
	}
	
	public int getOccur()
	{	
		return this.occur;	
	}
	
	public String getParameters()
	{	
		return "Cumulative Score Threshold Percentage = " + String.valueOf(threshold);
	}
	
	public String outputTerm()
	{
		return this.term + strTab + this.occur + strTab + this.total
				+ strTab+ df.format(this.conf) + strTab + df.format(this.supp) + strTab + df.format(this.hmcs);	
	}
	
	// --------------------
	
	public void sortBySupport(SortBy sort)
	{
		switch(sort)
		{
			case ASC:
				Collections.sort(this, new Comparator<HMCS>()
				{
					public int compare(HMCS o1, HMCS o2)
					{
						if(o1.supp == o2.supp)
						{
							return o1.term.compareTo(o2.term);
						}
						else
						{
							return Float.compare(o1.supp, o2.supp);
						}
					}
				});
				break;
			case DESC:
				Collections.sort(this, new Comparator<HMCS>()
				{
					public int compare(HMCS o1, HMCS o2)
					{
						if(o1.supp == o2.supp)
						{
							return o1.term.compareTo(o2.term);
						}
						else
						{
							return Float.compare(o2.supp, o1.supp);
						}
					}
				});
				break;
		}
	}
	
	public void sortByConfidence(SortBy sort)
	{
		switch(sort)
		{
			case ASC:
				Collections.sort(this, new Comparator<HMCS>()
				{
					public int compare(HMCS o1, HMCS o2)
					{
						if(o1.conf == o2.conf)
						{
							return o1.term.compareTo(o2.term);
						}
						else
						{
							return Float.compare(o1.conf, o2.conf);
						}
					}
				});
				break;
			case DESC:
				Collections.sort(this, new Comparator<HMCS>()
				{
					public int compare(HMCS o1, HMCS o2)
					{
						if(o1.conf == o2.conf)
						{
							return o1.term.compareTo(o2.term);
						}
						else
						{
							return Float.compare(o2.conf, o1.conf);
						}
					}
				});
				break;
		}
	}
	
	public void sortByHMCS(SortBy sort)
	{
		switch(sort)
		{
			case ASC:
				Collections.sort(this, new Comparator<HMCS>()
				{
					public int compare(HMCS o1, HMCS o2)
					{
						if(o1.hmcs == o2.hmcs)
						{
							return o1.term.compareTo(o2.term);
						}
						else
						{
							return Float.compare(o1.hmcs, o2.hmcs);
						}
					}
				});
				break;
			case DESC:
				Collections.sort(this, new Comparator<HMCS>()
				{
					public int compare(HMCS o1, HMCS o2)
					{
						if(o1.hmcs == o2.hmcs)
						{
							return o1.term.compareTo(o2.term);
						}
						else
						{
							return Float.compare(o2.hmcs, o1.hmcs);
						}
					}
				});
				break;
		}
	}
	
	public void sortByTermLength(SortBy sort)
	{
		switch(sort)
		{
			case ASC:
				Collections.sort(this, new Comparator<HMCS>()
				{
					public int compare(HMCS o1, HMCS o2)
					{
						if(o1.term.length() == o2.term.length())
						{
							return o1.term.compareTo(o2.term);
						}
						else
						{
							return o1.term.length() - o2.term.length();
						}
					}
				});
				break;
			case DESC:
				Collections.sort(this, new Comparator<HMCS>()
				{
					public int compare(HMCS o1, HMCS o2)
					{
						if(o1.term.length() == o2.term.length())
						{
							return o1.term.compareTo(o2.term);
						}
						else
						{
							return o2.term.length() - o1.term.length();
						}
					}
				});
				break;
		}
	}
	
	// --------------------
	
	public boolean contains(String strTerm)
	{
		if(this.isEmpty() || !this.hmTerm.containsKey(strTerm))
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	public boolean equals(Object o)
	{
		String strString = ((HMCS) o).term;
		if(term.equals(strString))
		{
			return true;
		}
		else
		{	
			return false;
		}
	}
}
