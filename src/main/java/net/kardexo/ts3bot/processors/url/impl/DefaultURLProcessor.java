package net.kardexo.ts3bot.processors.url.impl;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.core.util.IOUtils;

import net.kardexo.ts3bot.processors.url.IURLProcessor;

public class DefaultURLProcessor implements IURLProcessor
{
	private static final Pattern TITLE = Pattern.compile(".*<title>([^<>]*)</title>.*");
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.246";
	
	@Override
	public String process(String url)
	{
		try
		{
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestProperty("User-Agent", USER_AGENT);
			connection.setConnectTimeout(5000);
			connection.connect();
			
			if(connection.getContentType() != null && connection.getContentType().startsWith("text/html"))
			{
				Matcher matcher = TITLE.matcher(IOUtils.toString(new InputStreamReader(connection.getInputStream())));
				
				if(matcher.find())
				{
					return matcher.group(1);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	public boolean isApplicable(String url)
	{
		return true;
	}
}
