package Global;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import Global.StrIntPair.SortBy;

public class StrFloatPair extends ArrayList<StrFloatPair>
{
	private String s = "";
	private float f = 0.0f;
	private HashMap<String, Integer> hmTerm;
	
	public enum SortBy
	{
		ASC, DESC
	}
	
	public StrFloatPair()
	{
		this.hmTerm = new HashMap<String, Integer>();
	}
	
	public StrFloatPair(String strString, Float fFloat)
	{
		s = strString;
		f = fFloat;
	}
	
	// --------------------

	private StrFloatPair(String strString)
	{	
		s = strString;
	}
	
	private void addFreq(Float fFloat)
	{
		f = f + fFloat;
	}
	
	// --------------------
	
	public void addString(String strString)
	{
		if(this.isEmpty() || !this.hmTerm.containsKey(strString))
		{	
			this.add(new StrFloatPair(strString, 0.0f));
			this.hmTerm.put(strString, this.size() - 1);
		}
		else
		{
			int iIndex = this.hmTerm.get(strString);
			this.get(iIndex).f = 0.0f;
		}
	}	
	
	public void addPair(String strString, Float fFloat)
	{
		if(this.isEmpty() || !this.hmTerm.containsKey(strString))
		{	
			this.add(new StrFloatPair(strString, fFloat));
			this.hmTerm.put(strString, this.size() - 1);
		}
		else
		{
			int iIndex = this.hmTerm.get(strString);
			this.get(iIndex).f += fFloat;
		}
	}
	
	public void setPair(String strString, Float fFloat)
	{
		if(this.hmTerm.containsKey(strString))
		{
			int iIndex = this.hmTerm.get(strString);
			
			if(this.get(iIndex).getString() == strString)
			{
				this.get(iIndex).f = fFloat;
			}
		}
	}
	
	public void setFloat(Float fFloat)
	{
		f = fFloat;
	}
	
	public String getString()
	{
		return s;
	}

	public float getFloat()
	{
		return f;
	}
	
	public float getFloat(String strString)
	{
		int iIndex = this.hmTerm.get(strString);
		if(iIndex != -1)
		{
			return this.get(iIndex).getFloat();
		}
		else
		{
			return 0;
		}
	}
	
	public String getStrFloatPair()
	{	
		return s + Setting.strSeparator_Tab + f;
	}
		
	// --------------------
	
	public void sortByStr(SortBy sort)
	{
		switch(sort)
		{
			case ASC:
				Collections.sort(this, new Comparator<StrFloatPair>()
				{
					public int compare(StrFloatPair o1, StrFloatPair o2)
					{
						return o1.getString().compareTo(o2.getString());
					}
				});
				break;
			case DESC:
				Collections.sort(this, new Comparator<StrFloatPair>()
				{
					public int compare(StrFloatPair o1, StrFloatPair o2)
					{
						return o2.getString().compareTo(o1.getString());
					}
				});
				break;
		}
		
		UpdateHasmMap();
	}
	
	public void sortByFloat(SortBy sort)
	{	
		switch(sort)
		{
			case ASC:
				Collections.sort(this, new Comparator<StrFloatPair>()
				{
					public int compare(StrFloatPair o1, StrFloatPair o2)
					{	
						if(o1.getFloat() == o2.getFloat())
						{
							return o1.getString().compareTo(o2.getString());
						}
						else
						{
							return Float.compare(o1.getFloat(), o2.getFloat());
						}
					}
				});
				break;
			case DESC:
				Collections.sort(this, new Comparator<StrFloatPair>()
				{
					public int compare(StrFloatPair o1, StrFloatPair o2)
					{
						if(o1.getFloat() == o2.getFloat())
						{
							return o1.getString().compareTo(o2.getString());
						}
						else
						{
							return Float.compare(o2.getFloat(), o1.getFloat());
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
				Collections.sort(this, new Comparator<StrFloatPair>()
				{
					public int compare(StrFloatPair o1, StrFloatPair o2)
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
				Collections.sort(this, new Comparator<StrFloatPair>()
				{
					public int compare(StrFloatPair o1, StrFloatPair o2)
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
				Collections.sort(this, new Comparator<StrFloatPair>()
				{
					public int compare(StrFloatPair o1, StrFloatPair o2)
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
				Collections.sort(this, new Comparator<StrFloatPair>()
				{
					public int compare(StrFloatPair o1, StrFloatPair o2)
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
	
	public void sortByFloatAndTokenLength(SortBy sort)
	{	
		switch(sort)
		{
			case ASC:
				Collections.sort(this, new Comparator<StrFloatPair>()
				{
					public int compare(StrFloatPair o1, StrFloatPair o2)
					{	
						if(o1.getFloat() == o2.getFloat())
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
						else
						{
							return Float.compare(o1.getFloat(), o2.getFloat());
						}
					}
				});
				break;
			case DESC:
				Collections.sort(this, new Comparator<StrFloatPair>()
				{
					public int compare(StrFloatPair o1, StrFloatPair o2)
					{
						if(o1.getFloat() == o2.getFloat())
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
						else
						{
							return Float.compare(o2.getFloat(), o1.getFloat());
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
	
	public boolean equals(Object o)
	{
		String strString = ((StrFloatPair) o).getString();
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
