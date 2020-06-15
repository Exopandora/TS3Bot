package net.kardexo.ts3bot.processors.url.impl;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.logging.log4j.core.util.IOUtils;
import org.jsoup.Jsoup;

import net.kardexo.ts3bot.processors.url.IURLProcessor;

public class DefaultURLProcessor implements IURLProcessor
{
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.246";
	
	@Override
	public String process(String url)
	{
		try
		{
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestProperty("User-Agent", USER_AGENT);
			connection.setRequestProperty("Accept-Charset", "UTF-8"); 
			connection.setConnectTimeout(5000);
			connection.connect();
			
			return Jsoup.parse(IOUtils.toString(new InputStreamReader(connection.getInputStream(), "UTF-8"))).getElementsByTag("title").first().text();
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
