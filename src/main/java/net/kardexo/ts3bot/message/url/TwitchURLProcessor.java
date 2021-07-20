package net.kardexo.ts3bot.message.url;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.kardexo.ts3bot.api.Twitch;

public class TwitchURLProcessor implements IURLProcessor
{
	public static final URI API_URL = URI.create("https://api.twitch.tv/helix/");
	public static final String BASE_URL = "https://twitch.tv/";
	private static final Pattern TWITCH_URL = Pattern.compile("https:\\/\\/(?:www\\.)?twitch\\.tv\\/(?!directory(?=$|\\/.*)|p\\/.*)([^ /]+)");
	
	@Override
	public String process(String url)
	{
		try
		{
			return Twitch.details(this.extractUsername(url), false);
		}
		catch(Exception e)
		{
			return null;
		}
	}
	
	@Override
	public boolean isApplicable(String url)
	{
		return url != null && TWITCH_URL.matcher(url).matches();
	}
	
	private String extractUsername(String url)
	{
		Matcher matcher = TWITCH_URL.matcher(url);
		
		if(matcher.matches() && matcher.group(1) != null)
		{
			return matcher.group(1);
		}
		
		return null;
	}
}
