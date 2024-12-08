package net.kardexo.ts3bot.commands.impl;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.github.theholywaffle.teamspeak3.api.wrapper.ClientInfo;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;
import net.kardexo.ts3bot.commands.arguments.ClientArgumentType;

public class BalanceCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher, TS3Bot ts3bot)
	{
		dispatcher.register(Commands.literal("balance")
			.executes(context -> balance(context))
			.then(Commands.literal("set")
				.requires(source -> source.hasPermission("admin"))
				.then(Commands.argument("value", IntegerArgumentType.integer(0))
					.executes(context -> set(context, IntegerArgumentType.getInteger(context, "value")))
					.then(Commands.argument("username", ClientArgumentType.client(ts3bot))
						.executes(context -> set(context, IntegerArgumentType.getInteger(context, "value"), ClientArgumentType.getClient(context, "username"))))))
			.then(Commands.argument("username", ClientArgumentType.client(ts3bot))
				.executes(context -> balance(context, ClientArgumentType.getClient(context, "username")))));
	}
	
	private static int set(CommandContext<CommandSource> context, int value) throws CommandSyntaxException
	{
		ClientInfo client = context.getSource().getClientInfo();
		TS3Bot.getInstance().getEconomyService().set(client.getUniqueIdentifier(), value);
		context.getSource().sendFeedback("Set balance of " + client.getNickname() + " to " + value + TS3Bot.getInstance().getConfig().getCurrency());
		return value;
	}
	
	private static int set(CommandContext<CommandSource> context, int value, Client client) throws CommandSyntaxException
	{
		String uuid = client.getUniqueIdentifier();
		TS3Bot.getInstance().getEconomyService().set(uuid, value);
		context.getSource().sendFeedback("Set balance of " + client.getNickname() + " to " + value + TS3Bot.getInstance().getConfig().getCurrency());
		return value;
	}
	
	private static int balance(CommandContext<CommandSource> context) throws CommandSyntaxException
	{
		ClientInfo client = context.getSource().getClientInfo();
		long coins = TS3Bot.getInstance().getEconomyService().get(client.getUniqueIdentifier());
		context.getSource().sendFeedback(client.getNickname() + " has " + coins + TS3Bot.getInstance().getConfig().getCurrency());
		return (int) coins;
	}
	
	private static int balance(CommandContext<CommandSource> context, Client client) throws CommandSyntaxException
	{
		long coins = TS3Bot.getInstance().getEconomyService().get(client.getUniqueIdentifier());
		context.getSource().sendFeedback(client.getNickname() + " has " + coins + TS3Bot.getInstance().getConfig().getCurrency());
		return (int) coins;
	}
}
