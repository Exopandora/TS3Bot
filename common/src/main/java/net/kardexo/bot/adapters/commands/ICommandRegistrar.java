package net.kardexo.bot.adapters.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.config.Config;
import net.kardexo.bot.services.api.IAPIKeyService;
import net.kardexo.bot.services.api.IEconomyService;
import net.kardexo.bot.services.api.IPermissionService;
import net.kardexo.bot.services.api.IURLMessageProcessor;
import net.kardexo.bot.services.api.IUserConfigService;

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
