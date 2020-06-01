package net.kardexo.ts3bot.commands;

import java.util.List;
import java.util.function.Predicate;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

public class Commands
{
	public static LiteralArgumentBuilder<CommandSource> literal(String name)
	{
		return LiteralArgumentBuilder.literal(name);
	}
	
	public static <T> RequiredArgumentBuilder<CommandSource, T> argument(String name, ArgumentType<T> type)
	{
		return RequiredArgumentBuilder.argument(name, type);
	}
	
	public static String searchHistory(Predicate<String> predicate, List<String> history)
	{
		return Commands.searchHistory(predicate, history, 0, history.size());
	}
	
	public static String searchHistory(Predicate<String> predicate, List<String> history, int start, int end)
	{
		end = Math.min(end, history.size());
		
		for(int x = start; x < end; x++)
		{
			String message = history.get(x);
			
			if(predicate.test(message))
			{
				return message;
			}
		}
		
		return null;
	}
}
