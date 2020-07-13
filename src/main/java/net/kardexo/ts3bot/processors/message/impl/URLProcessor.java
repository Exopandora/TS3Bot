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
import net.kardexo.ts3bot.processors.url.impl.YouTubeURLProcessor;

public class URLProcessor implements IMessageProcessor
{
	private final List<IURLProcessor> processors = new ArrayList<IURLProcessor>();
	
	public URLProcessor()
	{
		this.processors.add(new SteamURLProcessor());
		this.processors.add(new TwitchURLProcessor());
		this.processors.add(new YouTubeURLProcessor());
		this.processors.add(new DefaultURLProcessor());
	}
	
	@Override
	public boolean onMessage(String message, TS3Api api, ClientInfo clientInfo, TextMessageTargetMode target)
	{
		String url = URLs.extract(message);
		
		if(url != null)
		{
			for(IURLProcessor processor : this.processors)
			{
				if(processor.isApplicable(url))
				{
					String response = URLProcessor.normalize(processor.process(url));
					
					if(response != null)
					{
						api.sendTextMessage(target, clientInfo.getId(), response);
						return true;
					}
				}
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
