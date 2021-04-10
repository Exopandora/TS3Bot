package net.kardexo.ts3bot.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import net.kardexo.ts3bot.TS3Bot;

public class Util
{
	public static final Pattern URL_PATTERN = Pattern.compile("\\[URL\\].*\\[\\/URL\\]");
	public static final String QUERY_SPLIT = "\\?|&";
	public static final Pattern PARAMETER_PATTERN = Pattern.compile("([^=]+)=([^=]+)");
	
	private static boolean isURL(String url)
	{
		return url != null && URL_PATTERN.matcher(url).matches();
	}
	
	public static String extract(String url)
	{
		if(url != null && Util.isURL(url))
		{
			return url.substring(5, url.length() - 6);
		}
		
		return null;
	}
	
	public static String wrap(String url)
	{
		return "[URL]" + url + "[/URL]";
	}
	
	public static Map<String, String> queryToMap(String query)
	{
		Map<String, String> result = new HashMap<String, String>();
		
		for(String parameter : query.split(Util.QUERY_SPLIT))
		{
			Matcher matcher = PARAMETER_PATTERN.matcher(parameter);
			
			if(matcher.matches())
			{
				result.put(matcher.group(1), matcher.group(2));
			}
		}
		
		return result;
	}
	
	public static String formatDuration(long gameDuration)
	{
		long seconds = gameDuration % 60;
		long minutes = (gameDuration % 3600) / 60;
		long hours = gameDuration / 3600;
		
		if(hours > 0)
		{
			return String.format("%d:%02d:%02d", hours, minutes, seconds);
		}
		
		return String.format("%02d:%02d", minutes, seconds);
	}	
	
	public static String repeat(CharSequence sequence, int repetitions)
	{
		StringBuilder builder = new StringBuilder();
		
		for(int x = 0; x < repetitions; x++)
		{
			builder.append(sequence);
		}
		
		return builder.toString();
	}
	
	public static CloseableHttpClient httpClient()
	{
		RequestConfig config = RequestConfig.custom()
				.setConnectionRequestTimeout(5000)
				.setCookieSpec(CookieSpecs.STANDARD_STRICT)
				.build();
		CloseableHttpClient client = HttpClientBuilder.create()
				.setUserAgent(TS3Bot.USER_AGENT)
				.setDefaultRequestConfig(config)
				.build();
		return client;
	}
}
