package net.kardexo.ts3bot.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;
import net.kardexo.ts3bot.commands.arguments.IntRangeArgumentType;
import net.kardexo.ts3bot.commands.arguments.IntRangeArgumentType.IntRange;
import net.kardexo.ts3bot.commands.arguments.WordListArgumentType;

public class RandomCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("random")
				.then(Commands.argument("bound", IntegerArgumentType.integer(1, Integer.MAX_VALUE - 1))
						.executes(context -> randomBound(context, IntegerArgumentType.getInteger(context, "bound"))))
				.then(Commands.argument("range", IntRangeArgumentType.range(0, Integer.MAX_VALUE))
						.executes(context -> randomRange(context, IntRangeArgumentType.getRange(context, "range"))))
				.then(Commands.argument("words", WordListArgumentType.list())
						.executes(context -> randomWord(context, WordListArgumentType.getList(context, "words")))));
	}
	
	private static int randomBound(CommandContext<CommandSource> context, int bound) throws CommandSyntaxException
	{
		int result = TS3Bot.RANDOM.nextInt(bound) + 1;
		context.getSource().sendFeedback(String.valueOf(result));
		return result;
	}
	
	private static int randomRange(CommandContext<CommandSource> context, IntRange range) throws CommandSyntaxException
	{
		int result = range.lowerBound() + TS3Bot.RANDOM.nextInt(range.upperBound() - range.lowerBound() + 1);
		context.getSource().sendFeedback(String.valueOf(result));
		return result;
	}
	
	private static int randomWord(CommandContext<CommandSource> context, String[] words) throws CommandSyntaxException
	{
		int index = TS3Bot.RANDOM.nextInt(words.length);
		context.getSource().sendFeedback(words[index]);
		return index;
	}
}
