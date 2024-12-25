package net.kardexo.bot.services.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.kardexo.bot.domain.CommandSource;
import net.kardexo.bot.services.commands.Commands;
import net.kardexo.bot.services.api.IPermissionService;

public class ExitCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher, IPermissionService permissionService)
	{
		dispatcher.register(Commands.literal("exit")
			.requires(source -> permissionService.hasPermission(source.getClient(), "admin"))
			.executes(ExitCommand::exit));
	}
	
	private static int exit(CommandContext<CommandSource> context)
	{
		context.getSource().getBot().disconnect();
		System.exit(0);
		return 0;
	}
}
