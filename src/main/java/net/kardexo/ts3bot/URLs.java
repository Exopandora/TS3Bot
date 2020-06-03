package net.kardexo.ts3bot;

import java.util.regex.Pattern;

public class URLs
{
	public static final Pattern URL_PATTERN = Pattern.compile("\\[URL\\].*\\[\\/URL\\]");
	public static final String QUERY_SPLIT = "\\?|&";
	
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
}
