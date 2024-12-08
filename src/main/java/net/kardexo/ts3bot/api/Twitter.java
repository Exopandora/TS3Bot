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
import net.kardexo.ts3bot.util.Util;

public class Twitter
{
	private static final URI API_URL = URI.create("https://api.twitter.com/2/");
	private static final SimpleCommandExceptionType ERROR_LOADONG_STATUS = new SimpleCommandExceptionType(new LiteralMessage("Error loading status"));
	private static final SimpleCommandExceptionType ERROR_LOADONG_PROFILE = new SimpleCommandExceptionType(new LiteralMessage("Error loading profile"));
	
	public static JsonNode status(String id) throws CommandSyntaxException
	{
		try(CloseableHttpClient client = Util.httpClient())
		{
			URI uri = new URIBuilder(API_URL.resolve("tweets"))
				.addParameter("ids", id)
				.addParameter("user.fields", "name")
				.addParameter("expansions", "author_id,attachments.media_keys")
				.build();
			
			HttpGet httpGet = new HttpGet(uri);
			httpGet.setHeader("Authorization", "Bearer " + TS3Bot.getInstance().getApiKeyManager().requestToken(APIKeyService.API_KEY_TWITTER, "bearer_token"));
			
			try(CloseableHttpResponse response = client.execute(httpGet))
			{
				return TS3Bot.getInstance().getObjectMapper().readTree(response.getEntity().getContent());
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		throw ERROR_LOADONG_STATUS.create();
	}
	
	public static JsonNode profile(String id) throws CommandSyntaxException
	{
		try(CloseableHttpClient client = Util.httpClient())
		{
			URI uri = new URIBuilder(API_URL.resolve("users/by/username/" + id))
				.addParameter("user.fields", "description")
				.build();
			
			HttpGet httpGet = new HttpGet(uri);
			httpGet.setHeader("Authorization", "Bearer " + TS3Bot.getInstance().getApiKeyManager().requestToken(APIKeyService.API_KEY_TWITTER, "bearer_token"));
			
			try(CloseableHttpResponse response = client.execute(httpGet))
			{
				return TS3Bot.getInstance().getObjectMapper().readTree(response.getEntity().getContent());
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		throw ERROR_LOADONG_PROFILE.create();
	}
}
