package net.kardexo.ts3bot.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;

public class CommandCoinflip
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("coinflip")
				.executes(context -> coinflip(context)));
	}
	
	private static int coinflip(CommandContext<CommandSource> context) throws CommandSyntaxException
	{
		int result = TS3Bot.RANDOM.nextInt(2);
		
		if(result > 0)
		{
			context.getSource().sendFeedback("heads");
		}
		else
		{
			context.getSource().sendFeedback("tails");
		}
		
		return result;
	}
}
