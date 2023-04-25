package net.kardexo.ts3bot.commands.impl;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;
import net.kardexo.ts3bot.commands.arguments.TS3UserArgumentType;

public class TransferCommand
{
	private static final SimpleCommandExceptionType NOT_ENOUGH_COINS = new SimpleCommandExceptionType(new LiteralMessage("You do not have enough " + TS3Bot.getInstance().getConfig().getCurrency().trim()));
	
	public static void register(CommandDispatcher<CommandSource> dispatcher, TS3Bot ts3bot)
	{
		dispatcher.register(Commands.literal("transfer")
			.then(Commands.argument("amount", IntegerArgumentType.integer(0))
				.then(Commands.argument("beneficiary", TS3UserArgumentType.client(ts3bot))
					.executes(context -> transfer(context, IntegerArgumentType.getInteger(context, "amount"), TS3UserArgumentType.getClient(context, "beneficiary"))))));
	}
	
	private static int transfer(CommandContext<CommandSource> context, int amount, Client beneficiary) throws CommandSyntaxException
	{
		if(!TS3Bot.getInstance().getCoinManager().transfer(context.getSource().getClientInfo().getUniqueIdentifier(), beneficiary.getUniqueIdentifier(), amount))
		{
			throw NOT_ENOUGH_COINS.create();
		}
		
		context.getSource().sendFeedback("You transferred " + amount + TS3Bot.getInstance().getConfig().getCurrency() + " to " + beneficiary.getNickname());
		return amount;
	}
}
