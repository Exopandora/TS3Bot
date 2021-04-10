package net.kardexo.ts3bot.messageprocessors.url.impl;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;

import com.fasterxml.jackson.databind.JsonNode;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.messageprocessors.url.IURLProcessor;
import net.kardexo.ts3bot.util.Util;

public class DefaultURLProcessor implements IURLProcessor
{
	private static final URI API_URL = URI.create("https://api.imagga.com/v2/tags");
	private static final String MIME_TYPE_TEXT_HTML = "text/html";
	private static final String MIME_TYPE_IMAGE = "image\\/.+";
	
	@Override
	public String process(String url)
	{
		try(CloseableHttpClient client = Util.httpClient())
		{
			List<String> contentTypes = new ArrayList<String>();
			HttpGet httpGet = new HttpGet(URI.create(url));
			httpGet.setHeader("Accept", MIME_TYPE_TEXT_HTML);
			httpGet.setHeader("Accept-Charset", StandardCharsets.UTF_8.toString());
			
			try(CloseableHttpResponse response = client.execute(httpGet))
			{
				String[] contentType = Objects.requireNonNullElse(response.getEntity().getContentType().getValue(), "").split(";");
				Arrays.stream(contentType).map(String::trim).collect(Collectors.toCollection(() -> contentTypes));
				
				if(contentTypes.contains(MIME_TYPE_TEXT_HTML))
				{
					return Jsoup.parse(EntityUtils.toString(response.getEntity())).getElementsByTag("title").first().text();
				}
			}
			
			if(contentTypes.stream().anyMatch(string -> string.matches(MIME_TYPE_IMAGE)))
			{
				httpGet.setURI(new URIBuilder(API_URL).addParameter("image_url", url).build());
				httpGet.setHeader("Authorization", "Basic " + TS3Bot.getInstance().getApiKeyManager().requestKey(TS3Bot.API_KEY_IMAGGA));
				
				try(CloseableHttpResponse response = client.execute(httpGet))
				{
					JsonNode node = TS3Bot.getInstance().getObjectMapper().readTree(response.getEntity().getContent()).path("result").path("tags");
					
					if(!node.isMissingNode())
					{
						return StreamSupport.stream(node.spliterator(), false)
								.sorted((a, b) -> Double.compare(a.path("confidence").asDouble(), b.path("confidence").asDouble()))
								.limit(10)
								.map(tag -> tag.path("tag").path("en").asText())
								.collect(Collectors.joining(", ", "Image tags: \"", "\""));
					}
				}
			}
			
			String host = new URI(url).getHost();
			
			if(host != null)
			{
				return host.replaceAll("www\\.", "");
			}
			
			return host;
		}
		catch(Exception e)
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
