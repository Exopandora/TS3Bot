package net.kardexo.ts3bot.processors.url.impl;

import java.net.URL;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.processors.url.IURLProcessor;
import net.kardexo.ts3bot.util.StringUtils;
import net.kardexo.ts3bot.util.URLs;

public class YouTubeURLProcessor implements IURLProcessor
{
	private static final String API_URL = "https://www.googleapis.com/youtube/v3/";
	private static final Pattern YOUTUBE_URL = Pattern.compile("https:\\/\\/(www\\.)?youtube\\.com\\/watch\\?(.*)");
	private static final Pattern YOUTUBE_URL_2 = Pattern.compile("https:\\/\\/(www\\.)?youtu\\.be\\/(.*)");
	
	@Override
	public String process(String url)
	{
		Map<String, String> parameters = this.extractQuery(url);
		String id = parameters.get("v");
		
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
					JsonNode snippet = items.get(0).path("snippet");
					String channelTitle = snippet.path("channelTitle").asText();
					String title = snippet.path("title").asText();
					
					if(channelTitle != null && !channelTitle.isEmpty() && title != null && !title.isEmpty())
					{
						StringBuilder builder = new StringBuilder(channelTitle + ": \"" + title + "\"");
						
						if(parameters.containsKey("t"))
						{
							builder.append(" [" + StringUtils.formatDuration(Long.parseLong(parameters.get("t"))) + "]");
						}
						
						return builder.toString();
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
	
	private Map<String, String> extractQuery(String url)
	{
		Matcher matcher = YOUTUBE_URL.matcher(url);
		
		if(matcher.matches() && matcher.group(2) != null)
		{
			return URLs.queryToMap(matcher.group(2));
		}
		
		matcher = YOUTUBE_URL_2.matcher(url);
		
		if(matcher.matches() && matcher.group(2) != null)
		{
			return URLs.queryToMap("v=" + matcher.group(2));
		}
		
		return null;
	}
}
