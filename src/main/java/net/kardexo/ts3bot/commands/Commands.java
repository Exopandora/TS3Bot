package net.kardexo.ts3bot.commands;

import java.util.List;
import java.util.regex.Pattern;

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
	
	public static String searchHistory(Pattern pattern, List<String> history)
	{
		for(String message : history)
		{
			if(pattern.matcher(message).matches())
			{
				return message;
			}
		}
		
		return null;
	}
}
