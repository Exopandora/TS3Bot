package net.kardexo.ts3bot.messageprocessors.url.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.utils.URIBuilder;

import com.fasterxml.jackson.databind.JsonNode;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.messageprocessors.url.IURLProcessor;
import net.kardexo.ts3bot.util.Util;

public class YouTubeURLProcessor implements IURLProcessor
{
	private static final URI API_URL = URI.create("https://www.googleapis.com/youtube/v3/");
	private static final Pattern YOUTUBE_URL = Pattern.compile("https:\\/\\/(www\\.)?youtube\\.com\\/watch\\?(.*)");
	private static final Pattern YOUTUBE_URL_2 = Pattern.compile("https:\\/\\/(www\\.)?youtu\\.be\\/(.*)");
	
	@Override
	public String process(String url)
	{
		Map<String, String> parameters = this.extractQuery(url);
		String id = parameters.get("v");
		
		if(id != null)
		{
			return this.watch(id, parameters.get("t"));
		}
		
		return null;
	}
	
	private String watch(String id, String timestamp)
	{
		try
		{
			URI uri = new URIBuilder(API_URL.resolve("videos"))
				.addParameter("id", id)
				.addParameter("part", "snippet")
				.addParameter("key", TS3Bot.getInstance().getApiKeyManager().requestKey(TS3Bot.API_KEY_YOUTUBE))
				.build();
			
			JsonNode node = TS3Bot.getInstance().getObjectMapper().readTree(uri.toURL());
			JsonNode items = node.path("items");
			
			if(items.size() == 1)
			{
				JsonNode snippet = items.get(0).path("snippet");
				String channelTitle = snippet.path("channelTitle").asText();
				String title = snippet.path("title").asText();
				
				if(channelTitle != null && !channelTitle.isEmpty() && title != null && !title.isEmpty())
				{
					StringBuilder builder = new StringBuilder(channelTitle + ": \"" + title + "\"");
					
					if(timestamp != null)
					{
						builder.append(" [" + Util.formatDuration(Long.parseLong(timestamp)) + "]");
					}
					
					return builder.toString();
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
	public boolean isApplicable(String message)
	{
		return message != null && (YOUTUBE_URL.matcher(message).matches() || YOUTUBE_URL_2.matcher(message).matches());
	}
	
	private Map<String, String> extractQuery(String url)
	{
		Matcher matcher = YOUTUBE_URL.matcher(url);
		
		if(matcher.matches() && matcher.group(2) != null)
		{
			return Util.queryToMap(matcher.group(2));
		}
		
		matcher = YOUTUBE_URL_2.matcher(url);
		
		if(matcher.matches() && matcher.group(2) != null)
		{
			return Util.queryToMap("v=" + matcher.group(2));
		}
		
		return new HashMap<String, String>();
	}
}
