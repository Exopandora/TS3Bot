package net.kardexo.ts3bot.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.ts3bot.Util;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;

public class CommandRandom
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("random")
				.then(Commands.argument("range", StringArgumentType.greedyString())
						.executes(context -> random(context, StringArgumentType.getString(context, "range")))));
	}
	
	private static int random(CommandContext<CommandSource> context, String argument) throws CommandSyntaxException
	{
		if(argument.matches("[0-9]+"))
		{
			int max = Integer.parseInt(argument);
			int result = Util.RANDOM.nextInt(max + 1);
			context.getSource().sendFeedback(String.valueOf(result));
			return result;
		}
		else if(argument.matches("[0-9]+-[0-9]+"))
		{
			String[] split = argument.split("-", 2);
			int min = Integer.parseInt(split[0]);
			int max = Integer.parseInt(split[1]);
			int result = min + Util.RANDOM.nextInt(max - min + 1);
			context.getSource().sendFeedback(String.valueOf(result));
			return result;
		}
		
		String[] split = argument.split(" ");
		int rand = Util.RANDOM.nextInt(split.length);
		context.getSource().sendFeedback(split[rand]);
		return rand + 1;
	}
}
