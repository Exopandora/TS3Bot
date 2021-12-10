package net.kardexo.ts3bot.commands.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;

import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;

public class HelpCommand
{
	private static final DynamicCommandExceptionType UNKNOWN_COMMAND = new DynamicCommandExceptionType(command -> new LiteralMessage("Unknown command " + command));
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("help")
				.executes(context -> help(context, dispatcher))
				.then(Commands.argument("command", StringArgumentType.greedyString())
						.executes(context -> help(context, dispatcher, StringArgumentType.getString(context, "command")))));
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
	
	private static int help(CommandContext<CommandSource> context, CommandDispatcher<CommandSource> dispatcher, String command) throws CommandSyntaxException
	{
		ParseResults<CommandSource> parse = dispatcher.parse(command, context.getSource());
		
		if(parse.getReader().canRead())
		{
			if(parse.getExceptions().size() == 1)
			{
				throw parse.getExceptions().values().iterator().next();
			}
			else if(parse.getContext().getRange().isEmpty())
			{
				throw UNKNOWN_COMMAND.create(command.split(" ")[0]);
			}
		}
		
		Map<CommandNode<CommandSource>, StringRange> nodes = parse.getContext().getLastChild().getNodes();
		StringBuilder builder = new StringBuilder("Usage: !" + command);
		
		parse.getContext().getLastChild().getArguments().entrySet().forEach(entry -> System.out.println(entry.getKey() + " " + entry.getValue()));
		
		if(!nodes.isEmpty())
		{
			HelpCommand.appendAllUsage(builder, dispatcher,  HelpCommand.nodeMapToList(nodes), context.getSource(), true);
		}
		
		context.getSource().sendFeedback(builder.toString());
		return 0;
	}
	
	public static void appendAllUsage(StringBuilder builder, CommandDispatcher<CommandSource> dispatcher, List<CommandNode<CommandSource>> nodes, CommandSource source, boolean restriced)
	{
		List<String> usages = new ArrayList<String>();
		
		for(String usage : dispatcher.getAllUsage(nodes.get(nodes.size() - 1), source, true))
		{
			if(!usage.isEmpty())
			{
				usages.add(usage);
			}
		}
		
		if(!usages.isEmpty())
		{
			builder.append(" [" + String.join(", ", usages) + "]");
		}
	}
	
	public static List<CommandNode<CommandSource>> nodeMapToList(Map<CommandNode<CommandSource>, StringRange> nodes)
	{
		return nodes.entrySet().stream().sorted((a, b) -> Integer.compare(b.getValue().getEnd(), a.getValue().getEnd())).map(Entry::getKey).toList();
	}
}
