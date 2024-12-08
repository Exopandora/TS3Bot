package net.kardexo.ts3bot.api;

import java.net.URI;

import net.kardexo.ts3bot.services.APIKeyService;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.message.url.TwitchURLProcessor;
import net.kardexo.ts3bot.util.Util;

public class Twitch
{
	private static final SimpleCommandExceptionType TWITCH_SERVICE_UNAVAILABLE = new SimpleCommandExceptionType(new LiteralMessage("Twitch service is currently unavailable"));
	
	public static String details(String user, boolean appendLink) throws CommandSyntaxException
	{
		try(CloseableHttpClient client = Util.httpClient())
		{
			URI uri = new URIBuilder(TwitchURLProcessor.API_URL.resolve("streams"))
				.addParameter("user_login", user)
				.build();
			
			HttpGet httpGet = new HttpGet(uri);
			httpGet.setHeader("Client-ID", TS3Bot.getInstance().getApiKeyManager().requestToken(APIKeyService.API_KEY_TWITCH, "client_id"));
			httpGet.setHeader("Authorization", "Bearer " + TS3Bot.getInstance().getApiKeyManager().requestToken(APIKeyService.API_KEY_TWITCH, "oauth_token"));
			
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
						result += " " + TwitchURLProcessor.BASE_URL + user;
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
		
		throw TWITCH_SERVICE_UNAVAILABLE.create();
	}
}
