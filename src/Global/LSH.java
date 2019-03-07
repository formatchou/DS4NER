package Global;
import java.util.*;

public class LSH
{
	// Parameters
	private ArrayList<String> alSeeds = new ArrayList<String>();
	private ArrayList<Integer>[] alPermutation;
	private HashMap<String, Integer> hmTerms;	// Term-ID pair
	private HashSet<Integer>[][] hsBucket;
	private int[][] arraySig_Seeds;
	private int iSeeds = 0;
	private int iTokens = 0;
	private int iKShingle = 1;
	private int iBand = 20;					// b, the number of bands
	private int iRow = 2;
	private int iPermutation = 0;
	private int iBucketShrink = 2;				// Each Bucket Length = Tokens/this_value
	private int iBucketLength = 0;
	private boolean bLSH = false;				// Does LSH build success?
	
	public LSH(StrFloatPair pairSeeds, int iKShingle, int iBand, int iRow) throws Exception
	{
		for(StrFloatPair pair:pairSeeds)
		{
			this.alSeeds.add(pair.getString().toUpperCase());
		}
		this.iSeeds = this.alSeeds.size();
		
		if(this.iSeeds == 0)
		{
			Setting.MyLog.info("No seeds, skip to build LSH");
			return;
		}
		
		// LSH Step1: Shingling
		String strTerm = "";
		ArrayList<String> alTokens = new ArrayList<String>();
		HashSet<String> hsTerms = new HashSet<String>();
		this.iKShingle = iKShingle;
		this.hmTerms = new HashMap<String, Integer>(); // Term-ID pair
		
		// Parse training data and seeds to build <Term-ID> pairs via hmTerms
		for(int i = 0 ; i < this.alSeeds.size() ; i++)
		{
			alTokens = Lib.SentenceToToken(this.alSeeds.get(i));
			
			if(alTokens.size() <= this.iKShingle)
			{
				strTerm = Lib.DuplicateSymbols(this.alSeeds.get(i));
				hsTerms.add(strTerm);
				strTerm = "";
			}
			else
			{
				for(int j = 0 ; j < alTokens.size() - this.iKShingle + 1 ; j++)
				{
					for(int k = j; k < j + this.iKShingle; k++)
					{
						strTerm += Lib.DuplicateSymbols(alTokens.get(k));
					}
					
					hsTerms.add(strTerm);
					strTerm = "";
				}
			}
		}
		
		ArrayList<String> alTerms = new ArrayList<String>(hsTerms);
		Collections.shuffle(alTerms);
		for(int i = 0 ; i < alTerms.size() ; i++)
		{	
			this.hmTerms.put(alTerms.get(i), i); // Term-ID pair
		}
		
		this.iTokens = alTerms.size(); // how many k-shingle (k-gram) tokens in corpus
		
		this.iBand = iBand;
		this.iRow = iRow;
		this.iPermutation = this.iBand * this.iRow;
		this.iBucketLength = Math.floorDiv(this.iTokens, this.iBucketShrink);
		
		Setting.MyLog.info("iSeeds = " + this.iSeeds);
		Setting.MyLog.info("iTokens = " + this.iTokens);
		Setting.MyLog.info("iKShingle = " + this.iKShingle);
		Setting.MyLog.info("iBand = " + this.iBand);
		Setting.MyLog.info("iRow = " + this.iRow);
		Setting.MyLog.info("iPermutation = " + this.iPermutation);
		Setting.MyLog.info("iBucketShrink = " + this.iBucketShrink);
		Setting.MyLog.info("iBucketLength = " + this.iBucketLength);
		
		// ----------
		
		// Transfer seeds to one-hot embedding
		HashSet<Integer>[] hsSeeds = new HashSet[this.iSeeds];
		
		for(int i = 0; i < this.iSeeds ; i++)
		{
			hsSeeds[i] = new HashSet<Integer>();
			alTokens = Lib.SentenceToToken(this.alSeeds.get(i));
			
			if(alTokens.size() == 1)
			{
				strTerm = alTokens.get(0);
				if(this.hmTerms.containsKey(strTerm))
				{
					hsSeeds[i].add(this.hmTerms.get(strTerm));
				}
				strTerm = "";
			}
			else
			{
				for(int j = 0 ; j < alTokens.size() - this.iKShingle + 1 ; j++)
				{
					for(int k = j; k < j + this.iKShingle; k++)
					{
						strTerm += alTokens.get(k);
					}
					
					if(this.hmTerms.containsKey(strTerm))
					{
						hsSeeds[i].add(this.hmTerms.get(strTerm));
					}
					strTerm = "";
				}
			}
		}
		
		// ----------
		
		alTokens.clear();
		alTokens.trimToSize();
		alTerms.clear();
		alTerms.trimToSize();
		hsTerms.clear();
		
		// LSH Step2: MinHashing done
		// Create permutation matrix
		alPermutation = new ArrayList[iPermutation];
		
		for(int i = 0; i < iPermutation; i++)
		{
			alPermutation[i] = new ArrayList<Integer>();
			
			if(i == 0) // Insert all elements to alPermutation[0], each element = index
			{
				for(int j = 0; j < iTokens ; j++)
				{
					alPermutation[i].add(j);
				}
			}
			else
			{
				// clone from first arraylist then shuffle
				alPermutation[i] = (ArrayList<Integer>) alPermutation[0].clone();
			}
			
			Collections.shuffle(alPermutation[i]);
		}
		
		// ----------
		
		// Build signature matrix of seeds
		this.arraySig_Seeds = new int[this.iSeeds][iPermutation];
		
		for(int i = 0; i < this.iSeeds; i++) // Seed_ID (x) of arraySig_Seeds
		{	
			for(int j = 0; j < iPermutation; j++) // iPermutation (y) of arraySig_Seeds
			{ 
				this.arraySig_Seeds[i][j] = getSignature(hsSeeds[i], j);
			}
		}
		
		for(int i = 0; i < this.iSeeds; i++)
		{
			hsSeeds[i].clear();
		}
		
		// ----------
		// Build Bucket Hash Table
		
		int iBucket_ID = 0;
		int iIndex_Row_Start = 0;
		int iIndex_Row_End = 0;
		int iSum = 0;
		this.hsBucket = new HashSet[this.iBand][this.iBucketLength];
		
		for(int i = 0; i < this.iSeeds; i++)
		{	
			for(int j = 0; j < this.iBand; j++)
			{
				iIndex_Row_Start = j * this.iRow;
				iIndex_Row_End = (j + 1) * this.iRow - 1;
				for(int k = iIndex_Row_Start; k <= iIndex_Row_End; k++) // xx
				{
					iSum += this.arraySig_Seeds[i][k];
				}
				
				// for row <iKShingle Pairs, Seed ID> = <100-200-300, 123>
				iBucket_ID = Math.floorMod(iSum, this.iBucketLength);
				if(this.hsBucket[j][iBucket_ID] == null)
				{
					this.hsBucket[j][iBucket_ID] = new HashSet<Integer>();
				}
				
				this.hsBucket[j][iBucket_ID].add(i);
				iSum = 0;
			}
			
			if(i > 0 && (i % 100000 == 0 || i == this.iSeeds - 1))
			{
				Setting.MyLog.info(Lib.GetProgress(i, this.iSeeds));
			}
		}
		
		// LSH Step3: Build Bucket Hash Table done
		bLSH = true;
	}
	
