package net.kardexo.ts3bot.msgproc.url;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.utils.URIBuilder;

import com.fasterxml.jackson.databind.JsonNode;

import net.kardexo.ts3bot.TS3Bot;
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
			return this.watch(id, YouTubeURLProcessor.parseTimestamp(parameters.get("t")));
		}
		
		return null;
	}
	
	private String watch(String id, long timestamp)
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
					
					if(timestamp > 0)
					{
						builder.append(" [" + Util.formatDuration(timestamp) + "]");
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
	
	private static long parseTimestamp(String timestamp)
	{
		if(timestamp == null)
		{
			return -1;
		}
		
		if(timestamp.matches("\\d+"))
		{
			return YouTubeURLProcessor.parseUnsignedLong(timestamp);
		}
		
		long result = 0;
		
		for(int x = 0, head = 0; x < timestamp.length(); x++)
		{
			char c = timestamp.charAt(x);
			boolean nan = c < '0' || c > '9';
			
			if((x == 0 || x - head == 0) && nan)
			{
				return -1;
			}
			
			if(c == 's' || c == 'm' || c == 'h')
			{
				long value = YouTubeURLProcessor.parseUnsignedLong(timestamp.substring(head, x));
				
				if(value < 0)
				{
					return -1;
				}
				
				switch(c)
				{
					case 's':
						result += value;
						break;
					case 'm':
						result += value * 60;
						break;
					case 'h':
						result += value * 3600;
						break;
				}
				
				head = x + 1;
			}
			else if(nan || x == timestamp.length() - 1)
			{
				return -1;
			}
		}
		
		return result;
	}
	
	private static long parseUnsignedLong(String s)
	{
		try
		{
			return Long.parseUnsignedLong(s);
		}
		catch(NumberFormatException e)
		{
			return -1;
		}
	}
}
