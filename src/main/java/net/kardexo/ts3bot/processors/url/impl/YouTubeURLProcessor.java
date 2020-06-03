package net.kardexo.ts3bot.processors.url.impl;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.URLs;
import net.kardexo.ts3bot.processors.url.IURLProcessor;

public class YouTubeURLProcessor implements IURLProcessor
{
	private static final String API_URL = "https://www.googleapis.com/youtube/v3/";
	private static final Pattern YOUTUBE_URL = Pattern.compile("https:\\/\\/(www\\.)?youtube\\.com\\/watch\\?(.*)");
	private static final Pattern YOUTUBE_URL_2 = Pattern.compile("https:\\/\\/(www\\.)?youtu\\.be\\/(.*)");
	
	@Override
	public String process(String url)
	{
		String id = this.extractWatchId(url);
		
		if(id != null)
		{
			StringBuilder query = new StringBuilder(API_URL + "videos");
			
			query.append("?id=" + id);
			query.append("&part=snippet");
			query.append("&key=" + TS3Bot.getInstance().getConfig().getApiYouTube());
			
			try
			{
				JsonNode node = new ObjectMapper().readTree(new URL(query.toString()));
				JsonNode items = node.path("items");
				
				if(items.size() == 1)
				{
					String title = items.get(0).path("snippet").path("title").asText();
					
					if(title != null && !title.isEmpty())
					{
						return title;
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	@Override
	public boolean isApplicable(String message)
	{
		return message != null && (YOUTUBE_URL.matcher(message).matches() || YOUTUBE_URL_2.matcher(message).matches());
	}
	
	private String extractWatchId(String message)
	{
		Matcher matcher = YOUTUBE_URL.matcher(message);
		
		if(matcher.matches() && matcher.group(2) != null)
		{
			for(String split : matcher.group(2).split(URLs.QUERY_SPLIT))
			{
				if(split.startsWith("v="))
				{
					return split.substring(2);
				}
			}
		}
		
		matcher = YOUTUBE_URL_2.matcher(message);
		
		if(matcher.matches() && matcher.group(2) != null)
		{
			return matcher.group(2).split(URLs.QUERY_SPLIT)[0];
		}
		
		return null;
	}
}
