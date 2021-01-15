package net.kardexo.ts3bot.messageprocessors.impl;

import java.util.ArrayList;
import java.util.List;

import net.kardexo.ts3bot.messageprocessors.IMessageProcessor;
import net.kardexo.ts3bot.messageprocessors.url.IURLProcessor;
import net.kardexo.ts3bot.messageprocessors.url.impl.DefaultURLProcessor;
import net.kardexo.ts3bot.messageprocessors.url.impl.SteamURLProcessor;
import net.kardexo.ts3bot.messageprocessors.url.impl.TwitchURLProcessor;
import net.kardexo.ts3bot.messageprocessors.url.impl.TwitterURLProcessor;
import net.kardexo.ts3bot.messageprocessors.url.impl.Watch2GetherURLProcessor;
import net.kardexo.ts3bot.messageprocessors.url.impl.YouTubeURLProcessor;
import net.kardexo.ts3bot.util.URLs;

public class URLMessageProcessor implements IMessageProcessor
{
	private static final DefaultURLProcessor DEFAULT_URL_PROCESSOR = new DefaultURLProcessor();
	private static final List<IURLProcessor> URL_PROCESSORS = new ArrayList<IURLProcessor>();
	
	static
	{
		URL_PROCESSORS.add(new SteamURLProcessor());
		URL_PROCESSORS.add(new TwitchURLProcessor());
		URL_PROCESSORS.add(new YouTubeURLProcessor());
		URL_PROCESSORS.add(new TwitterURLProcessor());
		URL_PROCESSORS.add(new Watch2GetherURLProcessor());
	}
	
	@Override
	public String process(String message)
	{
		String url = URLs.extract(message);
		
		if(url != null)
		{
			for(IURLProcessor processor : URL_PROCESSORS)
			{
				String response = this.process(processor, url);
				
				if(response != null)
				{
					return response;
				}
			}
			
			return this.process(DEFAULT_URL_PROCESSOR, url);
		}
		
		return null;
	}
	
	private String process(IURLProcessor processor, String url)
	{
		if(processor.isApplicable(url))
		{
			String response = URLMessageProcessor.normalize(processor.process(url));
			
			if(response != null && !response.isEmpty())
			{
				return response;
			}
		}
		
		return null;
	}
	
	private static String normalize(String string)
	{
		if(string != null)
		{
			return string.replaceAll("\\s+", " ");
		}
		
		return null;
	}
}
