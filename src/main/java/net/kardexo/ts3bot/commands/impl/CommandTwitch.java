package net.kardexo.ts3bot.commands.impl;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.messageprocessors.url.impl.TwitchURLProcessor;

public class CommandTwitch
{
	private static final SimpleCommandExceptionType TWITCH_SERVICE_UNAVAILABLE = new SimpleCommandExceptionType(new LiteralMessage("Twitch service is currently unavailable"));
	
	public static int twitch(CommandContext<CommandSource> context, String user) throws CommandSyntaxException
	{
		try
		{
			context.getSource().sendFeedback(TwitchURLProcessor.twitchDetails(user, true));
		}
		catch(Exception e)
		{
			throw TWITCH_SERVICE_UNAVAILABLE.create();
		}
		
		return 0;
	}
}
