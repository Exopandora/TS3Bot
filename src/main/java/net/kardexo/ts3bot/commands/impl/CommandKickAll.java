package net.kardexo.ts3bot.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;
import net.kardexo.ts3bot.util.TS3Utils;

public class CommandKickAll
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("kickall")
				.requires(source -> source.hasPermission("admin"))
				.executes(context -> kick(context))
				.then(Commands.argument("message", StringArgumentType.greedyString())
						.executes(context -> kick(context, StringArgumentType.getString(context, "message")))));
	}
	
	private static int kick(CommandContext<CommandSource> context) throws CommandSyntaxException
	{
		return CommandKickAll.kick(context, "");
	}
	
	private static int kick(CommandContext<CommandSource> context, String message) throws CommandSyntaxException
	{
		return TS3Utils.kick(client -> true, message);
	}
}
