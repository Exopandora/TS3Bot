package net.kardexo.bot.services.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.kardexo.bot.domain.CommandSource;
import net.kardexo.bot.domain.config.Config;
import net.kardexo.bot.services.api.IConfigService;
import net.kardexo.bot.services.api.IPermissionService;
import net.kardexo.bot.services.commands.Commands;

public class ReloadCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher, IConfigService<? extends Config> configService, IPermissionService permissionService)
	{
		dispatcher.register(Commands.literal("reload")
			.requires(source -> permissionService.hasPermission(source.getClient(), "admin"))
			.executes(context -> reloadConfig(context, configService)));
	}
	
	private static int reloadConfig(CommandContext<CommandSource> context, IConfigService<? extends Config> configService)
	{
		try
		{
			configService.reload();
			context.getSource().sendFeedback("Config successfully reloaded");
			return 1;
		}
		catch(Exception e)
		{
			context.getSource().sendFeedback("Failed to reload config");
			return 0;
		}
	}
}
