package net.kardexo.ts3bot.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;
import net.kardexo.ts3bot.services.EconomyService;

public class GambleCommand
{
	private static final SimpleCommandExceptionType NOT_ENOUGH_COINS = new SimpleCommandExceptionType(new LiteralMessage("You do not have enough " + TS3Bot.getInstance().getConfig().getCurrency().trim()));
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		CommandNode<CommandSource> gamble = dispatcher.register(Commands.literal("gamble")
			.then(Commands.argument("amount", IntegerArgumentType.integer(5))
				.executes(context -> gamble(context, IntegerArgumentType.getInteger(context, "amount"), 0.5D))
				.then(Commands.argument("win_chance", DoubleArgumentType.doubleArg(Double.MIN_VALUE, 1.0D))
					.executes(context -> gamble(context, IntegerArgumentType.getInteger(context, "amount"), DoubleArgumentType.getDouble(context, "win_chance"))))));
		
		dispatcher.register(Commands.literal("g").redirect(gamble));
	}
	
	private static int gamble(CommandContext<CommandSource> context, long amount, double winpct) throws CommandSyntaxException
	{
		if(winpct >= 1.0D)
		{
			throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.doubleTooHigh().create(winpct, 1.0D);
		}
		
		EconomyService manager = TS3Bot.getInstance().getEconomyService();
		String uuid = context.getSource().getClientInfo().getUniqueIdentifier();
		String currency = TS3Bot.getInstance().getConfig().getCurrency();
		
		if(!manager.hasCoins(uuid, amount))
		{
			throw NOT_ENOUGH_COINS.create();
		}
		
		if(TS3Bot.RANDOM.nextDouble(1.0D) < winpct)
		{
			double multiplier = (1.0D / winpct) - 1;
			long win = (long) Math.floor(amount * multiplier);
			manager.add(uuid, win);
			context.getSource().sendFeedback("You won " + win + currency + " (multiplier: " + multiplier + "). New balance: " + manager.get(uuid) + currency);
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
