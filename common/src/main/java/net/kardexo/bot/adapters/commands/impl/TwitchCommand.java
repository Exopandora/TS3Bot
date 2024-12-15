package net.kardexo.bot.adapters.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kardexo.bot.adapters.commands.CommandSource;
import net.kardexo.bot.adapters.commands.Commands;
import net.kardexo.bot.adapters.twitch.Twitch;
import net.kardexo.bot.domain.config.Config;
import net.kardexo.bot.services.api.IAPIKeyService;

import java.util.Map.Entry;

public class TwitchCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher, Config config, IAPIKeyService apiKeyService)
	{
		for(Entry<String, String> entry : config.getShortcuts().getTwitch().entrySet())
		{
			dispatcher.register(Commands.literal(entry.getKey())
				.executes(context -> TwitchCommand.twitch(context, apiKeyService, entry.getValue())));
		}
	}
	
	private static int twitch(CommandContext<CommandSource> context, IAPIKeyService apiKeyService, String user) throws CommandSyntaxException
	{
		context.getSource().sendFeedback(Twitch.details(apiKeyService, user, true));
		return 0;
	}
}
