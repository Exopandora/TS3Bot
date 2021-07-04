package net.kardexo.ts3bot.msgproc.url;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Watch2GetherURLProcessor implements IURLProcessor
{
	private static final Pattern WATCH2GETHER_URL = Pattern.compile("https:\\/\\/(?:www\\.)?watch2gether\\.com\\/rooms\\/([^?/]+)(?:.*)");
	private static final Pattern WATCH2GETHER_URL_2 = Pattern.compile("https:\\/\\/(?:www\\.)?w2g\\.tv\\/rooms\\/([^?/]+)(?:.*)");
	
	@Override
	public String process(String url)
	{
		String id = this.extractRoomId(url);
		
		if(id != null)
		{
			return "Watch2Gether room " + id;
		}
		
		return null;
	}
	
	@Override
	public boolean isApplicable(String message)
	{
		return message != null && (WATCH2GETHER_URL.matcher(message).matches() || WATCH2GETHER_URL_2.matcher(message).matches());
	}
	
	private String extractRoomId(String url)
	{
		Matcher matcher = WATCH2GETHER_URL.matcher(url);
		
		if(matcher.matches() && matcher.group(1) != null)
		{
			return matcher.group(1);
		}
		
		Matcher matcher2 = WATCH2GETHER_URL_2.matcher(url);
		
		if(matcher2.matches() && matcher2.group(1) != null)
		{
			return matcher2.group(1);
		}
		
		return null;
	}
}
