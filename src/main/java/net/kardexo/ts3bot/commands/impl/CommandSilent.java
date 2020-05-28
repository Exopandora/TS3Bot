package net.kardexo.ts3bot.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;

public class CommandSilent
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("silent")
				.requires(source -> source.hasPermission("admin"))
				.executes(context -> silent(context)));
	}
	
	private static int silent(CommandContext<CommandSource> context) throws CommandSyntaxException
	{
		if(TS3Bot.getInstance().isSilent())
		{
			TS3Bot.getInstance().setSilent(false);
			return 0;
		}
		else
		{
			TS3Bot.getInstance().setSilent(true);
			return 1;
		}
	}
}
