package net.kardexo.bot.services.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.kardexo.bot.domain.CommandSource;
import net.kardexo.bot.services.commands.Commands;
import net.kardexo.bot.services.api.commands.arguments.ClientArgumentType;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.services.api.IEconomyService;
import net.kardexo.bot.services.api.IPermissionService;

public class BalanceCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher, IBotClient bot, IPermissionService permissionService, IEconomyService economyService)
	{
		dispatcher.register(Commands.literal("balance")
			.executes(context -> balance(context, economyService))
			.then(Commands.literal("set")
				.requires(source -> permissionService.hasPermission(source.getClient(), "admin"))
				.then(Commands.argument("value", IntegerArgumentType.integer(0))
					.executes(context -> set(context, economyService, IntegerArgumentType.getInteger(context, "value")))
					.then(Commands.argument("username", ClientArgumentType.client(bot))
						.executes(context -> set(context, economyService, IntegerArgumentType.getInteger(context, "value"), ClientArgumentType.getClient(context, "username"))))))
			.then(Commands.argument("username", ClientArgumentType.client(bot))
				.executes(context -> balance(context, economyService, ClientArgumentType.getClient(context, "username")))));
	}
	
	private static int set(CommandContext<CommandSource> context, IEconomyService economyService, int value)
	{
		IClient client = context.getSource().getClient();
		economyService.set(client.getId(), value);
		context.getSource().sendFeedback("Set balance of " + client.getName() + " to " + value + economyService.getCurrency());
		return value;
	}
	
	private static int set(CommandContext<CommandSource> context, IEconomyService economyService, int value, IClient target)
	{
		String uuid = target.getId();
		economyService.set(uuid, value);
		context.getSource().sendFeedback("Set balance of " + target.getName() + " to " + value + economyService.getCurrency());
		return value;
	}
	
	private static int balance(CommandContext<CommandSource> context, IEconomyService economyService)
	{
		IClient client = context.getSource().getClient();
		long coins = economyService.get(client.getId());
		context.getSource().sendFeedback(client.getName() + " has " + coins + economyService.getCurrency());
		return (int) coins;
	}
	
	private static int balance(CommandContext<CommandSource> context, IEconomyService economyService, IClient client)
	{
		long coins = economyService.get(client.getId());
		context.getSource().sendFeedback(client.getName() + " has " + coins + economyService.getCurrency());
		return (int) coins;
	}
}
