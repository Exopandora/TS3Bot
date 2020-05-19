package net.kardexo.ts3bot;

import java.util.Random;

public class Util
{
	public static String extractURL(String url)
	{
		if(url != null)
		{
			return url.substring(5, url.length() - 6);
		}
		
		return null;
	}

	public static final Random RANDOM = new Random();
}
