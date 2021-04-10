package net.kardexo.ts3bot.messageprocessors.url.impl;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;

import com.fasterxml.jackson.databind.JsonNode;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.messageprocessors.url.IURLProcessor;
import net.kardexo.ts3bot.util.Util;

public class TwitterURLProcessor implements IURLProcessor
{
	private static final URI API_URL = URI.create("https://api.twitter.com/labs/2/");
	private static final Pattern TWITTER_STATUS_URL = Pattern.compile("https:\\/\\/(www\\.)?twitter\\.com\\/([^/]+)\\/status\\/([0-9]+).*");
	private static final Pattern TWITTER_PROFILE_URL = Pattern.compile("https:\\/\\/(www\\.)?twitter\\.com\\/([^/]+)");
	
	@Override
	public String process(String url)
	{
		if(TWITTER_STATUS_URL.matcher(url).matches())
		{
			String id = this.extractStatusId(url);
			
			if(id != null)
			{
				return this.status(id);
			}
		}
		else if(TWITTER_PROFILE_URL.matcher(url).matches())
		{
			String id = this.extractProfileId(url);
			
			if(id != null)
			{
				return this.profile(id);
			}
		}
		
		return null;
	}
	
	@Override
	public boolean isApplicable(String message)
	{
		return message != null && (TWITTER_STATUS_URL.matcher(message).matches() || TWITTER_PROFILE_URL.matcher(message).matches());
	}
	
	private String extractStatusId(String url)
	{
		Matcher matcher = TWITTER_STATUS_URL.matcher(url);
		
		if(matcher.matches() && matcher.group(3) != null)
		{
			return matcher.group(3);
		}
		
		return null;
	}
	
	private String extractProfileId(String url)
	{
		Matcher matcher = TWITTER_PROFILE_URL.matcher(url);
		
		if(matcher.matches() && matcher.group(2) != null)
		{
			return matcher.group(2);
		}
		
		return null;
	}
	
	private String status(String id)
	{
		try(CloseableHttpClient client = Util.httpClient())
		{
			URI uri = new URIBuilder(API_URL.resolve("tweets"))
				.addParameter("ids", id)
				.addParameter("user.fields", "name")
				.addParameter("expansions", "author_id,attachments.media_keys")
				.build();
			
			HttpGet httpGet = new HttpGet(uri);
			httpGet.setHeader("Authorization", "Bearer " + TS3Bot.getInstance().getApiKeyManager().requestToken(TS3Bot.API_KEY_TWITTER, "bearer_token"));
			
			try(CloseableHttpResponse response = client.execute(httpGet))
			{
				JsonNode node = TS3Bot.getInstance().getObjectMapper().readTree(response.getEntity().getContent());
				JsonNode data = node.path("data");
				JsonNode includes = node.path("includes");
				JsonNode users = includes.path("users");
				JsonNode media = includes.path("media");
				
				if(data.size() == 1 && users.size() == 1)
				{
					String text = data.get(0).path("text").asText();
					String user = users.get(0).path("name").asText();
					
					if(text != null && !text.isEmpty() && user != null && !user.isEmpty())
					{
						if(text.length() > 23)
						{
							return user + ": \"" + text.substring(0, text.length() - 24) + "\"";
						}
						else if(media.size() > 0)
						{
							int images = 0;
							int videos = 0;
							
							for(JsonNode item : media)
							{
								String type = item.path("type").asText();
								
								if("photo".equals(type))
								{
									images++;
								}
								else if("video".equals(type))
								{
									videos++;
								}
							}
							
							if(images > 0 && videos > 0)
							{
								return videos + " video" + (videos > 1 ? "s" : "") + " and " + images + " image" + (images > 1 ? "s" : "") + " by " + user;
							}
							else if(images > 0)
							{
								return (images > 1 ? images + " images" : "Image") + " by " + user;
							}
							else if(videos > 0)
							{
								return (videos > 1 ? videos + " videos" : "Video") + " by " + user;
							}
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	private String profile(String id)
	{
		try(CloseableHttpClient client = Util.httpClient())
		{
			URI uri = new URIBuilder(API_URL.resolve("users/by/username/" + id))
				.addParameter("user.fields", "description")
				.build();
			
			HttpGet httpGet = new HttpGet(uri);
			httpGet.setHeader("Authorization", "Bearer " + TS3Bot.getInstance().getApiKeyManager().requestToken(TS3Bot.API_KEY_TWITTER, "bearer_token"));
			
			try(CloseableHttpResponse response = client.execute(httpGet))
			{
				JsonNode node = TS3Bot.getInstance().getObjectMapper().readTree(response.getEntity().getContent());
				JsonNode data = node.path("data");
				String name = data.path("name").asText();
				String username = data.path("username").asText();
				String description = data.path("description").asText().replaceAll("\n+", "; ");
				
				if(name != null && !name.isEmpty() && username != null && !username.isEmpty())
				{
					StringBuilder builder = new StringBuilder();
					
					builder.append(name);
					builder.append(" (@" + username + ")");
					
					if(description != null && !description.isEmpty())
					{
						builder.append(" \"" + description + "\"");
					}
					
					return builder.toString();
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
