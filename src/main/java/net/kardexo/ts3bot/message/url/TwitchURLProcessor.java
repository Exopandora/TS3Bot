package net.kardexo.ts3bot.message.url;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;

import com.fasterxml.jackson.databind.JsonNode;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.util.Util;

public class TwitchURLProcessor implements IURLProcessor
{
	private static final URI API_URL = URI.create("https://api.twitch.tv/helix/");
	private static final String BASE_URL = "https://twitch.tv/";
	private static final Pattern TWITCH_URL = Pattern.compile("https:\\/\\/(?:www\\.)?twitch\\.tv\\/(?!directory(?=$|\\/.*)|p\\/.*)([^ /]+)");
	
	@Override
	public String process(String url)
	{
		return TwitchURLProcessor.twitchDetails(this.extractUsername(url), false);
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
	
	public static String twitchDetails(String user, boolean appendLink)
	{
		try(CloseableHttpClient client = Util.httpClient())
		{
			URI uri = new URIBuilder(API_URL.resolve("streams"))
				.addParameter("user_login", user)
				.build();
			
			HttpGet httpGet = new HttpGet(uri);
			httpGet.setHeader("Client-ID", TS3Bot.getInstance().getApiKeyManager().requestToken(TS3Bot.API_KEY_TWITCH, "client_id"));
			httpGet.setHeader("Authorization", "Bearer " + TS3Bot.getInstance().getApiKeyManager().requestToken(TS3Bot.API_KEY_TWITCH, "oauth_token"));
			
			try(CloseableHttpResponse response = client.execute(httpGet))
			{
				JsonNode node = TS3Bot.getInstance().getObjectMapper().readTree(response.getEntity().getContent());
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
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
}
