package Global;
import java.io.*;

public class CommandStream extends Thread
{
	InputStream isOutput;
	String strType;

	CommandStream(InputStream is, String type)
	{
		this.isOutput = is;
		this.strType = type;
	}

	public void run()
	{
		try
		{
			InputStreamReader isr = new InputStreamReader(isOutput);
			BufferedReader br = new BufferedReader(isr);
			String strLine = null;
			
			while((strLine = br.readLine()) != null)
			{
				System.out.println(strType + ": " + strLine);
			}
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
}