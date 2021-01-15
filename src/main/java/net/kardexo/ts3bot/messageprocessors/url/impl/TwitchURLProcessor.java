package net.kardexo.ts3bot.messageprocessors.url.impl;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.messageprocessors.url.IURLProcessor;

public class TwitchURLProcessor implements IURLProcessor
{
	private static final String API_URL = "https://api.twitch.tv/helix/streams?user_login=";
	private static final String BASE_URL = "https://twitch.tv/";
	private static final Pattern TWITCH_URL = Pattern.compile("https:\\/\\/www\\.twitch\\.tv\\/(?!directory(?=$|\\/.*)|p\\/.*)([^ ]+)");
	
	@Override
	public String process(String url)
	{
		try
		{
			return TwitchURLProcessor.twitchDetails(this.extractUsername(url), false);
		}
		catch(IOException e)
		{
			return null;
		}
	}
	
	@Override
	public boolean isApplicable(String url)
	{
		return url != null && TWITCH_URL.matcher(url).matches();
	}
	
	private String extractUsername(String url)
	{
		Matcher matcher = TWITCH_URL.matcher(url);
		
		if(matcher.matches() && matcher.group(1) != null)
		{
			return matcher.group(1);
		}
		
		return null;
	}
	
	public static String twitchDetails(String user, boolean appendLink) throws IOException
	{
		HttpURLConnection connection = (HttpURLConnection) new URL(API_URL + user).openConnection();
		
		try
		{
			connection.setRequestProperty("Client-ID", TS3Bot.getInstance().getApiKeyManager().requestToken(TS3Bot.API_KEY_TWITCH, "client_id"));
			connection.setRequestProperty("Authorization", "Bearer " + TS3Bot.getInstance().getApiKeyManager().requestToken(TS3Bot.API_KEY_TWITCH, "oauth_token"));
			connection.setConnectTimeout(5000);
			connection.connect();
			
			JsonNode node = TS3Bot.getInstance().getObjectMapper().readTree(connection.getInputStream());
			JsonNode data = node.path("data");
			
			if(data != null && data.size() > 0)
			{
				JsonNode content = data.get(0);
				String result = content.path("user_name").asText() + " is live for " + content.path("viewer_count").asInt() + " viewers " + content.path("title");
				
				if(appendLink)
				{
					result += " " + BASE_URL + user;
				}
				
				return result;
			}
			else
			{
				return user + " is currently offline";
			}
		}
		finally
		{
			connection.disconnect();
		}
	}
}
