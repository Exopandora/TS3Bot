package net.kardexo.bot.adapters.web;

import net.kardexo.bot.adapters.web.processors.DefaultURLProcessor;
import net.kardexo.bot.adapters.web.processors.IURLProcessor;
import net.kardexo.bot.adapters.web.processors.SteamURLProcessor;
import net.kardexo.bot.adapters.web.processors.TwitchURLProcessor;
import net.kardexo.bot.adapters.web.processors.TwitterURLProcessor;
import net.kardexo.bot.adapters.web.processors.Watch2GetherURLProcessor;
import net.kardexo.bot.adapters.web.processors.YouTubeURLProcessor;
import net.kardexo.bot.domain.ChatHistory;
import net.kardexo.bot.domain.Util;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.api.IChannel;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.domain.api.IConsoleChannel;
import net.kardexo.bot.services.api.IAPIKeyService;
import net.kardexo.bot.services.api.IMessageProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class URLMessageProcessor implements IMessageProcessor
{
	public static final int MESSAGE_LIFETIME_MILLIS = 10000;
	
	private final DefaultURLProcessor defaultURLProcessor;
	private final List<IURLProcessor> urlProcessors;
	
	public URLMessageProcessor(IAPIKeyService apiKeyService)
	{
		this.defaultURLProcessor = new DefaultURLProcessor(apiKeyService);
		this.urlProcessors = new ArrayList<IURLProcessor>();
		this.urlProcessors.add(new SteamURLProcessor(apiKeyService));
		this.urlProcessors.add(new TwitchURLProcessor(apiKeyService));
		this.urlProcessors.add(new YouTubeURLProcessor(apiKeyService));
		this.urlProcessors.add(new TwitterURLProcessor(apiKeyService));
		this.urlProcessors.add(new Watch2GetherURLProcessor());
	}
	
	@Override
	public void process(IBotClient bot, IChannel channel, IClient client, String message, ChatHistory chatHistory)
	{
		String response = this.processMessage(message, chatHistory);
		
		if(response != null)
		{
			bot.sendMessage(channel, response);
		}
	}
	
	@Override
	public boolean isApplicable(IBotClient bot, IChannel channel, IClient client, String message, ChatHistory chatHistory)
	{
		return !(channel instanceof IConsoleChannel) && !(client instanceof IBotClient);
	}
	
	public String processMessage(String message, @NotNull ChatHistory chatHistory)
	{
		if(chatHistory.appendAndCheckIfNew(message, MESSAGE_LIFETIME_MILLIS))
		{
			return this.processMessage(message);
		}
		
		return null;
	}
	
	public String processMessage(String message)
	{
		String url = Util.extractURL(message);
		
		if(url != null)
		{
			for(IURLProcessor processor : this.urlProcessors)
			{
				if(processor.isApplicable(url))
				{
					String result = processor.process(url);
					
					if(result != null && !result.isBlank())
					{
						return result;
					}
				}
			}
			
			String result = this.defaultURLProcessor.process(url);
			
			if(result != null && !result.isBlank())
			{
				return result;
			}
		}
		
		return null;
	}
}
