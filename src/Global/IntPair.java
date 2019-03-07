package Global;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class IntPair extends ArrayList<IntPair>
{
	private int id = 0;	// sentence id
	private int s = 0;		// start index
	private int e = 0;		// end index
	private HashMap<Integer, Integer> hmTerm;
	
	public IntPair()
	{
		this.hmTerm = new HashMap<Integer, Integer>();
	}
	
	public IntPair(int iID, int iStart, int iEnd)
	{
		id = iID;
		s = iStart;
		e = iEnd;
	}
	
	// --------------------
	
	public void addPair(int iID, int iStart, int iEnd)
	{
		if(this.isEmpty() || !this.hmTerm.containsKey(iID))
		{	
			this.add(new IntPair(iID, iStart, iEnd));
			this.hmTerm.put(iID, this.size() - 1);
		}
		else
		{
			int iIndex = this.hmTerm.get(iID);
			this.get(iIndex).s = iStart;
			this.get(iIndex).e = iEnd;
		}
	}
	
	public int getID()
	{
		return id;
	}
	
	public int getStart()
	{
		return s;
	}

	public int getEnd()
	{
		return e;
	}
	
	public String getIntPair()
	{
		return String.valueOf(id) + Setting.strSeparator_Tab + String.valueOf(s) + Setting.strSeparator_Tab + String.valueOf(e);
	}
	
	public void sortRandom()
	{
		Collections.shuffle(this, new Random(System.nanoTime()));
		UpdateHasmMap();
	}
	
	private void UpdateHasmMap()
	{
		for(int i = 0 ; i < this.size() ; i++)
		{
			this.hmTerm.put(this.get(i).getID(), i);
		}
	}
	
	// --------------------
	
	public boolean equals(Object o)
	{
		if(id == ((IntPair) o).getID() && s == ((IntPair) o).getStart() && e == ((IntPair) o).getEnd())
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean isContain(int iID, int iStart, int iEnd)
	{
		if(this.isEmpty() || !this.hmTerm.containsKey(iID))
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
		this.hmTerm.remove(this.get(iIndex).id);
		this.remove(iIndex);
	}
}
