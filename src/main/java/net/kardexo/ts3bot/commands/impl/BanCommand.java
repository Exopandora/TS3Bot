package net.kardexo.ts3bot.commands.impl;

import java.time.Duration;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;
import net.kardexo.ts3bot.commands.arguments.ClientArgumentType;
import net.kardexo.ts3bot.commands.arguments.DurationArgumentType;

public class BanCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher, TS3Bot ts3bot)
	{
		dispatcher.register(Commands.literal("ban")
			.requires(source -> source.hasPermission("admin") && source.getClientInfo().getId() != TS3Bot.getInstance().getId())
			.then(Commands.argument("username", ClientArgumentType.client(ts3bot))
				.then(Commands.argument("duration", DurationArgumentType.duration())
					.executes(context -> ban(context, ClientArgumentType.getClient(context, "username"), DurationArgumentType.getDuration(context, "duration"), null))
					.then(Commands.argument("reason", StringArgumentType.greedyString())
						.executes(context -> ban(context, ClientArgumentType.getClient(context, "username"), DurationArgumentType.getDuration(context, "duration"), StringArgumentType.getString(context, "reason")))))
				.then(Commands.literal("permanent")
					.executes(context -> ban(context, ClientArgumentType.getClient(context, "username"), Duration.ZERO, null))
					.then(Commands.argument("reason", StringArgumentType.greedyString())
						.executes(context -> ban(context, ClientArgumentType.getClient(context, "username"), Duration.ZERO, StringArgumentType.getString(context, "reason")))))));
	}
	
	private static int ban(CommandContext<CommandSource> context, Client client, Duration duration, String reason) throws CommandSyntaxException
	{
		int clientId = client.getId();
		TS3Bot.getInstance().getApi().banClient(clientId, duration.toSeconds());
		return clientId;
	}
}
