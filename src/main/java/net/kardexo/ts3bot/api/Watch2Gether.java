package net.kardexo.ts3bot.api;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import net.kardexo.ts3bot.services.APIKeyService;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.util.Util;

public class Watch2Gether
{
	public static final URI W2G_URL = URI.create("https://w2g.tv/rooms/");
	private static final URI API_URL = URI.create("https://api.w2g.tv/rooms/");
	private static final SimpleCommandExceptionType WATCH2GETHER_SERVICE_UNAVAILABLE = new SimpleCommandExceptionType(new LiteralMessage("Watch2Gether is currently unavailable"));
	
	public static JsonNode createRoom(String share) throws CommandSyntaxException
	{
		try(CloseableHttpClient client = Util.httpClient())
		{
			Map<String, Object> watch2gether = new HashMap<String, Object>();
			watch2gether.put("w2g_api_key", TS3Bot.getInstance().getApiKeyManager().requestKey(APIKeyService.API_KEY_WATCH_2_GETHER));
			watch2gether.put("share", share);
			watch2gether.put("bg_color", TS3Bot.getInstance().getConfig().getDefaultW2GBGColor());
			watch2gether.put("bg_opacity", TS3Bot.getInstance().getConfig().getDefaultW2GBGOpacity());
			
			HttpPost httpPost = new HttpPost(API_URL.resolve("create.json"));
			httpPost.addHeader("Content-Type", "application/json");
			httpPost.setEntity(new StringEntity(TS3Bot.getInstance().getObjectMapper().writeValueAsString(watch2gether)));
			
			try(CloseableHttpResponse response = client.execute(httpPost))
			{
				return TS3Bot.getInstance().getObjectMapper().readTree(response.getEntity().getContent());
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		throw WATCH2GETHER_SERVICE_UNAVAILABLE.create();
	}
}
