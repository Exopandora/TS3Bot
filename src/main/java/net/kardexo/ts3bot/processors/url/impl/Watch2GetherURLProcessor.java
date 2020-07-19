package net.kardexo.ts3bot.processors.url.impl;

import java.util.regex.Pattern;

import net.kardexo.ts3bot.processors.url.IURLProcessor;

public class Watch2GetherURLProcessor implements IURLProcessor
{
	private static final Pattern WATCH2GETHER_URL = Pattern.compile("https:\\/\\/(www\\.)?watch2gether\\.com\\/rooms\\/.*");
	
	@Override
	public String process(String url)
	{
		return "";
	}
	
	@Override
	public boolean isApplicable(String message)
	{
		return message != null && WATCH2GETHER_URL.matcher(message).matches();
	}
}
