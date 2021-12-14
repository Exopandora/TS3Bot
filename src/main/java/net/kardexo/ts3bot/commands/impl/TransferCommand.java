package net.kardexo.ts3bot.commands.impl;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;
import net.kardexo.ts3bot.util.Util;

public class TransferCommand
{
	private static final DynamicCommandExceptionType USERNAME_NOT_FOUND = new DynamicCommandExceptionType(username -> new LiteralMessage("Could not find user " + username));
	private static final SimpleCommandExceptionType NOT_ENOUGH_COINS = new SimpleCommandExceptionType(new LiteralMessage("You do not have enough " + TS3Bot.getInstance().getConfig().getCurrency().trim()));
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("transfer")
				.then(Commands.argument("amount", IntegerArgumentType.integer(0))
						.then(Commands.argument("beneficiary", StringArgumentType.greedyString())
								.executes(context -> transfer(context, IntegerArgumentType.getInteger(context, "amount"), StringArgumentType.getString(context, "beneficiary"))))));
	}
	
	private static int transfer(CommandContext<CommandSource> context, int amount, String beneficiary) throws CommandSyntaxException
	{
		Client recipient = Util.clientByUsername(beneficiary);
		
		if(recipient == null)
		{
			throw USERNAME_NOT_FOUND.create(beneficiary);
		}
		
		if(!TS3Bot.getInstance().getCoinManager().transfer(context.getSource().getClientInfo().getUniqueIdentifier(), recipient.getUniqueIdentifier(), amount))
		{
			throw NOT_ENOUGH_COINS.create();
		}
		
		context.getSource().sendFeedback("You transferred " + amount + TS3Bot.getInstance().getConfig().getCurrency() + " to " + recipient.getNickname());
		return amount;
	}
}
