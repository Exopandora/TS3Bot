package net.kardexo.bot.adapters.w2g;

import com.fasterxml.jackson.databind.JsonNode;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.kardexo.bot.domain.Util;
import net.kardexo.bot.services.api.IAPIKeyService;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static net.kardexo.bot.domain.Util.OBJECT_MAPPER;

public class Watch2Gether
{
	private static final Logger logger = LoggerFactory.getLogger(Watch2Gether.class);
	public static final URI W2G_URL = URI.create("https://w2g.tv/rooms/");
	private static final URI API_URL = URI.create("https://api.w2g.tv/rooms/");
	private static final SimpleCommandExceptionType WATCH2GETHER_SERVICE_UNAVAILABLE = new SimpleCommandExceptionType(new LiteralMessage("Watch2Gether is currently unavailable"));
	
	public static JsonNode createRoom(IAPIKeyService apiKeyService, String share, String bgColor, int bgOpacity) throws CommandSyntaxException
	{
		try(CloseableHttpClient client = Util.httpClient())
		{
			Map<String, Object> watch2gether = new HashMap<String, Object>();
			watch2gether.put("w2g_api_key", apiKeyService.requestKey(IAPIKeyService.API_KEY_WATCH_2_GETHER));
			watch2gether.put("share", share);
			watch2gether.put("bg_color", bgColor);
			watch2gether.put("bg_opacity", bgOpacity);
			
			HttpPost httpPost = new HttpPost(API_URL.resolve("create.json"));
			httpPost.addHeader("Content-Type", "application/json");
			httpPost.setEntity(new StringEntity(OBJECT_MAPPER.writeValueAsString(watch2gether)));
			
			try(CloseableHttpResponse response = client.execute(httpPost))
			{
				return OBJECT_MAPPER.readTree(response.getEntity().getContent());
			}
		}
		catch(Exception e)
		{
			logger.error("Error creating w2g room for {}", share, e);
		}
		
		throw WATCH2GETHER_SERVICE_UNAVAILABLE.create();
	}
}
