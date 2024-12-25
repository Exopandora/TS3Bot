package net.kardexo.bot.adapters.web;

import net.kardexo.bot.adapters.web.impl.DefaultURLProcessor;
import net.kardexo.bot.adapters.web.impl.SteamURLProcessor;
import net.kardexo.bot.adapters.web.impl.TwitchURLProcessor;
import net.kardexo.bot.adapters.web.impl.TwitterURLProcessor;
import net.kardexo.bot.adapters.web.impl.Watch2GetherURLProcessor;
import net.kardexo.bot.adapters.web.impl.YouTubeURLProcessor;
import net.kardexo.bot.domain.ChatHistory;
import net.kardexo.bot.domain.Util;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.api.IChannel;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.domain.api.IConsoleChannel;
import net.kardexo.bot.services.api.IAPIKeyService;
import net.kardexo.bot.services.api.IURLMessageProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class URLMessageProcessor implements IURLMessageProcessor
{
	public static final int MESSAGE_LIFETIME_MILLIS = 10000;
	
	private final DefaultURLProcessor defaultURLProcessor;
	private final List<IURLProcessor> urlProcessors = new ArrayList<IURLProcessor>();
	
	public URLMessageProcessor(IAPIKeyService apiKeyService)
	{
		this.defaultURLProcessor = new DefaultURLProcessor(apiKeyService);
		IURLProcessorRegistrar.INSTANCE.forEach(registrar -> registrar.register(this.urlProcessors, apiKeyService));
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
	
	@Override
	public String processMessage(String message, @NotNull ChatHistory chatHistory)
	{
		if(chatHistory.appendAndCheckIfNew(message, MESSAGE_LIFETIME_MILLIS))
		{
			return this.processMessage(message);
		}
		
		return null;
	}
	
	@Override
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
