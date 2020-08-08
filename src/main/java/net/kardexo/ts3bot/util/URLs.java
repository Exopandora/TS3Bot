package net.kardexo.ts3bot.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLs
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
		if(url != null && URLs.isURL(url))
		{
			return url.substring(5, url.length() - 6);
		}
		
		return null;
	}
	
	public static Map<String, String> queryToMap(String query)
	{
		Map<String, String> result = new HashMap<String, String>();
		
		for(String parameter : query.split(URLs.QUERY_SPLIT))
		{
			Matcher matcher = PARAMETER_PATTERN.matcher(parameter);
			
			if(matcher.matches())
			{
				result.put(matcher.group(1), matcher.group(2));
			}
		}
		
		return result;
	}
}
