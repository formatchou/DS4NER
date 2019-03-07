package Crawler;
import java.io.*;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.json.*;
import org.jsoup.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import Global.Lib;
import Global.Setting;
import java.util.*;
import java.net.URLEncoder;

public class GoogleSnippetCrawler
{
	static String strSeeds = "";
	static String strOutput_Dir = "";
	static File fSeeds;
	static File fOutput_Dir;
	static String strGgoogleUrl = "https://www.google.com.tw/search?q=";
	static String strAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36";
	
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
		opts.addOption(Option.builder("strOutput_Dir").desc("<Corpus\\Training\\>strOutput_Dir (directory)").required(true).hasArg().build());
		
		try
		{
			if(Setting.strType.toUpperCase().equals("IDE"))
			{
				fSeeds = Lib.SubFileOrFolder(Setting.dirCorpusTraining, strSeeds);
				fOutput_Dir = Lib.SubFileOrFolder(Setting.dirCorpusTraining, strOutput_Dir);
			}
			else
			{
				DefaultParser parser = new DefaultParser();
				CommandLine cmd = parser.parse(opts, args);
				
				fSeeds = Lib.SubFileOrFolder(Setting.dirCorpusTraining, cmd.getOptionValue("strSeeds"));
				fOutput_Dir = Lib.SubFileOrFolder(Setting.dirCorpusTraining, cmd.getOptionValue("strOutput_Dir"));
			}
			
			if(bParseArguments)
			{
				Setting.MyLog.info("fSeeds = " + fSeeds.getCanonicalPath());
				Setting.MyLog.info("fOutput_Dir = " + fOutput_Dir.getCanonicalPath());
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
		
		GoogleSnippetCrawler crawler = new GoogleSnippetCrawler()
                .setQueryURL("https://www.google.com.tw/search?hl=zh-TW&q=")
                .setWorkingFolder(fOutput_Dir.getAbsolutePath())
                .readNewSeeds(fSeeds.getAbsolutePath())
                .setUserAgent(strAgent)
                .setSleepTimeBase(4000)
                .create();
        crawler.run();
        
        while(crawler.searchQueue.size() > 0)
        {
        	Thread.sleep(1000);
        }
	}
	
	public GoogleSnippetCrawler create()
	{
		createWorkingFolder();
		if(hasNewSeeds)
			addSeeds(seedFilePath);
		return this;
	}

	private String workingFolder = "./temp";

	public GoogleSnippetCrawler setWorkingFolder(String path)
	{
		File folder = new File(path);
		this.workingFolder = folder.getAbsolutePath();
		return this;
	}

	private void createWorkingFolder()
	{
		File folder = new File(workingFolder + File.separatorChar + "error");
		folder.mkdirs();
		if(outputFolder == null)
			outputFolder = workingFolder + File.separatorChar + "result";
		folder = new File(outputFolder);
		folder.mkdirs();
	}

	public GoogleSnippetCrawler setQueryURL(String url)
	{
		this.strGgoogleUrl = url;
		return this;
	}

	private String userAgent = "Mozilla/5.0";

	public GoogleSnippetCrawler setUserAgent(String userAgent)
	{
		this.userAgent = userAgent;
		return this;
	}

	private long sleepTimeBase = 4000;
	private long sleepTime = sleepTimeBase;

	public GoogleSnippetCrawler setSleepTimeBase(long sleepTimeBase)
	{
		this.sleepTimeBase = sleepTimeBase;
		this.sleepTime = sleepTimeBase;
		return this;
	}

	private boolean hasNewSeeds = false;
	private String seedFilePath = "";

	public GoogleSnippetCrawler readNewSeeds(String path)
	{
		File folder = new File(path);
		if(folder.exists())
		{
			hasNewSeeds = true;
			seedFilePath = path;
		}
		else
		{
			System.out.println("Seed file doesn't exist. (" + path + ")");
		}
		return this;
	}

	private void addSeeds(String seedsFile)
	{
		String listFile = workingFolder + File.separatorChar + "query_list.txt";
		try
		{
			BufferedWriter queryList = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(listFile, true), "utf8"));
			BufferedReader newList = new BufferedReader(new InputStreamReader(new FileInputStream(seedsFile), "utf8"));
			Date date = new Date();
			String currentTime = Long.toString(date.getTime());
			int count = 1;
			while(newList.ready())
			{
				String input = newList.readLine();
				queryList.write(currentTime + "-" + Integer.toString(count) + " " + input + "\n");
				count++;
			}
			newList.close();
			queryList.flush();
			queryList.close();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

	private String outputFolder = null;

	public GoogleSnippetCrawler setOutputPath(String path)
	{
		outputFolder = path;
		return this;
	}

	private boolean skipCurrent = false;

	private void skipThis()
	{
		skipCurrent = true;
	}

	private boolean readKeyWords(Queue queue)
	{
		return true;
	}

	public void run()
	{
		fullUpQueue();
		do
		{
			for(int i = 0; i < searchQueue.size(); i++)
			{
				String[] splited = searchQueue.get(i).split(" ", 2);
				String id = splited[0];
				String query = splited[1];
				boolean accepted = false;
				long time = (new Date()).getTime();
				int errCount = -1;
				while(!accepted)
				{
					errCount++;
					bedTime();
					accepted = googleTheGod(query, id);
					if(!accepted)
						error = true;
				}
				time = (new Date()).getTime() - time;
				checkPointSave(id);
				totalError += errCount;
				String msgOutput = (new Date()).toString() + ": " + Float.toString((float) (time / 100) / 10) + "s : " + sleepTime + " " + message + "\t[" + id.split("-", 2)[1] + "] " + query;
				System.out.println(msgOutput);
				writeToLog(msgOutput);
				time = (new Date()).getTime();
				errCount = 0;
			}
			fullUpQueue();
		}
		while(searchQueue.size() > 0);
	}

	private ArrayList<String> searchQueue;

	private void fullUpQueue()
	{
		searchQueue = new ArrayList<String>();
		try
		{
			String inputListFile = workingFolder + File.separatorChar + "query_list.txt";
			BufferedReader listFile = new BufferedReader(new InputStreamReader(new FileInputStream(inputListFile), "utf8"));
			File checkPoint = new File(workingFolder + File.separatorChar + "checkPoint");
			if(checkPoint.exists())
			{
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(checkPoint), "utf8"));
				String checkPointId = br.readLine();
				br.close();
				while(listFile.ready())
				{
					String input = listFile.readLine();
					String[] splited = input.split(" ", 2);
					if(checkPointId.equals(splited[0]))
					{
						break;
					}
				}
			}
			int count = 0;
			while(listFile.ready() && count < 1000)
			{
				String input = listFile.readLine();
				searchQueue.add(input);
			}
			listFile.close();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

	private void checkPointSave(String checkPointId)
	{
		try
		{
			String checkPointFile = workingFolder + File.separatorChar + "checkPoint";
			BufferedWriter checkPoint = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(checkPointFile), "utf8"));
			checkPoint.write(checkPointId);
			checkPoint.flush();
			checkPoint.close();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

	class Data
	{
		String docTile;
		String url;
		String sneppit;
		String date;
	}

	private String message = "";

	private boolean googleTheGod(String query, String id)
	{
		try
		{
			String url = strGgoogleUrl + URLEncoder.encode(query, "UTF-8");
			Connection connection = Jsoup.connect(url);
			Document doc = connection.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36").followRedirects(true).get();
			Element searchElement = doc.getElementById("search");
			Elements nids = searchElement.select("#rso");
			JSONArray jsonArray = new JSONArray();
			for(int i = 0; i < nids.size(); i++)
			{
				Elements youtube = nids.get(i).select("._OKe");
				if(youtube.size() > 0)
				{
					Elements link = youtube.select("._PWc h3 a");
					if(link.size() > 0)
					{
						JSONObject data = new JSONObject();
						data.put("title", link.get(0).text());
						data.put("url", link.get(0).attr("href").toString());
						Elements text = youtube.select("._RBg");
						String snippet = "";
						if(text.size() > 0)
						{
							snippet = text.get(0).text().replaceAll("， 更多", "");
						}
						data.put("snippet", snippet);
						jsonArray.put(data);
					}
				}
				Elements results = nids.get(i).select(".g .rc");
				for(int j = 0; j < results.size(); j++)
				{
					Element re = results.get(j);
					JSONObject data = new JSONObject();
					data.put("title", re.select(".r a").get(0).text());
					data.put("url", re.select(".r a").get(0).attr("href").toString());
					Elements st = re.select(".s .st");
					String snippet = "";
					if(st.size() > 0)
						snippet = st.get(0).text();
					data.put("snippet", snippet);
					jsonArray.put(data);
				}
			}
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("query", query);
			jsonObj.put("date", (new Date()).toString());
			jsonObj.put("html", doc.html());
			jsonObj.put("data", jsonArray);
			saveFile("r:/gg.html", doc.html());
			if(jsonArray.length() == 0)
			{
				Element top = doc.getElementById("topstuff");
				if(top == null)
				{
					saveFile(workingFolder + File.separatorChar + "error" + File.separatorChar + id + "-" + (new Date()).toString() + ".txt", jsonObj.toString(2));
					return false;
				}
				String topText = top.text();
				if(!topText.contains("找不到符合搜尋字詞"))
				{
					saveFile(outputFolder + File.separatorChar + id, jsonObj.toString(2));
					return false;
				}
			}
			saveFile(outputFolder + File.separatorChar + id, jsonObj.toString(2));
			message = "(" + jsonArray.length() + ")";
			return true;
		}
		catch(Exception e)
		{
			String msgOutput = (new Date()).toString() + ": Error (" + query + ", " + id + ")\n    " + e.toString();
			System.out.println(msgOutput);
			writeToLog(msgOutput);
		}
		return false;
	}

	private void saveFile(String file, String text)
	{
		try
		{
			BufferedWriter outputFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf8"));
			outputFile.write(text);
			outputFile.flush();
			outputFile.close();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

	private void writeToLog(String msg)
	{
		try
		{
			String file = workingFolder + "/log.txt";
			BufferedWriter logFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "utf8"));
			logFile.write(msg + "\n");
			logFile.flush();
			logFile.close();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

	private boolean error = false;
	private int totalError = 0;
	private int adjustEvery = 10;
	private int sleepCount = 0;
	private int noErrorCount = 0;

	private void bedTime()
	{
		if(sleepTime < sleepTimeBase)
			sleepTime = sleepTimeBase;
		if(error)
		{
			error = false;
			long nextSleep = sleepTime * 600;
			System.out.println((new Date()).toString() + ": waiting for " + nextSleep / 60 / 1000 + " min");
			try
			{
				Thread.sleep(nextSleep * 600);
			}
			catch(Exception e)
			{
				System.out.println(e.getMessage());
			}
		}
		try
		{
			float ratio = 1.0f + (new Random()).nextFloat();
			Thread.sleep((long) (sleepTime * ratio));
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
		sleepCount++;
		if(sleepCount == adjustEvery)
		{
			/*
			 * if(totalError == 0) { noErrorCount++; } else { noErrorCount = 0;
			 * }
			 * 
			 * 
			 * if(noErrorCount > 5) { sleepTime *= 0.95; if(sleepTime < 4000)
			 * sleepTime = 4000; noErrorCount = 0; } sleepTime +=
			 * sleepTime*((float)(totalError)/(float)(adjustEvery-totalError+1))
			 * ; totalError = 0;
			 */
			sleepCount = 0;
			try
			{
				float ratio = 1.0f + (new Random()).nextFloat();;
				ratio = 5 * ratio;
				Thread.sleep((long) (sleepTime * ratio));
			}
			catch(Exception e)
			{
				System.out.println(e.getMessage());
			}
		}
	}
}
