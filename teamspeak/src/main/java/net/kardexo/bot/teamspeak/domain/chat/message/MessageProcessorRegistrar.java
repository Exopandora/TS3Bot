package net.kardexo.bot.teamspeak.domain.chat.message;

import net.kardexo.bot.api.IAPIKeyService;
import net.kardexo.bot.api.IConfigService;
import net.kardexo.bot.api.IEconomyService;
import net.kardexo.bot.api.IPermissionService;
import net.kardexo.bot.api.IUserConfigService;
import net.kardexo.bot.domain.chat.message.IMessageProcessor;
import net.kardexo.bot.domain.chat.message.IMessageProcessorRegistrar;
import net.kardexo.bot.domain.chat.message.IURLMessageProcessor;
import net.kardexo.bot.domain.client.IBotClient;
import net.kardexo.bot.domain.config.Config;

import java.util.List;
import java.util.Random;

public class MessageProcessorRegistrar implements IMessageProcessorRegistrar {
	@Override
	public void register(
		List<IMessageProcessor> registrar,
		IBotClient bot,
		IConfigService<? extends Config> configService,
		IAPIKeyService apiKeyService,
		IPermissionService permissionService,
		IEconomyService economyService,
		IUserConfigService userConfigService,
		IURLMessageProcessor urlMessageProcessor,
		Random random
	) {
		registrar.add(urlMessageProcessor);
	}
}
