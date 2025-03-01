package net.kardexo.bot.services.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import net.kardexo.bot.domain.CommandSource;
import net.kardexo.bot.domain.config.Config;
import net.kardexo.bot.services.commands.Commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HelpCommand
{
	private static final DynamicCommandExceptionType UNKNOWN_COMMAND = new DynamicCommandExceptionType(command -> new LiteralMessage("Unknown command " + command));
	
	public static void register(CommandDispatcher<CommandSource> dispatcher, Config config)
	{
		dispatcher.register(Commands.literal("help")
			.executes(context -> help(context, dispatcher, config))
			.then(Commands.argument("command", StringArgumentType.greedyString())
				.executes(context -> help(context, dispatcher, config, StringArgumentType.getString(context, "command")))));
	}
	
	private static int help(CommandContext<CommandSource> context, CommandDispatcher<CommandSource> dispatcher, Config config)
	{
		Map<CommandNode<CommandSource>, String> usage = dispatcher.getSmartUsage(dispatcher.getRoot(), context.getSource());
		StringBuilder builder = new StringBuilder();
		
		for(Entry<CommandNode<CommandSource>, String> command : usage.entrySet())
		{
			if(command.getKey().canUse(context.getSource()))
			{
				builder.append("\n");
				builder.append(config.getCommandPrefix());
				builder.append(command.getValue());
			}
		}
		
		context.getSource().sendFeedback(builder.toString());
		return 0;
	}
	
	private static int help(CommandContext<CommandSource> context, CommandDispatcher<CommandSource> dispatcher, Config config, String command) throws CommandSyntaxException
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
		
		List<CommandNode<CommandSource>> nodes = parse.getContext().getLastChild().getNodes().stream()
			.map(ParsedCommandNode::getNode)
			.toList();
		StringBuilder builder = new StringBuilder("Usage: " + config.getCommandPrefix() + command);
		
		if(!nodes.isEmpty())
		{
			HelpCommand.appendAllUsage(builder, dispatcher, nodes, context.getSource());
		}
		
		context.getSource().sendFeedback(builder.toString());
		return 0;
	}
	
	public static void appendAllUsage(StringBuilder builder, CommandDispatcher<CommandSource> dispatcher, List<CommandNode<CommandSource>> nodes, CommandSource source)
	{
		List<String> usages = new ArrayList<String>();
		
		for(String usage : dispatcher.getAllUsage(nodes.getLast(), source, true))
		{
			if(!usage.isEmpty())
			{
				usages.add(usage);
			}
		}
		
		if(!usages.isEmpty())
		{
			builder.append(" [");
			builder.append(String.join(", ", usages));
			builder.append("]");
		}
	}
}
