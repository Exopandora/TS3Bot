package net.kardexo.ts3bot.commands.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.databind.JsonNode;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;

public class BingoCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("bingo")
				.requires(source -> source.getClientInfo().getId() != TS3Bot.getInstance().getId())
				.executes(context -> bingo(context))
				.then(Commands.literal("list")
						.executes(context -> list(context))));
	}
	
	private static int bingo(CommandContext<CommandSource> context)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_WEEK, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		long seed = Math.abs(calendar.getTimeInMillis() ^ context.getSource().getClientInfo().getUniqueIdentifier().hashCode());
		Random random = new Random(seed);
		ArrayList<JsonNode> items = new ArrayList<JsonNode>(TS3Bot.getInstance().getConfig().getBingoItems());
		StringBuilder builder = new StringBuilder("\nTicket-ID " + seed);
		
		for(int x = 0; x < TS3Bot.getInstance().getConfig().getBingoTicketSize(); x++)
		{
			int index = random.nextInt(items.size());
			JsonNode node = items.get(index);
			
			if(node.isArray())
			{
				if(node.size() == 1)
				{
					builder.append("\n  " + node.get(0).asText());
				}
				else if(node.size() > 1)
				{
					builder.append("\n  " + node.get(random.nextInt(node.size())).asText());
				}
				else if(node.size() == 0)
				{
					builder.append("\n  [Missing]");
				}
			}
			else
			{
				builder.append("\n  " + node.asText());
			}
			
			items.remove(index);
		}
		
		context.getSource().sendFeedback(builder.toString());
		return (int) seed;
	}
	
	private static int list(CommandContext<CommandSource> context)
	{
		List<JsonNode> items = TS3Bot.getInstance().getConfig().getBingoItems();
		StringBuilder builder = new StringBuilder();
		
		for(JsonNode item : items)
		{
			builder.append("\n");
			
			if(item.isArray())
			{
				for(int x = 0; x < item.size(); x++)
				{
					if(x > 0)
					{
						builder.append(", ");
					}
					
					builder.append(item.get(x).asText());
				}
			}
			else
			{
				builder.append(item.asText());
			}
		}
		
		context.getSource().sendFeedback(builder.toString());
		return items.size();
	}
}
