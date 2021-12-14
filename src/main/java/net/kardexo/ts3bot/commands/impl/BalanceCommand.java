package net.kardexo.ts3bot.commands.impl;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.github.theholywaffle.teamspeak3.api.wrapper.ClientInfo;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;
import net.kardexo.ts3bot.util.Util;

public class BalanceCommand
{
	private static final DynamicCommandExceptionType USERNAME_NOT_FOUND = new DynamicCommandExceptionType(username -> new LiteralMessage("Could not find user " + username));
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("balance")
				.executes(context -> balance(context))
				.then(Commands.literal("set")
						.requires(source -> source.hasPermission("admin"))
						.then(Commands.argument("value", IntegerArgumentType.integer(0))
								.executes(context -> set(context, IntegerArgumentType.getInteger(context, "value")))
								.then(Commands.argument("username", StringArgumentType.greedyString())
										.executes(context -> set(context, IntegerArgumentType.getInteger(context, "value"), StringArgumentType.getString(context, "username"))))))
				.then(Commands.argument("username", StringArgumentType.greedyString())
						.executes(context -> balance(context, StringArgumentType.getString(context, "username")))));
	}
	
	private static int set(CommandContext<CommandSource> context, int value) throws CommandSyntaxException
	{
		ClientInfo client = context.getSource().getClientInfo();
		TS3Bot.getInstance().getCoinManager().set(client.getUniqueIdentifier(), value);
		context.getSource().sendFeedback("Set coins for " + client.getNickname() + " to " + value);
		return value;
	}
	
	private static int set(CommandContext<CommandSource> context, int value, String username) throws CommandSyntaxException
	{
		Client client = Util.clientByUsername(username);
		
		if(client == null)
		{
			throw USERNAME_NOT_FOUND.create(username);
		}
		
		String uuid = client.getUniqueIdentifier();
		TS3Bot.getInstance().getCoinManager().set(uuid, value);
		context.getSource().sendFeedback("Set coins for " + client.getNickname() + " to " + value);
		return value;
	}
	
	private static int balance(CommandContext<CommandSource> context) throws CommandSyntaxException
	{
		ClientInfo client = context.getSource().getClientInfo();
		long coins = TS3Bot.getInstance().getCoinManager().get(client.getUniqueIdentifier());
		context.getSource().sendFeedback(client.getNickname() + " has " + coins + " coins");
		return (int) coins;
	}
	
	private static int balance(CommandContext<CommandSource> context, String username) throws CommandSyntaxException
	{
		Client client = Util.clientByUsername(username);
		
		if(client == null)
		{
			throw USERNAME_NOT_FOUND.create(username);
		}
		
		long coins = TS3Bot.getInstance().getCoinManager().get(client.getUniqueIdentifier());
		context.getSource().sendFeedback(client.getNickname() + " has " + coins + " coins");
		return (int) coins;
	}
}
