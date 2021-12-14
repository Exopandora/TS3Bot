package net.kardexo.ts3bot.commands.impl;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;
import net.kardexo.ts3bot.util.Util;

public class KickCommand
{
	private static final DynamicCommandExceptionType USERNAME_NOT_FOUND = new DynamicCommandExceptionType(username -> new LiteralMessage("Could not find user " + username));
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("kick")
				.requires(source -> source.getClientInfo().getId() != TS3Bot.getInstance().getId())
				.executes(context -> kick(context))
				.then(Commands.argument("username", StringArgumentType.greedyString())
						.requires(source -> source.hasPermission("admin"))
						.executes(context -> kick(context, StringArgumentType.getString(context, "username")))));
	}
	
	private static int kick(CommandContext<CommandSource> context) throws CommandSyntaxException
	{
		int clientId = context.getSource().getClientInfo().getId();
		TS3Bot.getInstance().getApi().kickClientFromServer(clientId);
		return clientId;
	}
	
	private static int kick(CommandContext<CommandSource> context, String username) throws CommandSyntaxException
	{
		Client client = Util.clientByUsername(username);
		
		if(client == null)
		{
			throw USERNAME_NOT_FOUND.create(username);
		}
		
		int clientId = client.getId();
		TS3Bot.getInstance().getApi().kickClientFromServer(clientId);
		return clientId;
	}
}