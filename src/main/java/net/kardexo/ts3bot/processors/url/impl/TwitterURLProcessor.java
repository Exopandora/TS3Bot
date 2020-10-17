package net.kardexo.ts3bot.processors.url.impl;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.processors.url.IURLProcessor;

public class TwitterURLProcessor implements IURLProcessor
{
	private static final String API_URL = "https://api.twitter.com/labs/2/";
	private static final Pattern TWITTER_URL = Pattern.compile("https:\\/\\/(www\\.)?twitter\\.com\\/([^/]+)/status/([0-9]+)");
	
	@Override
	public String process(String url)
	{
		String id = this.extractTweetId(url);
		
		if(id != null)
		{
			StringBuilder query = new StringBuilder(API_URL + "tweets");
			
			query.append("?ids=" + id);
			query.append("&user.fields=name");
			query.append("&expansions=author_id");
			
			HttpURLConnection connection = null;
			
			try
			{
				connection = (HttpURLConnection) new URL(query.toString()).openConnection();
				connection.setRequestProperty("Authorization", "Bearer " + TS3Bot.getInstance().getConfig().getApiTwitterBearerToken());
				connection.setConnectTimeout(5000);
				connection.connect();
				
				JsonNode node = new ObjectMapper().readTree(connection.getInputStream());
				JsonNode data = node.path("data");
				JsonNode users = node.path("includes").path("users");
				
				if(data.size() == 1 && users.size() == 1)
				{
					String text = data.get(0).path("text").asText();
					String user = users.get(0).path("name").asText();
					
					if(text != null && !text.isEmpty() && user != null && !user.isEmpty())
					{
						return user + ": " + text.substring(0, text.length() - 24);
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				connection.disconnect();
			}
		}
		
		return null;
	}
	
	@Override
	public boolean isApplicable(String message)
	{
		return message != null && TWITTER_URL.matcher(message).matches();
	}
	
	private String extractTweetId(String url)
	{
		Matcher matcher = TWITTER_URL.matcher(url);
		
		if(matcher.matches() && matcher.group(3) != null)
		{
			return matcher.group(3);
		}
		
		return null;
	}
}
