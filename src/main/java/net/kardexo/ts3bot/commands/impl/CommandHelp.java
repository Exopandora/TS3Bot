package net.kardexo.ts3bot.commands.impl;

import java.util.Map;
import java.util.Map.Entry;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;

import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;

public class CommandHelp
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("help")
				.executes(context -> help(context, dispatcher)));
	}
	
	private static int help(CommandContext<CommandSource> context, CommandDispatcher<CommandSource> dispatcher) throws CommandSyntaxException
	{
		Map<CommandNode<CommandSource>, String> usage = dispatcher.getSmartUsage(dispatcher.getRoot(), context.getSource());
		StringBuilder builder = new StringBuilder();
		
		for(Entry<CommandNode<CommandSource>, String> command : usage.entrySet())
		{
			if(command.getKey().canUse(context.getSource()))
			{
				builder.append("\n!" + command.getValue());
			}
		}
		
		context.getSource().sendFeedback(builder.toString());
		return 0;
	}
}
