package Global;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

public class StrIntPair extends ArrayList<StrIntPair>
{
	private String s = "";		// String
	private int i = 0;			// Int
	private HashMap<String, Integer> hmTerm;
	
	public enum SortBy
	{
		ASC, DESC
	}
	
	public StrIntPair()
	{
		this.hmTerm = new HashMap<String, Integer>();
	}
	
	public StrIntPair(String strString, int iInt)
	{
		s = strString;
		i = iInt;
	}
	
	// --------------------

	private StrIntPair(String strString)
	{	
		s = strString;
		i = 1;
	}
	
	private void addInt(int iInt)
	{
		i = i + iInt;
	}
	
	// --------------------
	
	public void addString(String strString)
	{
		if(this.isEmpty() || !this.hmTerm.containsKey(strString))
		{
			this.add(new StrIntPair(strString));
			this.hmTerm.put(strString, this.size() - 1);
		}
		else
		{
			int iIndex = this.hmTerm.get(strString);
			this.get(iIndex).i += 1;
		}
	}
	
	public void addPair(String strString, int iInt)
	{
		if(this.isEmpty() || !this.hmTerm.containsKey(strString))
		{
			this.add(new StrIntPair(strString, iInt));
			this.hmTerm.put(strString, this.size() - 1);
		}
		else
		{
			int iIndex = this.hmTerm.get(strString);
			this.get(iIndex).i += iInt;
		}
	}
	
	public void setPair(String strString, int iInt)
	{
		if(this.hmTerm.containsKey(strString))
		{
			int iIndex = this.hmTerm.get(strString);
			
			if(this.get(iIndex).getString() == strString)
			{
				this.get(iIndex).i = iInt;
			}
		}
	}
	
	public String getString()
	{
		return s;
	}

	public int getInt()
	{
		return i;
	}
	
	public int getInt(String strString)
	{
		int iIndex = this.hmTerm.get(strString);
		if(iIndex != -1)
		{
			return this.get(iIndex).getInt();
		}
		else
		{
			return 0;
		}
	}
	
	public int getIntSum()
	{
		int iSum = 0;
		
		for(StrIntPair p:this)
		{
			iSum += p.getInt();;
		}
		
		return iSum;
	}
	
	public String getStrIntPair()
	{
		return s + Setting.strSeparator_Tab + i;
	}
	
	// --------------------
	
	public void sortByStr(SortBy sort)
	{
		switch(sort)
		{
			case ASC:
				Collections.sort(this, new Comparator<StrIntPair>()
				{
					public int compare(StrIntPair o1, StrIntPair o2)
					{
						return o1.getString().compareTo(o2.getString());
					}
				});
				break;
			case DESC:
				Collections.sort(this, new Comparator<StrIntPair>()
				{
					public int compare(StrIntPair o1, StrIntPair o2)
					{
						return o2.getString().compareTo(o1.getString());
					}
				});
				break;
		}
		
		UpdateHasmMap();
	}

	public void sortByInt(SortBy sort)
	{
		switch(sort)
		{
			case ASC:
				Collections.sort(this, new Comparator<StrIntPair>()
				{
					public int compare(StrIntPair o1, StrIntPair o2)
					{	
						if(o1.getInt() == o2.getInt())
						{
							return o1.getString().compareTo(o2.getString());
						}
						else
						{
							return o1.getInt() - o2.getInt();
						}
					}
				});
				break;
			case DESC:
				Collections.sort(this, new Comparator<StrIntPair>()
				{
					public int compare(StrIntPair o1, StrIntPair o2)
					{
						if(o1.getInt() == o2.getInt())
						{
							return o1.getString().compareTo(o2.getString());
						}
						else
						{
							return o2.getInt() - o1.getInt();
						}
					}
				});
				break;
		}
		
		UpdateHasmMap();
	}
	
	public void sortByLength(SortBy sort)
	{
		switch(sort)
		{
			case ASC:
				Collections.sort(this, new Comparator<StrIntPair>()
				{
					public int compare(StrIntPair o1, StrIntPair o2)
					{
						if(o1.getString().length() == o2.getString().length())
						{
							return o1.getString().compareTo(o2.getString());
						}
						else
						{
							return o1.getString().length() - o2.getString().length();
						}
					}
				});
				break;
			case DESC:
				Collections.sort(this, new Comparator<StrIntPair>()
				{
					public int compare(StrIntPair o1, StrIntPair o2)
					{
						if(o1.getString().length() == o2.getString().length())
						{
							return o1.getString().compareTo(o2.getString());
						}
						else
						{
							return o2.getString().length() - o1.getString().length();
						}
					}
				});
				break;
		}
		
		UpdateHasmMap();
	}
	
	public void sortByTokenLength(SortBy sort)
	{
		switch(sort)
		{
			case ASC:
				Collections.sort(this, new Comparator<StrIntPair>()
				{
					public int compare(StrIntPair o1, StrIntPair o2)
					{		
						if(Lib.GetTokenLength(o1.getString()) == Lib.GetTokenLength(o2.getString()))
						{
							return o1.getString().compareTo(o2.getString());
						}
						else
						{
							return Integer.compare(Lib.GetTokenLength(o1.getString()), Lib.GetTokenLength(o2.getString()));
						}
					}
				});
				break;
			case DESC:
				Collections.sort(this, new Comparator<StrIntPair>()
				{
					public int compare(StrIntPair o1, StrIntPair o2)
					{
						if(Lib.GetTokenLength(o1.getString()) == Lib.GetTokenLength(o2.getString()))
						{
							return o1.getString().compareTo(o2.getString());
						}
						else
						{
							return Integer.compare(Lib.GetTokenLength(o2.getString()), Lib.GetTokenLength(o1.getString()));
						}
					}
				});
				break;
		}
		
		UpdateHasmMap();
	}
	
	private void UpdateHasmMap()
	{
		for(int i = 0 ; i < this.size() ; i++)
		{
			this.hmTerm.put(this.get(i).getString(), i);
		}
	}
	
	// --------------------
	
	public void loadStrFreqPair(StrIntPair o)
	{
		this.add(o);
	}
	
	public boolean equals(Object o)
	{
		String strString = ((StrIntPair) o).getString();
		if(s.equals(strString))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean isContain(String strString)
	{	
		if(this.isEmpty() || !this.hmTerm.containsKey(strString))
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	public void reset()
	{
		this.hmTerm.clear();
		this.clear();
	}
	
	public void removeElement(int iIndex)
	{
		this.hmTerm.remove(this.get(iIndex).s);		
		this.remove(iIndex);
	}
}
