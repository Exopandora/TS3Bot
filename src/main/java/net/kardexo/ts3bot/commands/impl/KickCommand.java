package net.kardexo.ts3bot.commands.impl;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;
import net.kardexo.ts3bot.commands.arguments.ClientArgumentType;

public class KickCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher, TS3Bot ts3bot)
	{
		dispatcher.register(Commands.literal("kick")
			.requires(source -> source.getClientInfo().getId() != TS3Bot.getInstance().getId())
			.executes(context -> kick(context, context.getSource().getClientInfo(), null))
			.then(Commands.argument("username", ClientArgumentType.client(ts3bot))
				.requires(source -> source.hasPermission("admin"))
				.executes(context -> kick(context, ClientArgumentType.getClient(context, "username"), null))
				.then(Commands.argument("reason", StringArgumentType.greedyString())
					.executes(context -> kick(context, ClientArgumentType.getClient(context, "username"), StringArgumentType.getString(context, "reason"))))));
	}
	
	private static int kick(CommandContext<CommandSource> context, Client client, String reason) throws CommandSyntaxException
	{
		int clientId = client.getId();
		TS3Bot.getInstance().getApi().kickClientFromServer(reason, client);
		return clientId;
	}
}
