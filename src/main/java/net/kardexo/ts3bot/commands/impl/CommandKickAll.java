package net.kardexo.ts3bot.commands.impl;

import java.util.List;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;

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
		List<Client> clients = TS3Bot.getInstance().getApi().getClients();
		TS3Bot.getInstance().getApi().kickClientFromServer((Client[]) clients.toArray());
		return clients.size();
	}
	
	private static int kick(CommandContext<CommandSource> context, String message) throws CommandSyntaxException
	{
		List<Client> clients = TS3Bot.getInstance().getApi().getClients();
		TS3Bot.getInstance().getApi().kickClientFromServer(message, (Client[]) clients.toArray());
		return clients.size();
	}
}
