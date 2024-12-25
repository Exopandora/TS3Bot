package net.kardexo.bot.adapters.web.impl;

import com.fasterxml.jackson.databind.JsonNode;
import net.kardexo.bot.adapters.twitter.Twitter;
import net.kardexo.bot.adapters.web.IURLProcessor;
import net.kardexo.bot.services.api.IAPIKeyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TwitterURLProcessor implements IURLProcessor
{
	private static final Logger logger = LoggerFactory.getLogger(TwitterURLProcessor.class);
	private static final Pattern TWITTER_STATUS_URL = Pattern.compile("https://(?:www\\.)?(?:twitter|x)\\.com/[^/]+/status/([0-9]+).*");
	private static final Pattern TWITTER_PROFILE_URL = Pattern.compile("https://(?:www\\.)?(?:twitter|x)\\.com/([^/]+)");
	private static final Pattern STATUS = Pattern.compile("^([\\s\\S]*)https://t\\.co/.{10}$");
	
	private final IAPIKeyService apiKeyService;
	
	public TwitterURLProcessor(IAPIKeyService apiKeyService)
	{
		this.apiKeyService = apiKeyService;
	}
	
	@Override
	public String process(String url)
	{
		if(TWITTER_STATUS_URL.matcher(url).matches())
		{
			String id = TwitterURLProcessor.extractStatusId(url);
			
			if(id != null)
			{
				return TwitterURLProcessor.status(this.apiKeyService, id);
			}
		}
		else if(TWITTER_PROFILE_URL.matcher(url).matches())
		{
			String id = TwitterURLProcessor.extractProfileId(url);
			
			if(id != null)
			{
				return TwitterURLProcessor.profile(this.apiKeyService, id);
			}
		}
		
		return null;
	}
	
	@Override
	public boolean isApplicable(String message)
	{
		return message != null && (TWITTER_STATUS_URL.matcher(message).matches() || TWITTER_PROFILE_URL.matcher(message).matches());
	}
	
	private static String extractStatusId(String url)
	{
		Matcher matcher = TWITTER_STATUS_URL.matcher(url);
		
		if(matcher.matches() && matcher.group(1) != null)
		{
			return matcher.group(1);
		}
		
		return null;
	}
	
	private static String extractProfileId(String url)
	{
		Matcher matcher = TWITTER_PROFILE_URL.matcher(url);
		
		if(matcher.matches() && matcher.group(1) != null)
		{
			return matcher.group(1);
		}
		
		return null;
	}
	
	private static String status(IAPIKeyService apiKeyService, String id)
	{
		try
		{
			JsonNode node = Twitter.status(apiKeyService, id);
			JsonNode data = node.path("data");
			JsonNode includes = node.path("includes");
			JsonNode users = includes.path("users");
			JsonNode media = includes.path("media");
			
			if(data.size() == 1 && users.size() == 1)
			{
				String text = data.get(0).path("text").asText();
				String user = users.get(0).path("name").asText();
				
				if(text != null && user != null && !user.isEmpty())
				{
					Matcher matcher = TwitterURLProcessor.STATUS.matcher(text);
					String status;
					
					if(matcher.matches())
					{
						status = matcher.group(1).replaceAll("\\s*\n\\s*", " ").trim();
					}
					else
					{
						status = text.replaceAll("\\s*\n\\s*", " ").trim();
					}
					
					if(!media.isEmpty())
					{
						String attachments = Attachments.parse(media).toString();
						
						if(!attachments.isEmpty())
						{
							if(status.isEmpty())
							{
								return attachments + " by " + user;
							}
							
							status += " [" + attachments + "]";
						}
					}
					
					if(!status.isEmpty())
					{
						return user + ": \"" + status + "\"";
					}
				}
			}
		}
		catch(Exception e)
		{
			logger.error("Error fetching twitter status {}", id, e);
		}
		
		return null;
	}
	
	private static String profile(IAPIKeyService apiKeyService, String id)
	{
		try
		{
			JsonNode data = Twitter.profile(apiKeyService, id).path("data");
			String name = data.path("name").asText();
			String username = data.path("username").asText();
			String description = data.path("description").asText().replaceAll("\n+", "; ");
			
			if(name != null && !name.isEmpty() && username != null && !username.isEmpty())
			{
				StringBuilder builder = new StringBuilder();
				
				builder.append(name);
				builder.append(" (@");
				builder.append(username);
				builder.append(")");
				
				if(!description.isEmpty())
				{
					builder.append(" \"");
					builder.append(description);
					builder.append("\"");
				}
				
				return builder.toString();
			}
		}
		catch(Exception e)
		{
			logger.error("Error fetching twitter profile {}", id, e);
		}
		
		return null;
	}
	
	private record Attachments(int images, int videos)
	{
		public static Attachments parse(JsonNode media)
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
			
			return new Attachments(images, videos);
		}
		
		@Override
		public String toString()
		{
			if(this.images > 0 && this.videos > 0)
			{
				return this.videos + " video" + (this.videos > 1 ? "s" : "") + " and " + this.images + " image" + (this.images > 1 ? "s" : "");
			}
			else if(this.images > 0)
			{
				return (this.images > 1 ? this.images + " images" : "Image");
			}
			else if(this.videos > 0)
			{
				return (this.videos > 1 ? this.videos + " videos" : "Video");
			}
			
			return "";
		}
	}
}
