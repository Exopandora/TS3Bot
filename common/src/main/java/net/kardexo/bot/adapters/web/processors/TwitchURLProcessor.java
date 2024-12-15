package net.kardexo.bot.adapters.web.processors;

import net.kardexo.bot.adapters.twitch.Twitch;
import net.kardexo.bot.services.api.IAPIKeyService;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TwitchURLProcessor implements IURLProcessor
{
	public static final URI API_URL = URI.create("https://api.twitch.tv/helix/");
	public static final String BASE_URL = "https://twitch.tv/";
	private static final Pattern TWITCH_URL = Pattern.compile("https:\\/\\/(?:www\\.)?twitch\\.tv\\/(?!directory(?=$|\\/.*)|p\\/.*)([^ /]+)");
	
	private final IAPIKeyService apiKeyService;
	
	public TwitchURLProcessor(IAPIKeyService apiKeyService)
	{
		this.apiKeyService = apiKeyService;
	}
	
	@Override
	public String process(String url)
	{
		try
		{
			return Twitch.details(this.apiKeyService, this.extractUsername(url), false);
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
