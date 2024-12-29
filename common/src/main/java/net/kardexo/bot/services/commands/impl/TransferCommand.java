package net.kardexo.bot.services.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.kardexo.bot.domain.CommandSource;
import net.kardexo.bot.services.commands.Commands;
import net.kardexo.bot.services.api.commands.arguments.ClientArgumentType;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.services.api.IEconomyService;

public class TransferCommand
{
	private static final DynamicCommandExceptionType NOT_ENOUGH_COINS = new DynamicCommandExceptionType(currency -> new LiteralMessage("You do not have enough " + currency));
	
	public static void register(CommandDispatcher<CommandSource> dispatcher, IBotClient bot, IEconomyService economyService)
	{
		dispatcher.register(Commands.literal("transfer")
			.then(Commands.argument("amount", IntegerArgumentType.integer(0))
				.then(Commands.argument("beneficiary", ClientArgumentType.client(bot))
					.executes(context -> transfer(context, economyService, IntegerArgumentType.getInteger(context, "amount"), ClientArgumentType.getClient(context, "beneficiary"))))));
	}
	
	private static int transfer(CommandContext<CommandSource> context, IEconomyService economyService, int amount, IClient beneficiary) throws CommandSyntaxException
	{
		if(!economyService.transfer(context.getSource().getClient().getId(), beneficiary.getId(), amount))
		{
			throw NOT_ENOUGH_COINS.create(economyService.getCurrency());
		}
		
		context.getSource().sendFeedback("You transferred " + amount + economyService.getCurrency() + " to " + beneficiary.getName());
		return amount;
	}
}
