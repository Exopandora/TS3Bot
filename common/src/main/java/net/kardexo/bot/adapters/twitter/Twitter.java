package net.kardexo.bot.adapters.twitter;

import com.fasterxml.jackson.databind.JsonNode;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.kardexo.bot.domain.Util;
import net.kardexo.bot.services.api.IAPIKeyService;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import static net.kardexo.bot.domain.Util.OBJECT_MAPPER;

public class Twitter
{
	private static final Logger logger = LoggerFactory.getLogger(Twitter.class);
	private static final URI API_URL = URI.create("https://api.twitter.com/2/");
	private static final SimpleCommandExceptionType ERROR_LOADING_STATUS = new SimpleCommandExceptionType(new LiteralMessage("Error loading status"));
	private static final SimpleCommandExceptionType ERROR_LOADING_PROFILE = new SimpleCommandExceptionType(new LiteralMessage("Error loading profile"));
	
	public static JsonNode status(IAPIKeyService apiKeyService, String id) throws CommandSyntaxException
	{
		try(CloseableHttpClient client = Util.httpClient())
		{
			URI uri = new URIBuilder(API_URL.resolve("tweets"))
				.addParameter("ids", id)
				.addParameter("user.fields", "name")
				.addParameter("expansions", "author_id,attachments.media_keys")
				.build();
			
			HttpGet httpGet = new HttpGet(uri);
			httpGet.setHeader("Authorization", "Bearer " + apiKeyService.requestToken(IAPIKeyService.API_KEY_TWITTER, "bearer_token"));
			
			try(CloseableHttpResponse response = client.execute(httpGet))
			{
				return OBJECT_MAPPER.readTree(response.getEntity().getContent());
			}
		}
		catch(Exception e)
		{
			logger.error("Error fetching twitter status {}", id, e);
		}
		
		throw ERROR_LOADING_STATUS.create();
	}
	
	public static JsonNode profile(IAPIKeyService apiKeyService, String id) throws CommandSyntaxException
	{
		try(CloseableHttpClient client = Util.httpClient())
		{
			URI uri = new URIBuilder(API_URL.resolve("users/by/username/" + id))
				.addParameter("user.fields", "description")
				.build();
			
			HttpGet httpGet = new HttpGet(uri);
			httpGet.setHeader("Authorization", "Bearer " + apiKeyService.requestToken(IAPIKeyService.API_KEY_TWITTER, "bearer_token"));
			
			try(CloseableHttpResponse response = client.execute(httpGet))
			{
				return OBJECT_MAPPER.readTree(response.getEntity().getContent());
			}
		}
		catch(Exception e)
		{
			logger.error("Error fetching twitter profile {}", id, e);
		}
		
		throw ERROR_LOADING_PROFILE.create();
	}
}
