package net.kardexo.ts3bot.message.url;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;

import net.kardexo.ts3bot.api.Twitter;

public class TwitterURLProcessor implements IURLProcessor
{
	private static final Pattern TWITTER_STATUS_URL = Pattern.compile("https:\\/\\/(www\\.)?twitter\\.com\\/([^/]+)\\/status\\/([0-9]+).*");
	private static final Pattern TWITTER_PROFILE_URL = Pattern.compile("https:\\/\\/(www\\.)?twitter\\.com\\/([^/]+)");
	private static final Pattern STATUS = Pattern.compile("^([\\s\\S]*)https:\\/\\/t\\.co\\/.{10}$");
	
	@Override
	public String process(String url)
	{
		if(TWITTER_STATUS_URL.matcher(url).matches())
		{
			String id = TwitterURLProcessor.extractStatusId(url);
			
			if(id != null)
			{
				return TwitterURLProcessor.status(id);
			}
		}
		else if(TWITTER_PROFILE_URL.matcher(url).matches())
		{
			String id = TwitterURLProcessor.extractProfileId(url);
			
			if(id != null)
			{
				return TwitterURLProcessor.profile(id);
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
		
		if(matcher.matches() && matcher.group(3) != null)
		{
			return matcher.group(3);
		}
		
		return null;
	}
	
	private static String extractProfileId(String url)
	{
		Matcher matcher = TWITTER_PROFILE_URL.matcher(url);
		
		if(matcher.matches() && matcher.group(2) != null)
		{
			return matcher.group(2);
		}
		
		return null;
	}
	
	private static String status(String id)
	{
		try
		{
			JsonNode node = Twitter.status(id);
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
					String status = null;
					
					if(matcher.matches())
					{
						status = matcher.group(1).replaceAll("\\s*\n\\s*", " ").trim();
					}
					else
					{
						status = text.replaceAll("\\s*\n\\s*", " ").trim();
					}
					
					if(media.size() > 0)
					{
						String attachments = Attachments.parse(media).toString();
						
						if(!attachments.isEmpty())
						{
							if(status == null || status.isEmpty())
							{
								return attachments + " by " + user;
							}
							
							status += " [" + attachments + "]";
						}
					}
					
					if(status != null && !status.isEmpty())
					{
						return user + ": \"" + status.replaceAll("(https:\\/\\/t\\.co\\/.{10})", "[URL]$1[/URL]") + "\"";
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
	
	private static String profile(String id)
	{
		try
		{
			JsonNode data = Twitter.profile(id).path("data");
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
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static class Attachments
	{
		private final int images;
		private final int videos;
		
		public Attachments(int images, int videos)
		{
			this.images = images;
			this.videos = videos;
		}
		
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
