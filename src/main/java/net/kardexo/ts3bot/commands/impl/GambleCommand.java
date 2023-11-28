package net.kardexo.ts3bot.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;
import net.kardexo.ts3bot.util.CoinManager;

public class GambleCommand
{
	private static final SimpleCommandExceptionType NOT_ENOUGH_COINS = new SimpleCommandExceptionType(new LiteralMessage("You do not have enough " + TS3Bot.getInstance().getConfig().getCurrency().trim()));
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		CommandNode<CommandSource> gamble = dispatcher.register(Commands.literal("gamble")
			.then(Commands.argument("amount", IntegerArgumentType.integer(1))
				.executes(context -> gamble(context, IntegerArgumentType.getInteger(context, "gamble")))));
		
		dispatcher.register(Commands.literal("g").redirect(gamble));
	}
	
	private static int gamble(CommandContext<CommandSource> context, long amount) throws CommandSyntaxException
	{
		CoinManager manager = TS3Bot.getInstance().getCoinManager();
		String uuid = context.getSource().getClientInfo().getUniqueIdentifier();
		String currency = TS3Bot.getInstance().getConfig().getCurrency();
		
		if(!manager.hasCoins(uuid, amount))
		{
			throw NOT_ENOUGH_COINS.create();
		}
		
		if(TS3Bot.RANDOM.nextBoolean())
		{
			manager.add(uuid, amount);
			context.getSource().sendFeedback("You won " + amount + currency + ". New balance: " + manager.get(uuid) + currency);
			return (int) amount;
		}
		else
		{
			manager.subtract(uuid, amount);
			context.getSource().sendFeedback("You lost " + amount + currency + ". New balance: " + manager.get(uuid) + currency);
			return (int) -amount;
		}
	}
}
