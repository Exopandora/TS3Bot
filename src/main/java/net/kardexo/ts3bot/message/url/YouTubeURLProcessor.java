package net.kardexo.ts3bot.message.url;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.config.CookieSpecs;

import com.fasterxml.jackson.databind.JsonNode;

import net.kardexo.ts3bot.api.YouTube;
import net.kardexo.ts3bot.util.Util;

public class YouTubeURLProcessor extends DefaultURLProcessor
{
	private static final Pattern YOUTUBE_URL = Pattern.compile("https:\\/\\/(?:www\\.)?(?:youtube\\.com|youtu\\.be).*");
	private static final Pattern YOUTUBE_WATCH_URL = Pattern.compile("https:\\/\\/(?:www\\.)?youtube\\.com\\/watch\\?(.*)");
	private static final Pattern YOUTUBE_WATCH_URL_2 = Pattern.compile("https:\\/\\/(?:www\\.)?youtu\\.be\\/(.*)");
	
	@Override
	public String process(String url)
	{
		Matcher matcher = YOUTUBE_WATCH_URL.matcher(url);
		
		if(matcher.matches() && matcher.group(1) != null)
		{
			return this.watch(Util.queryToMap(matcher.group(1)));
		}
		
		matcher = YOUTUBE_WATCH_URL_2.matcher(url);
		
		if(matcher.matches() && matcher.group(1) != null)
		{
			return this.watch(Util.queryToMap("v=" + matcher.group(1)));
		}
		
		return super.process(url, CookieSpecs.IGNORE_COOKIES);
	}
	
	private String watch(Map<String, String> query)
	{
		String id = query.get("v");
		
		if(id != null)
		{
			try
			{
				long timestamp = YouTubeURLProcessor.parseTimestamp(query.get("t"));
				JsonNode video = YouTube.watch(id, timestamp);
				JsonNode snippet = video.path("snippet");
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
		return message != null && YOUTUBE_URL.matcher(message).matches();
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
