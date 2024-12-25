package net.kardexo.bot.services.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.kardexo.bot.domain.CommandSource;
import net.kardexo.bot.services.commands.Commands;

import java.util.Collections;
import java.util.List;

import static net.kardexo.bot.domain.Util.getClientNamesInChannel;

public class PlayCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("play")
			.executes(PlayCommand::play));
	}
	
	private static int play(CommandContext<CommandSource> context)
	{
		List<String> usernames = getClientNamesInChannel(context.getSource().getChannel());
		Collections.shuffle(usernames, context.getSource().getRandomSource());
		StringBuilder builder = new StringBuilder("The results are in:");
		
		for(int x = 0; x < usernames.size(); x++)
		{
			builder.append("\n#");
			builder.append(x + 1);
			builder.append(": ");
			builder.append(usernames.get(x));
		}
		
		context.getSource().sendFeedback(builder.toString());
		return usernames.size();
	}
}
