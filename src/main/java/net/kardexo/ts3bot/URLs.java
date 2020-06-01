package net.kardexo.ts3bot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLs
{
	private static final Pattern YOUTUBE_URL = Pattern.compile("\\[URL\\]https:\\/\\/(www\\.)?youtube\\.com\\/watch\\?(.*)\\[\\/URL\\]");
	private static final Pattern YOUTUBE_URL_2 = Pattern.compile("\\[URL\\]https:\\/\\/(www\\.)?youtu\\.be\\/(.*)\\[\\/URL\\]");
	private static final Pattern STEAM_URL = Pattern.compile("\\[URL\\]https?:\\/\\/([^\\.]+\\.)?(steamcommunity|steampowered)\\.[^ ]+\\[\\/URL\\]");
	private static final String QUERY_SPLIT = "\\?|&";
	
	public static String extract(String url)
	{
		if(url != null)
		{
			return url.substring(5, url.length() - 6);
		}
		
		return null;
	}
	
	public static String getYouTubeWatchId(String message)
	{
		Matcher matcher = YOUTUBE_URL.matcher(message);
		
		if(matcher.find() && matcher.group(2) != null)
		{
			for(String split : matcher.group(2).split(QUERY_SPLIT))
			{
				if(split.startsWith("v="))
				{
					return split.substring(2);
				}
			}
		}
		
		matcher = YOUTUBE_URL_2.matcher(message);
		
		if(matcher.find() && matcher.group(2) != null)
		{
			return matcher.group(2).split(QUERY_SPLIT)[0];
		}
		
		return null;
	}
	
	public static boolean isYouTube(String message)
	{
		return message != null && (YOUTUBE_URL.matcher(message).matches() || YOUTUBE_URL_2.matcher(message).matches());
	}
	
	public static boolean isSteam(String message)
	{
		return message != null && STEAM_URL.matcher(message).matches();
	}
}
