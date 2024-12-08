package net.kardexo.ts3bot.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import net.kardexo.ts3bot.services.APIKeyService;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.kardexo.ts3bot.TS3Bot;

public class Imagga
{
	private static final URI API_URL = URI.create("https://api.imagga.com/v2/tags");
	private static final SimpleCommandExceptionType IMAGGA_SERVICE_UNAVAILABLE = new SimpleCommandExceptionType(new LiteralMessage("Error loading video"));
	
	public static JsonNode tagImage(CloseableHttpClient client, HttpGet httpGet, String url) throws URISyntaxException, ClientProtocolException, IOException, CommandSyntaxException
	{
		httpGet.setURI(new URIBuilder(API_URL).addParameter("image_url", url).build());
		httpGet.setHeader("Authorization", "Basic " + TS3Bot.getInstance().getApiKeyManager().requestKey(APIKeyService.API_KEY_IMAGGA));
		
		try(CloseableHttpResponse response = client.execute(httpGet))
		{
			JsonNode node = TS3Bot.getInstance().getObjectMapper().readTree(response.getEntity().getContent()).path("result").path("tags");
			
			if(!node.isMissingNode())
			{
				return node;
			}
		}
		
		throw IMAGGA_SERVICE_UNAVAILABLE.create();
	}
}
