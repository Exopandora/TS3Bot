package net.kardexo.bot.services.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.kardexo.bot.domain.CommandSource;
import net.kardexo.bot.services.commands.Commands;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.services.api.IPermissionService;

public class SilentCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher, IPermissionService permissionService)
	{
		dispatcher.register(Commands.literal("silent")
			.requires(source -> permissionService.hasPermission(source.getClient(), "admin"))
			.executes(SilentCommand::toggleSilent));
	}
	
	private static int toggleSilent(CommandContext<CommandSource> context)
	{
		IBotClient bot = context.getSource().getBot();
		
		if(bot.isSilent())
		{
			bot.setSilent(false);
			return 0;
		}
		
		bot.setSilent(true);
		return 1;
	}
}
