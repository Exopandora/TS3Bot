package net.kardexo.ts3bot.message;

import java.util.List;
import java.util.stream.Collectors;

import com.github.theholywaffle.teamspeak3.api.TextMessageTargetMode;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.impl.HelpCommand;

public class CommandMessageProcressor implements IMessageProcessor
{
	@Override
	public void process(TS3Bot bot, String message, int invokerId, TextMessageTargetMode targetMode)
	{
		CommandSource source = new CommandSource(bot.getApi(), bot.getApi().getClientInfo(invokerId == -1 ? bot.getId() : invokerId), targetMode);
		
		if(bot.isSilent() && (!source.hasPermission("admin") || invokerId != -1))
		{
			return;
		}
		
		ParseResults<CommandSource> parse = bot.getCommandDispatcher().parse(message.substring(1), source);
		
		try
		{
			if(parse.getReader().canRead())
			{
				if(parse.getExceptions().size() == 1)
				{
					throw parse.getExceptions().values().iterator().next();
				}
				else if(parse.getContext().getRange().isEmpty())
				{
					throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(parse.getReader());
				}
			}
			
			bot.getCommandDispatcher().execute(parse);
		}
		catch(CommandSyntaxException e)
		{
			if(e.getCursor() != -1)
			{
				List<CommandNode<CommandSource>> nodes = parse.getContext().getLastChild().getNodes().stream()
					.map(ParsedCommandNode::getNode)
					.toList();
				
				if(nodes.isEmpty())
				{
					source.sendFeedback(e.getRawMessage().getString());
				}
				else
				{
					StringBuilder builder = new StringBuilder();
					String command = nodes.stream().map(CommandNode::getName).collect(Collectors.joining(" "));
					HelpCommand.appendAllUsage(builder, bot.getCommandDispatcher(), nodes, source, true);
					source.sendFeedback("Usage: !" + command + builder.toString());
				}
			}
			else
			{
				source.sendFeedback(e.getRawMessage().getString());
			}
		}
	}
	
	@Override
	public boolean isApplicable(TS3Bot bot, String message, int invokerId, TextMessageTargetMode targetMode)
	{
		return message.startsWith("!") && !message.matches("!+");
	}
}
