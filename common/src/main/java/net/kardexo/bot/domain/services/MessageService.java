package net.kardexo.bot.domain.services;

import net.kardexo.bot.api.IAPIKeyService;
import net.kardexo.bot.api.IConfigService;
import net.kardexo.bot.api.IEconomyService;
import net.kardexo.bot.api.IMessageService;
import net.kardexo.bot.api.IPermissionService;
import net.kardexo.bot.api.IUserConfigService;
import net.kardexo.bot.domain.channel.IChannel;
import net.kardexo.bot.domain.channel.IConsoleChannel;
import net.kardexo.bot.domain.chat.ChatHistory;
import net.kardexo.bot.domain.chat.message.IMessageProcessor;
import net.kardexo.bot.domain.chat.message.IMessageProcessorRegistrar;
import net.kardexo.bot.domain.client.IBotClient;
import net.kardexo.bot.domain.client.IClient;
import net.kardexo.bot.domain.config.Config;
import net.kardexo.bot.domain.services.commands.CommandMessageProcessor;
import net.kardexo.bot.domain.services.url.URLMessageProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MessageService implements IMessageService {
	private final List<IMessageProcessor> messageProcessors = new ArrayList<IMessageProcessor>();
	private final IBotClient bot;
	
	public MessageService(
		IBotClient bot,
		IConfigService<? extends Config> configService,
		IAPIKeyService apiKeyService,
		IPermissionService permissionService,
		IEconomyService economyService,
		IUserConfigService userConfigService,
		Random random
	) {
		this.bot = bot;
		URLMessageProcessor urlMessageProcessor = new URLMessageProcessor(apiKeyService);
		CommandMessageProcessor commandMessageProcessor = new CommandMessageProcessor(
			bot,
			configService,
			apiKeyService,
			permissionService,
			economyService,
			userConfigService,
			urlMessageProcessor,
			random
		);
		this.messageProcessors.add(commandMessageProcessor);
		for (IMessageProcessorRegistrar registrar : IMessageProcessorRegistrar.INSTANCE) {
			registrar.register(
				this.messageProcessors,
				bot,
				configService,
				apiKeyService,
				permissionService,
				economyService,
				userConfigService,
				urlMessageProcessor,
				random
			);
		}
	}
	
	@Override
	public void onMessage(IChannel channel, IClient client, String message, ChatHistory chatHistory) {
		if (this.bot.equals(client) && !(channel instanceof IConsoleChannel)) {
			return;
		}
		String msg = message.strip();
		if (msg.isEmpty()) {
			return;
		}
		for (IMessageProcessor processor : this.messageProcessors) {
			if (processor.isApplicable(this.bot, channel, client, msg, chatHistory)) {
				processor.process(this.bot, channel, client, msg, chatHistory);
			}
		}
	}
}
