package net.kardexo.bot.domain.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.kardexo.bot.api.IAPIKeyService;
import net.kardexo.bot.api.IConfigService;
import net.kardexo.bot.api.IEconomyService;
import net.kardexo.bot.api.IPermissionService;
import net.kardexo.bot.api.IUserConfigService;
import net.kardexo.bot.domain.chat.message.IURLMessageProcessor;
import net.kardexo.bot.domain.client.IBotClient;
import net.kardexo.bot.domain.config.Config;

import java.util.ServiceLoader;

public interface ICommandRegistrar
{
	ServiceLoader<ICommandRegistrar> INSTANCE = ServiceLoader.load(ICommandRegistrar.class);
	
	void register
	(
		CommandDispatcher<CommandSource> dispatcher,
		IBotClient bot,
		IConfigService<? extends Config> configService,
		IAPIKeyService apiKeyService,
		IPermissionService permissionService,
		IEconomyService economyService,
		IUserConfigService userConfigService,
		IURLMessageProcessor urlMessageProcessor
	);
}
