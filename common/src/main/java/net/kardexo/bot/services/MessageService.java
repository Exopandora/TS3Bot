package net.kardexo.bot.services;

import net.kardexo.bot.adapters.commands.CommandMessageProcessor;
import net.kardexo.bot.adapters.web.URLMessageProcessor;
import net.kardexo.bot.domain.ChatHistory;
import net.kardexo.bot.domain.config.Config;
import net.kardexo.bot.services.api.IAPIKeyService;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.services.api.IEconomyService;
import net.kardexo.bot.services.api.IMessageService;
import net.kardexo.bot.services.api.IMessageProcessor;
import net.kardexo.bot.services.api.IPermissionService;
import net.kardexo.bot.services.api.IUserConfigService;
import net.kardexo.bot.domain.api.MessageTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MessageService implements IMessageService
{
	private final List<IMessageProcessor> messageProcessors = new ArrayList<IMessageProcessor>();
	private final IBotClient bot;
	
	public MessageService
	(
		IBotClient bot,
		Config config,
		IAPIKeyService apiKeyService,
		IPermissionService permissionService,
		IEconomyService economyService,
		IUserConfigService userConfigService,
		Random random
	)
	{
		this.bot = bot;
		URLMessageProcessor urlMessageProcessor = new URLMessageProcessor(apiKeyService);
		this.messageProcessors.add(new CommandMessageProcessor(bot, config, apiKeyService, permissionService, economyService, userConfigService, urlMessageProcessor, random));
		this.messageProcessors.add(urlMessageProcessor);
	}
	
	@Override
	public void onMessage(IClient client, String message, MessageTarget target, ChatHistory chatHistory)
	{
		if(this.bot.equals(client) && target != MessageTarget.CONSOLE)
		{
			return;
		}
		
		String msg = message.strip();
		
		if(msg.isEmpty())
		{
			return;
		}
		
		for(IMessageProcessor processor : this.messageProcessors)
		{
			if(processor.isApplicable(this.bot, msg, client, target, chatHistory))
			{
				processor.process(this.bot, msg, client, target, chatHistory);
			}
		}
	}
}
