package net.kardexo.ts3bot.processors.url.impl;

import java.util.regex.Pattern;

import net.kardexo.ts3bot.processors.url.IURLProcessor;

public class SteamURLProcessor implements IURLProcessor
{
	private static final Pattern STEAM_URL = Pattern.compile("https?:\\/\\/([^\\.]+\\.)?(steamcommunity|steampowered)\\.[^ ]+");
	
	@Override
	public String process(String url)
	{
		return "steam://openurl/" + url;
	}
	
	@Override
	public boolean isApplicable(String url)
	{
		return url != null && STEAM_URL.matcher(url).matches();
	}
}
