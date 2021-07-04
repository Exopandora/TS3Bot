package net.kardexo.ts3bot.commands.impl;

import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;

public class CommandText
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		for(Entry<String, JsonNode> entry : TS3Bot.getInstance().getConfig().getShortcuts().getText().entrySet())
		{
			dispatcher.register(Commands.literal(entry.getKey())
					.executes(context -> text(context, entry.getValue())));
		}
	}
	
	private static int text(CommandContext<CommandSource> context, JsonNode node) throws CommandSyntaxException
	{
		if(node.isArray())
		{
			int index = TS3Bot.RANDOM.nextInt(node.size());
			context.getSource().sendFeedback(node.get(index).asText());
			return index;
		}
		else
		{
			context.getSource().sendFeedback(node.asText());
			return 0;
		}
	}
}
