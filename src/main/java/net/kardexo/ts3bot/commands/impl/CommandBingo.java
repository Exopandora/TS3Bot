package net.kardexo.ts3bot.commands.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import com.fasterxml.jackson.databind.JsonNode;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;

public class CommandBingo
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("bingo")
				.requires(source -> source.getClientInfo().getId() != TS3Bot.getInstance().getId())
				.executes(context -> bingo(context)));
	}
	
	private static int bingo(CommandContext<CommandSource> context)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		long seed = Math.abs(calendar.getTimeInMillis() ^ context.getSource().getClientInfo().getUniqueIdentifier().hashCode());
		Random random = new Random(seed);
		ArrayList<JsonNode> items = new ArrayList<JsonNode>(TS3Bot.getInstance().getConfig().getBingoItems());
		StringBuilder builder = new StringBuilder("Ticket-ID " + seed);
		
		for(int x = 0; x < TS3Bot.getInstance().getConfig().getBingoTicketSize(); x++)
		{
			int index = random.nextInt(items.size());
			JsonNode node = items.get(index);
			
			if(node.isArray())
			{
				if(node.size() == 1)
				{
					builder.append("\n" + node.get(0).asText());
				}
				else if(node.size() > 1)
				{
					index = random.nextInt(node.size());
					builder.append("\n" + node.get(index).asText());
				}
				else if(node.size() == 0)
				{
					builder.append("\n[Missing]");
				}
			}
			else
			{
				builder.append("\n" + node.asText());
			}
			
			items.remove(index);
		}
		
		context.getSource().sendFeedback(builder.toString());
		return (int) seed;
	}
}
