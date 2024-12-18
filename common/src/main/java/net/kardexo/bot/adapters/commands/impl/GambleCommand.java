package net.kardexo.bot.adapters.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import net.kardexo.bot.adapters.commands.CommandSource;
import net.kardexo.bot.adapters.commands.Commands;
import net.kardexo.bot.services.api.IEconomyService;

public class GambleCommand
{
	private static final DynamicCommandExceptionType NOT_ENOUGH_COINS = new DynamicCommandExceptionType(currency -> new LiteralMessage("You do not have enough " + currency));
	
	public static void register(CommandDispatcher<CommandSource> dispatcher, IEconomyService economyService)
	{
		CommandNode<CommandSource> gamble = dispatcher.register(Commands.literal("gamble")
			.then(Commands.argument("amount", IntegerArgumentType.integer(5))
				.executes(context -> gamble(context, economyService, IntegerArgumentType.getInteger(context, "amount"), 0.5D))
				.then(Commands.argument("win_chance", DoubleArgumentType.doubleArg(Double.MIN_VALUE, 1.0D))
					.executes(context -> gamble(context, economyService, IntegerArgumentType.getInteger(context, "amount"), DoubleArgumentType.getDouble(context, "win_chance"))))));
		dispatcher.register(Commands.literal("g").redirect(gamble));
	}
	
	private static int gamble(CommandContext<CommandSource> context, IEconomyService economyService, long amount, double winpct) throws CommandSyntaxException
	{
		if(winpct >= 1.0D)
		{
			throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.doubleTooHigh().create(winpct, 1.0D);
		}
		
		String uuid = context.getSource().getClient().getId();
		String currency = economyService.getCurrency();
		
		if(!economyService.hasCoins(uuid, amount))
		{
			throw NOT_ENOUGH_COINS.create(economyService.getCurrency());
		}
		
		if(context.getSource().getRandomSource().nextDouble(1.0D) < winpct)
		{
			double multiplier = (1.0D / winpct) - 1;
			long win = (long) Math.floor(amount * multiplier);
			economyService.add(uuid, win);
			context.getSource().sendFeedback("You won " + win + currency + " (multiplier: " + multiplier + "). New balance: " + economyService.get(uuid) + currency);
			return (int) amount;
		}
		else
		{
			economyService.subtract(uuid, amount);
			context.getSource().sendFeedback("You lost " + amount + currency + ". New balance: " + economyService.get(uuid) + currency);
			return (int) -amount;
		}
	}
}
