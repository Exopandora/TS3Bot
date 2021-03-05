package net.kardexo.ts3bot.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;

public class CommandSay
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("say")
				.requires(source -> source.getClientInfo().getId() == TS3Bot.getInstance().getId())
				.then(Commands.argument("message", StringArgumentType.greedyString())
						.executes(context -> say(context, StringArgumentType.getString(context, "message")))));
	}
	
	private static int say(CommandContext<CommandSource> context, String message) throws CommandSyntaxException
	{
		TS3Bot.getInstance().getApi().sendChannelMessage(message);
		return 0;
	}
}
