package net.kardexo.bot.adapters.twitch;

import com.fasterxml.jackson.databind.JsonNode;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.kardexo.bot.adapters.web.processors.TwitchURLProcessor;
import net.kardexo.bot.services.api.IAPIKeyService;
import net.kardexo.bot.domain.Util;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import static net.kardexo.bot.domain.Util.OBJECT_MAPPER;

public class Twitch
{
	private static final Logger logger = LoggerFactory.getLogger(Twitch.class);
	private static final SimpleCommandExceptionType TWITCH_SERVICE_UNAVAILABLE = new SimpleCommandExceptionType(new LiteralMessage("Twitch service is currently unavailable"));
	
	public static String details(IAPIKeyService apiKeyService, String user, boolean appendLink) throws CommandSyntaxException
	{
		try(CloseableHttpClient client = Util.httpClient())
		{
			URI uri = new URIBuilder(TwitchURLProcessor.API_URL.resolve("streams"))
				.addParameter("user_login", user)
				.build();
			
			HttpGet httpGet = new HttpGet(uri);
			httpGet.setHeader("Client-ID", apiKeyService.requestToken(IAPIKeyService.API_KEY_TWITCH, "client_id"));
			httpGet.setHeader("Authorization", "Bearer " + apiKeyService.requestToken(IAPIKeyService.API_KEY_TWITCH, "oauth_token"));
			
			try(CloseableHttpResponse response = client.execute(httpGet))
			{
				JsonNode node = OBJECT_MAPPER.readTree(response.getEntity().getContent());
				JsonNode data = node.path("data");
				
				if(data != null && !data.isEmpty())
				{
					JsonNode content = data.get(0);
					String result = content.path("user_name").asText() + " is live for " + content.path("viewer_count").asInt() + " viewers " + content.path("title");
					
					if(appendLink)
					{
						result += " " + TwitchURLProcessor.BASE_URL + user;
					}
					
					return result;
				}
				
				return user + " is currently offline";
			}
		}
		catch(Exception e)
		{
			logger.error("Error fetching twitch details {}", user, e);
		}
		
		throw TWITCH_SERVICE_UNAVAILABLE.create();
	}
}
