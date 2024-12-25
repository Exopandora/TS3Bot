package net.kardexo.bot.services.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.kardexo.bot.domain.CommandSource;
import net.kardexo.bot.services.commands.Commands;
import net.kardexo.bot.services.commands.arguments.IntRangeArgumentType;
import net.kardexo.bot.services.commands.arguments.IntRangeArgumentType.IntRange;
import net.kardexo.bot.services.commands.arguments.WordListArgumentType;

public class RandomCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		LiteralCommandNode<CommandSource> random = dispatcher.register(Commands.literal("random")
			.then(Commands.argument("bound", IntegerArgumentType.integer(1, Integer.MAX_VALUE - 1))
				.executes(context -> randomBound(context, IntegerArgumentType.getInteger(context, "bound"))))
			.then(Commands.argument("range", IntRangeArgumentType.range(0, Integer.MAX_VALUE))
				.executes(context -> randomRange(context, IntRangeArgumentType.getRange(context, "range"))))
			.then(Commands.argument("words", WordListArgumentType.list())
				.executes(context -> randomWord(context, WordListArgumentType.getList(context, "words")))));
		
		dispatcher.register(Commands.literal("r").redirect(random));
	}
	
	private static int randomBound(CommandContext<CommandSource> context, int bound)
	{
		int result = context.getSource().getRandomSource().nextInt(bound) + 1;
		context.getSource().sendFeedback(String.valueOf(result));
		return result;
	}
	
	private static int randomRange(CommandContext<CommandSource> context, IntRange range)
	{
		int result = range.lowerBound() + context.getSource().getRandomSource().nextInt(range.upperBound() - range.lowerBound() + 1);
		context.getSource().sendFeedback(String.valueOf(result));
		return result;
	}
	
	private static int randomWord(CommandContext<CommandSource> context, String[] words)
	{
		int index = context.getSource().getRandomSource().nextInt(words.length);
		context.getSource().sendFeedback(words[index]);
		return index;
	}
}