	private int getSignature(HashSet<Integer> hsInput, int iPermutation_ID) // Calculate signature value 
	{
		int iInput_Index = -1;
		int iPermutation_Value = this.iTokens;
		
		for(int iToken_ID:hsInput)
		{
			if(this.alPermutation[iPermutation_ID].get(iToken_ID) < iPermutation_Value)
			{
				iPermutation_Value = this.alPermutation[iPermutation_ID].get(iToken_ID); 
				iInput_Index = iToken_ID;
			}
		}
		
		return iInput_Index;
	}
	
	public StrFloatPair getCandidatePairs(String strSentence)
	{	
		HashSet<String> hsCandidatePairs = new HashSet<String>();
		StrFloatPair pairSeeds = new StrFloatPair();
		
		if(bLSH)
		{
			// Transfer sentence → tokens → k-shingles
			ArrayList<String> alTokens = Lib.SentenceToToken(strSentence.toUpperCase());
			HashSet<Integer> hsSentence = new HashSet<Integer>(); // one-hot embedding
			
			if(alTokens.size() == 1)
			{
				if(this.hmTerms.containsKey(Lib.DuplicateSymbols(alTokens.get(0))))
				{
					hsSentence.add(this.hmTerms.get(Lib.DuplicateSymbols(alTokens.get(0))));
				}
			}
			else
			{	
				String strTerm = "";
				for(int i = 0 ; i < alTokens.size() - this.iKShingle + 1 ; i++)
				{
					for(int j = i; j < i + this.iKShingle; j++)
					{
						strTerm += Lib.DuplicateSymbols(alTokens.get(j));
					}
					
					if(this.hmTerms.containsKey(strTerm))
					{
						hsSentence.add(this.hmTerms.get(strTerm));
					}
					strTerm = "";
				}
			}
			
			// ----------
			
			if(hsSentence.size() > 0)
			{
				// Build signature vector of sentence
				int[] arraySig_Sentence = new int[this.iPermutation];
				
				for(int i = 0; i < this.iPermutation; i++)
				{	
					arraySig_Sentence[i] = getSignature(hsSentence, i);
				}
				
				// Find Candidate Pairs
				int iBucket_ID = 0;
				int iIndex_Row_Start = 0;
				int iIndex_Row_End = 0;
				int iSum = 0;
				
				for(int i = 0; i < this.iBand; i++)
				{
					iIndex_Row_Start = i * this.iRow;
					iIndex_Row_End = (i + 1) * this.iRow - 1;
					
					for(int j = iIndex_Row_Start; j <= iIndex_Row_End; j++)
					{ 
						iSum += arraySig_Sentence[j]; 
					}
					
					iBucket_ID = Math.floorMod(iSum, this.iBucketLength);
					
					// for row <iKShingle Pairs, Seed ID> = <100-200-300, 123>
					if(this.hsBucket[i][iBucket_ID] != null)
					{
						for(int id:this.hsBucket[i][iBucket_ID])
						{	
							hsCandidatePairs.add(this.alSeeds.get(id));
						}
					}
					
					iSum = 0;
				}
			}
		}
		
		for(String s:hsCandidatePairs)
		{
			pairSeeds.addString(s);
		}
		
		pairSeeds.sortByTokenLength(StrFloatPair.SortBy.DESC);
		
		return pairSeeds;
	}
}
