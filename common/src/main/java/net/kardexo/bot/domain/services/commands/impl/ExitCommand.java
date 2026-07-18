package net.kardexo.bot.domain.services.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.kardexo.bot.api.IPermissionService;
import net.kardexo.bot.domain.commands.CommandSource;
import net.kardexo.bot.domain.services.commands.Commands;

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
