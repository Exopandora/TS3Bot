package net.kardexo.ts3bot.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;

public class RandomCommand
{
	private static final DynamicCommandExceptionType INVALID_RANGE = new DynamicCommandExceptionType(range -> new LiteralMessage("Invalid range " + range));
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("random")
				.then(Commands.argument("range", StringArgumentType.greedyString())
						.executes(context -> random(context, StringArgumentType.getString(context, "range")))));
	}
	
	private static int random(CommandContext<CommandSource> context, String argument) throws CommandSyntaxException
	{
		if(argument.matches("\\d+"))
		{
			return RandomCommand.randomBound(context, argument);
		}
		else if(argument.matches("\\d+-\\d+"))
		{
			return RandomCommand.randomRange(context, argument);
		}
		
		return RandomCommand.randomWord(context, argument);
	}
	
	private static int randomRange(CommandContext<CommandSource> context, String argument) throws CommandSyntaxException
	{
		String[] split = argument.split("-", 2);
		int min = RandomCommand.parseInt(split[0]);
		int max = RandomCommand.parseInt(split[1]);
		
		if(max < min)
		{
			throw INVALID_RANGE.create(argument);
		}
		
		int result = min + TS3Bot.RANDOM.nextInt(max - min + 1);
		context.getSource().sendFeedback(String.valueOf(result));
		return result;
	}
	
	private static int randomBound(CommandContext<CommandSource> context, String argument) throws CommandSyntaxException
	{
		int max = RandomCommand.parseInt(argument);
		int result = TS3Bot.RANDOM.nextInt(max + 1);
		context.getSource().sendFeedback(String.valueOf(result));
		return result;
	}
	
	private static int randomWord(CommandContext<CommandSource> context, String argument) throws CommandSyntaxException
	{
		String[] split = argument.split(" +");
		int rand = TS3Bot.RANDOM.nextInt(split.length);
		context.getSource().sendFeedback(split[rand]);
		return rand + 1;
	}
	
	private static int parseInt(String integer) throws CommandSyntaxException
	{
		try
		{
			return Integer.parseInt(integer);
		}
		catch(NumberFormatException e)
		{
			throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidInt().create(integer);
		}
	}
}
