package net.kardexo.ts3bot.commands.impl;

import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;

public class RulesCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("rules")
			.executes(context -> rules(context)));
	}
	
	private static int rules(CommandContext<CommandSource> context) throws CommandSyntaxException
	{
		StringBuilder builder = new StringBuilder();
		List<String> rules = TS3Bot.getInstance().getConfig().getRules();
		
		for(int x = 0; x < rules.size(); x++)
		{
			builder.append("\n" + (x + 1) + ". " + rules.get(x));
		}
		
		context.getSource().sendFeedback(builder.toString());
		return 1;
	}
}
