package net.kardexo.ts3bot.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;

public class BotCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("bot")
				.executes(context -> bot(context)));
	}
	
	private static int bot(CommandContext<CommandSource> context) throws CommandSyntaxException
	{
		context.getSource().sendPrivateMessage("KardExo TS3 Bot");
		return 0;
	}
}
