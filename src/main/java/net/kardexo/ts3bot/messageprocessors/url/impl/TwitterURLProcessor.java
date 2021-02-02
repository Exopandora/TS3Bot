package net.kardexo.ts3bot.messageprocessors.url.impl;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.messageprocessors.url.IURLProcessor;

public class TwitterURLProcessor implements IURLProcessor
{
	private static final String API_URL = "https://api.twitter.com/labs/2/";
	private static final Pattern TWITTER_STATUS_URL = Pattern.compile("https:\\/\\/(www\\.)?twitter\\.com\\/([^/]+)\\/status\\/([0-9]+).*");
	private static final Pattern TWITTER_PROFILE_URL = Pattern.compile("https:\\/\\/(www\\.)?twitter\\.com\\/([^/]+)");
	
	@Override
	public String process(String url)
	{
		if(TWITTER_STATUS_URL.matcher(url).matches())
		{
			return this.status(url);
		}
		else if(TWITTER_PROFILE_URL.matcher(url).matches())
		{
			return this.profile(url);
		}
		
		return null;
	}
	
	@Override
	public boolean isApplicable(String message)
	{
		return message != null && (TWITTER_STATUS_URL.matcher(message).matches() || TWITTER_PROFILE_URL.matcher(message).matches());
	}
	
	private String extractStatusId(String url)
	{
		Matcher matcher = TWITTER_STATUS_URL.matcher(url);
		
		if(matcher.matches() && matcher.group(3) != null)
		{
			return matcher.group(3);
		}
		
		return null;
	}
	
	private String extractProfileId(String url)
	{
		Matcher matcher = TWITTER_PROFILE_URL.matcher(url);
		
		if(matcher.matches() && matcher.group(2) != null)
		{
			return matcher.group(2);
		}
		
		return null;
	}
	
	private String status(String url)
	{
		String id = this.extractStatusId(url);
		
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
				connection.setRequestProperty("Authorization", "Bearer " + TS3Bot.getInstance().getApiKeyManager().requestToken(TS3Bot.API_KEY_TWITTER, "bearer_token"));
				connection.setConnectTimeout(5000);
				connection.connect();
				
				JsonNode node = TS3Bot.getInstance().getObjectMapper().readTree(connection.getInputStream());
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
	
	private String profile(String url)
	{
		String id = this.extractProfileId(url);
		
		if(id != null)
		{
			StringBuilder query = new StringBuilder(API_URL + "users/by/username/" + id);
			
			query.append("?user.fields=description");
			
			HttpURLConnection connection = null;
			
			try
			{
				connection = (HttpURLConnection) new URL(query.toString()).openConnection();
				connection.setRequestProperty("Authorization", "Bearer " + TS3Bot.getInstance().getApiKeyManager().requestToken(TS3Bot.API_KEY_TWITTER, "bearer_token"));
				connection.setConnectTimeout(5000);
				connection.connect();
				
				JsonNode node = TS3Bot.getInstance().getObjectMapper().readTree(connection.getInputStream());
				JsonNode data = node.path("data");
				String name = data.path("name").asText();
				String username = data.path("username").asText();
				String description = data.path("description").asText().replaceAll("\n+", "; ");
				
				if(name != null && !name.isEmpty() && username != null && !username.isEmpty())
				{
					StringBuilder builder = new StringBuilder();
					
					builder.append(name);
					builder.append(" (@" + username + ")");
					
					if(description != null && !description.isEmpty())
					{
						builder.append(" \"" + description + "\"");
					}
					
					return builder.toString();
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
}
