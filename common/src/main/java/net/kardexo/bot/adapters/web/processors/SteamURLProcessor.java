package net.kardexo.bot.adapters.web.processors;

import net.kardexo.bot.services.api.IAPIKeyService;

import java.util.regex.Pattern;

public class SteamURLProcessor extends DefaultURLProcessor
{
	private static final Pattern STEAM_URL = Pattern.compile("https?:\\/\\/([^\\.]+\\.)?(steamcommunity|steampowered)\\.[^ ]+");
	
	public SteamURLProcessor(IAPIKeyService apiKeyService)
	{
		super(apiKeyService);
	}
	
	@Override
	public String process(String url)
	{
		String response = super.process(url);
		StringBuilder builder = new StringBuilder();
		
		if(response != null)
		{
			builder.append(response);
			builder.append(" ");
		}
		
		return builder.append("steam://openurl/").append(url).toString();
	}
	
	@Override
	public boolean isApplicable(String url)
	{
		return url != null && STEAM_URL.matcher(url).matches();
	}
}
