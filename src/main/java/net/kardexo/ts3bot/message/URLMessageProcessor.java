package net.kardexo.ts3bot.msgproc;

import java.util.ArrayList;
import java.util.List;

import com.github.theholywaffle.teamspeak3.api.TextMessageTargetMode;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.msgproc.url.DefaultURLProcessor;
import net.kardexo.ts3bot.msgproc.url.IURLProcessor;
import net.kardexo.ts3bot.msgproc.url.SteamURLProcessor;
import net.kardexo.ts3bot.msgproc.url.TwitchURLProcessor;
import net.kardexo.ts3bot.msgproc.url.TwitterURLProcessor;
import net.kardexo.ts3bot.msgproc.url.Watch2GetherURLProcessor;
import net.kardexo.ts3bot.msgproc.url.YouTubeURLProcessor;
import net.kardexo.ts3bot.util.Util;

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
	public void process(TS3Bot bot, String message, int invokerId, TextMessageTargetMode targetMode)
	{
		String response = URLMessageProcessor.response(message, true);
		
		if(response != null)
		{
			bot.getApi().sendTextMessage(targetMode, invokerId, response);
		}
	}
	
	@Override
	public boolean isApplicable(TS3Bot bot, String message, int invokerId, TextMessageTargetMode targetMode)
	{
		return targetMode == TextMessageTargetMode.CHANNEL && invokerId != bot.getId() && invokerId != -1;
	}
	
	public static String response(String message, boolean checkHistory)
	{
		if(!checkHistory || TS3Bot.getInstance().getChatHistory().appendAndCheckIfNew(message, 10000))
		{
			return URLMessageProcessor.processMessage(message);
		}
		
		return null;
	}
	
	private static String processMessage(String message)
	{
		String url = Util.extract(message);
		
		if(url != null)
		{
			for(IURLProcessor processor : URL_PROCESSORS)
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
			
			String result = DEFAULT_URL_PROCESSOR.process(url);
			
			if(result != null && !result.isBlank())
			{
				return result;
			}
		}
		
		return null;
	}
}
