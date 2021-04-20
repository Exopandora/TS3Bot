package net.kardexo.ts3bot.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;
import net.kardexo.ts3bot.messageprocessors.url.impl.TwitchURLProcessor;

public class CommandTwitch
{
	private static final SimpleCommandExceptionType TWITCH_SERVICE_UNAVAILABLE = new SimpleCommandExceptionType(new LiteralMessage("Twitch service is currently unavailable"));
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("bobross")
				.executes(context -> CommandTwitch.twitch(context, "BobRoss")));
		dispatcher.register(Commands.literal("amouranth")
				.executes(context -> CommandTwitch.twitch(context, "Amouranth")));
	}
	
	private static int twitch(CommandContext<CommandSource> context, String user) throws CommandSyntaxException
	{
		String details = TwitchURLProcessor.twitchDetails(user, true);
		
		if(details != null)
		{
			context.getSource().sendFeedback(details);
		}
		else
		{
			throw TWITCH_SERVICE_UNAVAILABLE.create();
		}
		
		return 0;
	}
}
