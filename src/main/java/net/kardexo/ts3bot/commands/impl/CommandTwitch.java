package net.kardexo.ts3bot.commands.impl;

import java.util.Map.Entry;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.api.Twitch;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;

public class CommandTwitch
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		for(Entry<String, String> entry : TS3Bot.getInstance().getConfig().getShortcuts().getTwitch().entrySet())
		{
			dispatcher.register(Commands.literal(entry.getKey())
					.executes(context -> CommandTwitch.twitch(context, entry.getValue())));
		}
	}
	
	private static int twitch(CommandContext<CommandSource> context, String user) throws CommandSyntaxException
	{
		context.getSource().sendFeedback(Twitch.details(user, true));
		return 0;
	}
}
