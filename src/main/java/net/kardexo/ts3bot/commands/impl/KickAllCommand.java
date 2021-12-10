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

public class KickAllCommand
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
		Client[] clients = KickAllCommand.getClients();
		TS3Bot.getInstance().getApi().kickClientFromServer(clients);
		return clients.length;
	}
	
	private static int kick(CommandContext<CommandSource> context, String message) throws CommandSyntaxException
	{
		Client[] clients = KickAllCommand.getClients();
		TS3Bot.getInstance().getApi().kickClientFromServer(message, clients);
		return clients.length;
	}
	
	private static Client[] getClients()
	{
		List<Client> list = TS3Bot.getInstance().getApi().getClients();
		list.removeIf(client -> client.getId() == TS3Bot.getInstance().getId());
		
		Client[] clients = new Client[list.size()];
		list.toArray(clients);
		
		return clients;
	}
}
