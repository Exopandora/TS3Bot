package net.kardexo.bot.domain.chat.message;

import net.kardexo.bot.api.IAPIKeyService;
import net.kardexo.bot.api.IConfigService;
import net.kardexo.bot.api.IEconomyService;
import net.kardexo.bot.api.IPermissionService;
import net.kardexo.bot.api.IUserConfigService;
import net.kardexo.bot.domain.client.IBotClient;
import net.kardexo.bot.domain.config.Config;

import java.util.List;
import java.util.Random;
import java.util.ServiceLoader;

public interface IMessageProcessorRegistrar {
	ServiceLoader<IMessageProcessorRegistrar> INSTANCE = ServiceLoader.load(IMessageProcessorRegistrar.class);
	
	void register(
		List<IMessageProcessor> registrar,
		IBotClient bot,
		IConfigService<? extends Config> configService,
		IAPIKeyService apiKeyService,
		IPermissionService permissionService,
		IEconomyService economyService,
		IUserConfigService userConfigService,
		IURLMessageProcessor urlMessageProcessor,
		Random random
	);
}
