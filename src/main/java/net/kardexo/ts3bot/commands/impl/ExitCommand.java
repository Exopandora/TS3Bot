package net.kardexo.ts3bot.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;

public class ExitCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("exit")
				.requires(source -> source.hasPermission("admin"))
				.executes(context -> exit(context)));
	}
	
	private static int exit(CommandContext<CommandSource> context) throws CommandSyntaxException
	{
		System.exit(0);
		return 0;
	}
}
