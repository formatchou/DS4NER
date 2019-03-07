package Crawler;
import Global.*;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import org.apache.commons.cli.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class WebContentCrawler
{
	static String strSeeds = "";
	static String strOutput = "";
	static File fSeeds;
	static File fOutput;
	
	static int iQueryInterval = 10000;							// ms, the interval time between two search
	static int iConnectTimeout = 5000;							// ms
	static int iQueryResults = 10;								// wish to get query results
	static int iFailRetry = 3;									// max Fail Count
	static String strBrowserAgent = "Mozilla/5.0 (Windows NT 6.1)";
	// &pws=0					don't ref. google account info,
	// &tbs=li:1					exact match
	// &lr=lang_zh-TW&hl=zh-TW	Language
	// &filter=0 				filter similar query results
	static String strQueryParameter = "&pws=0&tbs=li:1&lr=lang_zh-TW&hl=zh-TW&filter=1";
	static String strQuerySite = "https://tw.appledaily.com";	// limite query results on https://tw.appledaily.com
	
	public static void main(String[] args) throws Exception
	{	
		String strConfig = "";
		if(args.length > 0 && args[0].toUpperCase().startsWith("-CONFIG"))
		{
			strConfig = args[1];
		}
		
		if(Setting.Initialize(strConfig) && ParseArguments(args))
		{
			Start();
		}
		
		Setting.MyLog.info("========================= End =========================");
		Setting.MyLog.info("");
		System.gc();
		System.exit(0);
	}
	
	private static boolean ParseArguments(String[] args) throws Exception
	{
		Setting.MyLog.info("======================== Start ========================");
		Setting.MyLog.info("Now execute: " + System.getProperty("sun.java.command"));
		
		boolean bParseArguments = true;
		HelpFormatter hf = new HelpFormatter();
		
		Options opts = new Options();
		opts.addOption(Option.builder("h").longOpt("help").desc("Shows argument examples").build());
		opts.addOption(Option.builder("Config").required(false).hasArg().build());
		opts.addOption(Option.builder("strSeeds").desc("<Corpus\\Training\\>strSeeds.txt").required(true).hasArg().build());
		opts.addOption(Option.builder("strOutput").desc("<Corpus\\Training\\>strOutput.txt").required(true).hasArg().build());
		
		try
		{
			if(Setting.strType.toUpperCase().equals("IDE"))
			{
				fSeeds = Lib.SubFileOrFolder(Setting.dirCorpusTraining, strSeeds);
				fOutput = Lib.SubFileOrFolder(Setting.dirCorpusTraining, strOutput);
			}
			else
			{
				DefaultParser parser = new DefaultParser();
				CommandLine cmd = parser.parse(opts, args);
				
				fSeeds = Lib.SubFileOrFolder(Setting.dirCorpusTraining, cmd.getOptionValue("strSeeds"));
				fOutput = Lib.SubFileOrFolder(Setting.dirCorpusTraining, cmd.getOptionValue("strOutput"));
			}
			
			if(bParseArguments)
			{
				Setting.MyLog.info("fSeeds = " + fSeeds.getCanonicalPath());
				Setting.MyLog.info("fOutput = " + fOutput.getCanonicalPath());
			}
		}
		catch (Exception e)
		{
			bParseArguments = false;
			hf.printHelp("Argument examples", opts);
		}
		
		return bParseArguments;
	}
	
	private static void Start() throws Exception
	{
		Setting.MyLog.info("================= Main Function Start =================");
		
		ArrayList<String> alSeeds = Lib.LoadArrayList(fSeeds, "UTF-8", false);
		
		for(int i = 0; i < alSeeds.size(); i++)
		{
			long lStart = System.nanoTime();
			
			// Get Query Results from Search Engine
			ArrayList<String> alQueryResults = GetQueryResults(alSeeds.get(i));
			
			if(alQueryResults == null)
			{
				Setting.MyLog.warn("No Response!");
			}
			else if(alQueryResults.size() == 1 && alQueryResults.get(0) == "No Query Result!")
			{
				Setting.MyLog.info("Seed:" + alSeeds.get(i) + " No Query Results!");
			}
			else
			{	
				Setting.MyLog.info("Now start to extract sentences that contain Keyword:" + alSeeds.get(i));
				
				for(int j = 0; j < alQueryResults.size(); j++)
				{	
					Setting.MyLog.info("Url = " + alQueryResults.get(j));
					ArrayList<String> alOutput = Extraction(alQueryResults.get(j), alSeeds.get(i));
					
					if(alOutput != null)
					{
						Lib.SaveFile(alOutput, fOutput, true);
					}
				}
			}
			
			// Used time < iQueryInterval → sleep (QueryInterval - Used time)
			// Used time ≧ iQueryInterval → start to query next keyword
			int iSleep = iQueryInterval - (int) ((System.nanoTime() - lStart) / 1e6);
			
			if(iSleep > 0)
			{
				Setting.MyLog.info("Now Sleep " + iSleep + "ms\r\n");
				Thread.sleep(iSleep);
			}
			else
			{
				Setting.MyLog.info("Don't need sleep!\r\n");
			}
			
			if(i > 0 && (i % 1000 == 0 || i == alSeeds.size() - 1))
			{
				Setting.MyLog.info(Lib.GetProgress(i, alSeeds.size()));
			}
		}
	}
	
	private static ArrayList<String> GetQueryResults(String strKeyword) throws Exception
	{
		ArrayList<String> alUrl = new ArrayList<String>();
		
		for(int i = 0; i < iFailRetry; i++)
		{
			alUrl = ParseQueryResult(strKeyword);
			
			if(alUrl != null)
			{
				break;
			}
			else
			{
				Setting.MyLog.info("Can't get query results, sleep 1-5 sec and retry!");
				Thread.sleep(Lib.GetRandom(1, 3) * 1000); // Sleep 1-3 sec
			}
		}
		
		if(alUrl.size() > 0)
		{
			return alUrl;
		}
		else
		{
			Lib.SaveFile(strKeyword, Lib.SubFileOrFolder(Setting.dirCorpusTraining, "Retry.txt"), true);
			return null;
		}
	}
	
	private static ArrayList<String> ParseQueryResult(String strKeyword) throws Exception
	{	
		ArrayList<String> alUrl = new ArrayList<String>();
		String strQueryString = ReturnQueryString(strKeyword);
		Setting.MyLog.info("Keyword:" + strKeyword + ", Url = " + strQueryString);
		
		try
		{	
			URL url = new URL(strQueryString);
			URLConnection URLConn = (HttpURLConnection) url.openConnection();      	        
			URLConn.setRequestProperty("User-agent", strBrowserAgent);
			Document doc = Jsoup.parse(URLConn.getInputStream(), "UTF-8", strQueryString);
			
			Elements elements = doc.getElementsByTag("h3").select("a[href]");
			
			for(Element e:elements)
			{
				String strUrl = e.outerHtml().replace("<a href=\"/url?q=", "");
				strUrl = strUrl.substring(0,strUrl.indexOf("&amp;"));	
				alUrl.add(strUrl);
			}
			
			if(alUrl.size() == 0)
			{
				alUrl.add("No Query Result!");
			}
			return alUrl;
		}
		catch(Exception e)
		{
			if(e.toString().contains(", URL="))
			{
				Setting.MyLog.info("Exception : " + e.toString().substring(0, e.toString().indexOf(", URL=")));
			}
			else
			{
				Setting.MyLog.info("Exception : " + e.toString());
			}
			
			return null;
		}
	}
	
	private static String ReturnQueryString(String strKeyWord) throws Exception
	{
		StringBuilder sbQueryString = new StringBuilder();
		sbQueryString.append("http://www.google.com/search?q=");		// have to contain as_epq
		sbQueryString.append(URLEncoder.encode(strKeyWord,"UTF-8"));
		sbQueryString.append("+site:" + strQuerySite);					// limite query results
		sbQueryString.append("&ie=utf-8&oe=utf-8");						// input/output encoding
		sbQueryString.append("&num=" + String.valueOf(iQueryResults));	// query result count
		sbQueryString.append(strQueryParameter);
		
		return sbQueryString.toString();
	}
	
	public static Boolean CheckContentType(String strUrl)
	{
		strUrl = strUrl.toLowerCase();
		if(!strUrl.startsWith("http"))
		{
			return false;
		}
		
		ArrayList<String> alSuffix = new ArrayList<String>();
		alSuffix.add("pdf");
		alSuffix.add("doc");
		alSuffix.add("docx");
		alSuffix.add("xls");
		alSuffix.add("xlsx");
		alSuffix.add("ppt");
		alSuffix.add("pptx");
		alSuffix.add("rar");
		alSuffix.add("zip");
		
		for(String s:alSuffix)
		{
			if(strUrl.endsWith(s))
			{
				return false;
			}
		}
		
		return true;
	}

	public static ArrayList<String> Extraction(String strUrl, String strKeyword) throws Exception
	{
		try
		{
			ArrayList<String> alOutput = new ArrayList<String>();
			Document document = Jsoup.connect(strUrl).userAgent(strBrowserAgent).followRedirects(true).get();
			
			// ----------
			
			document.body().traverse(new NodeVisitor()
			{
				@Override public void head(Node node, int depth)
				{
					if(node instanceof TextNode)
					{
						String strNodeText = ((TextNode) node).text().trim();
						
						if(strNodeText.contains(strKeyword))
						{
							alOutput.add(strNodeText);
						}
					}
				}

				@Override public void tail(Node node, int depth)
				{
				}
			});
			
			return alOutput;
		}
		catch(Exception e)
		{
			// do something
			return null;
		}
	}
}
