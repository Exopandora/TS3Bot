package net.kardexo.bot.adapters.commands.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.kardexo.bot.adapters.commands.CommandSource;
import net.kardexo.bot.adapters.commands.Commands;
import net.kardexo.bot.domain.config.Config;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class BingoCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher, Config config)
	{
		dispatcher.register(Commands.literal("bingo")
			.requires(source -> !source.getClient().equals(source.getBot()))
			.executes(context -> bingo(context, config.getBingoTicketSize(), config.getBingoItems()))
			.then(Commands.literal("list")
				.executes(context -> list(context, config.getBingoItems()))));
	}
	
	private static int bingo(CommandContext<CommandSource> context, int bingoTicketSize, List<JsonNode> bingoItems)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_WEEK, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		long seed = Math.abs(calendar.getTimeInMillis() ^ context.getSource().getClient().getUniqueId().hashCode());
		Random random = new Random(seed);
		ArrayList<JsonNode> items = new ArrayList<JsonNode>(bingoItems);
		StringBuilder builder = new StringBuilder("\nTicket-ID " + seed);
		
		for(int x = 0; x < bingoTicketSize; x++)
		{
			int index = random.nextInt(items.size());
			JsonNode node = items.get(index);
			
			if(node.isArray())
			{
				if(node.size() == 1)
				{
					builder.append("\n  ");
					builder.append(node.get(0).asText());
				}
				else if(node.size() > 1)
				{
					builder.append("\n  ");
					builder.append(node.get(random.nextInt(node.size())).asText());
				}
				else if(node.isEmpty())
				{
					builder.append("\n  [Missing]");
				}
			}
			else
			{
				builder.append("\n  ");
				builder.append(node.asText());
			}
			
			items.remove(index);
		}
		
		context.getSource().sendFeedback(builder.toString());
		return (int) seed;
	}
	
	private static int list(CommandContext<CommandSource> context, List<JsonNode> bingoItems)
	{
		StringBuilder builder = new StringBuilder();
		
		for(JsonNode item : bingoItems)
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
		return bingoItems.size();
	}
}
