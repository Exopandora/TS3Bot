package net.kardexo.bot.services.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.kardexo.bot.domain.CommandSource;
import net.kardexo.bot.services.commands.Commands;
import net.kardexo.bot.domain.config.Config;

import java.util.List;

public class RulesCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher, Config config)
	{
		dispatcher.register(Commands.literal("rules")
			.executes(context -> rules(context, config.getRules())));
	}
	
	private static int rules(CommandContext<CommandSource> context, List<String> rules)
	{
		StringBuilder builder = new StringBuilder();
		
		for(int x = 0; x < rules.size(); x++)
		{
			builder.append("\n");
			builder.append(x + 1);
			builder.append(". ");
			builder.append(rules.get(x));
		}
		
		context.getSource().sendFeedback(builder.toString());
		return 1;
	}
}
