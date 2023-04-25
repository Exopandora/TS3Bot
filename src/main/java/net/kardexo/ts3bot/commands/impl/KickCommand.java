package net.kardexo.ts3bot.commands.impl;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;
import net.kardexo.ts3bot.commands.arguments.TS3UserArgumentType;

public class KickCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher, TS3Bot ts3bot)
	{
		dispatcher.register(Commands.literal("kick")
			.requires(source -> source.getClientInfo().getId() != TS3Bot.getInstance().getId())
			.executes(context -> kick(context))
			.then(Commands.argument("username", TS3UserArgumentType.client(ts3bot))
				.requires(source -> source.hasPermission("admin"))
				.executes(context -> kick(context, TS3UserArgumentType.getClient(context, "username")))));
	}
	
	private static int kick(CommandContext<CommandSource> context) throws CommandSyntaxException
	{
		int clientId = context.getSource().getClientInfo().getId();
		TS3Bot.getInstance().getApi().kickClientFromServer(clientId);
		return clientId;
	}
	
	private static int kick(CommandContext<CommandSource> context, Client client) throws CommandSyntaxException
	{
		int clientId = client.getId();
		TS3Bot.getInstance().getApi().kickClientFromServer(clientId);
		return clientId;
	}
}
