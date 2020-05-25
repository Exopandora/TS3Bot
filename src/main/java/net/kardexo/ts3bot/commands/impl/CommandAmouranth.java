package net.kardexo.ts3bot.commands.impl;

import com.mojang.brigadier.CommandDispatcher;

import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;

public class CommandAmouranth
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("amouranth")
				.executes(context -> CommandTwitch.twitch(context, "Amouranth")));
	}
}