package net.kardexo.ts3bot.processors.message.impl;

import java.util.ArrayList;
import java.util.List;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.api.TextMessageTargetMode;
import com.github.theholywaffle.teamspeak3.api.wrapper.ClientInfo;

import net.kardexo.ts3bot.URLs;
import net.kardexo.ts3bot.processors.message.IMessageProcessor;
import net.kardexo.ts3bot.processors.url.IURLProcessor;
import net.kardexo.ts3bot.processors.url.impl.DefaultURLProcessor;
import net.kardexo.ts3bot.processors.url.impl.SteamURLProcessor;
import net.kardexo.ts3bot.processors.url.impl.TwitchURLProcessor;
import net.kardexo.ts3bot.processors.url.impl.TwitterURLProcessor;
import net.kardexo.ts3bot.processors.url.impl.YouTubeURLProcessor;

public class URLProcessor implements IMessageProcessor
{
	private static final DefaultURLProcessor DEFAULT_URL_PROCESSOR = new DefaultURLProcessor();
	private static final List<IURLProcessor> URL_PROCESSORS = new ArrayList<IURLProcessor>();
	
	static
	{
		URL_PROCESSORS.add(new SteamURLProcessor());
		URL_PROCESSORS.add(new TwitchURLProcessor());
		URL_PROCESSORS.add(new YouTubeURLProcessor());
		URL_PROCESSORS.add(new TwitterURLProcessor());
	}
	
	@Override
	public boolean onMessage(String message, TS3Api api, ClientInfo clientInfo, TextMessageTargetMode target)
	{
		String url = URLs.extract(message);
		
		if(url != null)
		{
			for(IURLProcessor processor : URL_PROCESSORS)
			{
				if(this.process(processor, url, api, clientInfo, target))
				{
					return true;
				}
			}
			
			return this.process(DEFAULT_URL_PROCESSOR, url, api, clientInfo, target);
		}
		
		return false;
	}
	
	private boolean process(IURLProcessor processor, String url, TS3Api api, ClientInfo clientInfo, TextMessageTargetMode target)
	{
		if(processor.isApplicable(url))
		{
			String response = URLProcessor.normalize(processor.process(url));
			
			if(response != null)
			{
				if(!response.isEmpty())
				{
					api.sendTextMessage(target, clientInfo.getId(), response);
				}
				
				return true;
			}
		}
		
		return false;
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
