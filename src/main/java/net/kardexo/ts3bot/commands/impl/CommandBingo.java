package net.kardexo.ts3bot.commands.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

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
		ArrayList<String> items = new ArrayList<String>(TS3Bot.getInstance().getConfig().getBingoItems());
		StringBuilder builder = new StringBuilder("Ticket-ID " + seed);
		
		for(int x = 0; x < TS3Bot.getInstance().getConfig().getBingoTicketSize(); x++)
		{
			int item = random.nextInt(items.size());
			builder.append("\n" + items.get(item));
			items.remove(item);
		}
		
		context.getSource().sendFeedback(builder.toString());
		return (int) seed;
	}
}
