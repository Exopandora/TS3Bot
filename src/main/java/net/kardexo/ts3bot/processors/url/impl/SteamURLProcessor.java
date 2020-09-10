package net.kardexo.ts3bot.processors.url.impl;

import java.util.regex.Pattern;

public class SteamURLProcessor extends DefaultURLProcessor
{
	private static final Pattern STEAM_URL = Pattern.compile("https?:\\/\\/([^\\.]+\\.)?(steamcommunity|steampowered)\\.[^ ]+");
	
	@Override
	public String process(String url)
	{
		String response = super.process(url);
		StringBuilder builder = new StringBuilder();
		
		if(response != null)
		{
			builder.append(response + " ");
		}
		
		return builder.append("steam://openurl/" + url).toString();
	}
	
	@Override
	public boolean isApplicable(String url)
	{
		return url != null && STEAM_URL.matcher(url).matches();
	}
}
