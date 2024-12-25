package net.kardexo.bot.services.api;

import com.mojang.brigadier.CommandDispatcher;
import net.kardexo.bot.domain.CommandSource;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.config.Config;

import java.util.ServiceLoader;

public interface ICommandRegistrar
{
	ServiceLoader<ICommandRegistrar> INSTANCE = ServiceLoader.load(ICommandRegistrar.class);
	
	void register
	(
		CommandDispatcher<CommandSource> dispatcher,
		IBotClient bot,
		Config config,
		IAPIKeyService apiKeyService,
		IPermissionService permissionService,
		IEconomyService economyService,
		IUserConfigService userConfigService,
		IURLMessageProcessor urlMessageProcessor
	);
}
