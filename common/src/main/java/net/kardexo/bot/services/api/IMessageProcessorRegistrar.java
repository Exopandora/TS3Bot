package net.kardexo.bot.services.api;

import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.config.Config;

import java.util.List;
import java.util.Random;
import java.util.ServiceLoader;

public interface IMessageProcessorRegistrar
{
	ServiceLoader<IMessageProcessorRegistrar> INSTANCE = ServiceLoader.load(IMessageProcessorRegistrar.class);
	
	void register
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
	);
}
