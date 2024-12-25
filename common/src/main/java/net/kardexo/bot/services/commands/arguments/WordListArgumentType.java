package net.kardexo.bot.services.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class WordListArgumentType implements ArgumentType<String[]>
{
	@Override
	public String[] parse(StringReader reader) throws CommandSyntaxException
	{
		String text = reader.getRemaining();
		reader.setCursor(reader.getTotalLength());
		return text.split(" +");
	}
	
	public static WordListArgumentType list()
	{
		return new WordListArgumentType();
	}
	
	public static String[] getList(final CommandContext<?> context, final String name)
	{
		return context.getArgument(name, String[].class);
	}
}
