package net.kardexo.ts3bot.commands.impl;

import java.util.Collections;
import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;
import net.kardexo.ts3bot.util.Util;

public class PlayCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("play")
				.executes(PlayCommand::play));
	}
	
	private static int play(CommandContext<CommandSource> context) throws CommandSyntaxException
	{
		List<String> usernames = Util.getClientNamesInChannel(context.getSource().getClientInfo().getChannelId());
		Collections.shuffle(usernames);
		StringBuilder builder = new StringBuilder("The results are in:");
		
		for(int x = 0; x < usernames.size(); x++)
		{
			builder.append("\n#" + (x + 1) + ": " + usernames.get(x));
		}
		
		context.getSource().sendFeedback(builder.toString());
		return usernames.size();
	}
}
