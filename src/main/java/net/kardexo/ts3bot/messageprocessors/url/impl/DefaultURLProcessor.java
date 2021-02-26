package net.kardexo.ts3bot.messageprocessors.url.impl;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.logging.log4j.core.util.IOUtils;
import org.jsoup.Jsoup;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.messageprocessors.url.IURLProcessor;

public class DefaultURLProcessor implements IURLProcessor
{
	private static final String API_URL = "https://api.imagga.com/v2/tags?image_url=";
	private static final String MIME_TYPE_TEXT_HTML = "text/html";
	private static final String MIME_TYPE_IMAGE = "image\\/.+";
	
	@Override
	public String process(String url)
	{
		List<String> contentTypes = new ArrayList<String>();
		HttpURLConnection connection = null;
		
		try
		{
			connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestProperty("User-Agent", TS3Bot.USER_AGENT);
			connection.setRequestProperty("Accept", MIME_TYPE_TEXT_HTML);
			connection.setRequestProperty("Accept-Charset", "UTF-8");
			connection.setConnectTimeout(5000);
			connection.connect();
			
			Arrays.stream(Objects.requireNonNullElse(connection.getContentType(), "").split(";")).map(String::trim).collect(Collectors.toCollection(() -> contentTypes));
			
			if(contentTypes.contains(MIME_TYPE_TEXT_HTML))
			{
				try
				{
					return Jsoup.parse(IOUtils.toString(new InputStreamReader(connection.getInputStream(), "UTF-8"))).getElementsByTag("title").first().text();
				}
				finally
				{
					connection.disconnect();
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
		
		if(contentTypes.stream().anyMatch(string -> string.matches(MIME_TYPE_IMAGE)))
		{
			try
			{
				connection = (HttpURLConnection) new URL(API_URL + url).openConnection();
				connection.setRequestProperty("Authorization", "Basic " + TS3Bot.getInstance().getApiKeyManager().requestKey(TS3Bot.API_KEY_IMAGGA));
				connection.setConnectTimeout(5000);
				connection.connect();
				
				JsonNode node = new ObjectMapper().readTree(connection.getInputStream()).path("result").path("tags");
				
				if(!node.isMissingNode())
				{
					return StreamSupport.stream(node.spliterator(), false)
							.sorted((a, b) -> Double.compare(a.path("confidence").asDouble(), b.path("confidence").asDouble()))
							.limit(10)
							.map(tag -> tag.path("tag").path("en").asText())
							.collect(Collectors.joining(", ", "Image tags: \"", "\""));
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
		
		try
		{
			String host = new URI(url).getHost();
			
			if(host != null)
			{
				return host.replaceAll("www\\.", "");
			}
			
			return host;
		}
		catch(URISyntaxException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	public boolean isApplicable(String url)
	{
		return true;
	}
}
