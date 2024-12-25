package net.kardexo.bot.adapters.messages;

import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.config.Config;
import net.kardexo.bot.services.api.IAPIKeyService;
import net.kardexo.bot.services.api.IEconomyService;
import net.kardexo.bot.services.api.IMessageProcessor;
import net.kardexo.bot.services.api.IMessageProcessorRegistrar;
import net.kardexo.bot.services.api.IPermissionService;
import net.kardexo.bot.services.api.IURLMessageProcessor;
import net.kardexo.bot.services.api.IUserConfigService;

import java.util.List;
import java.util.Random;

public class MessageProcessorRegistrar implements IMessageProcessorRegistrar
{
	@Override
	public void register
	(
		List<IMessageProcessor> registrar,
		IBotClient bot,
		Config config,
		IAPIKeyService apiKeyService,
		IPermissionService permissionService,
		IEconomyService economyService,
		IUserConfigService userConfigService,
		IURLMessageProcessor urlMessageProcessor,
		Random random
	)
	{
		registrar.add(urlMessageProcessor);
	}
}
