package net.kardexo.ts3bot.messageprocessors.url.impl;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;

import org.apache.logging.log4j.core.util.IOUtils;
import org.jsoup.Jsoup;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.messageprocessors.url.IURLProcessor;
import net.kardexo.ts3bot.util.StringUtils;

public class DefaultURLProcessor implements IURLProcessor
{
	private static final String MIME_TYPE_TEXT_HTML = "text/html";
	
	@Override
	public String process(String url)
	{
		try
		{
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestProperty("User-Agent", TS3Bot.USER_AGENT);
			connection.setRequestProperty("Accept", MIME_TYPE_TEXT_HTML);
			connection.setRequestProperty("Accept-Charset", "UTF-8");
			connection.setConnectTimeout(5000);
			connection.connect();
			
			if(Arrays.stream(Objects.requireNonNullElse(connection.getContentType(), "").split(";")).map(String::trim).anyMatch(MIME_TYPE_TEXT_HTML::equals))
			{
				try
				{
					return Jsoup.parse(IOUtils.toString(new InputStreamReader(connection.getInputStream(), "UTF-8"))).getElementsByTag("title").first().text();
				}
				finally
				{
					connection.disconnect();
				}
			}
			else
			{
				connection.disconnect();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		try
		{
			String host = new URI(url).getHost();
			
			if(host != null)
			{
				return StringUtils.capitalize(host.replaceAll("www.", ""));
			}
			
			return host;
		}
		catch(URISyntaxException e)
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
