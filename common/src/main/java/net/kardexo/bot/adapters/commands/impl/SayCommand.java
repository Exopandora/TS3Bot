package net.kardexo.bot.adapters.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.kardexo.bot.adapters.commands.CommandSource;
import net.kardexo.bot.adapters.commands.Commands;
import net.kardexo.bot.domain.api.IBotClient;

public class SayCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("say")
			.requires(source -> source.getClient().equals(source.getBot()))
			.then(Commands.argument("message", StringArgumentType.greedyString())
				.executes(context -> say(context, StringArgumentType.getString(context, "message")))));
	}
	
	private static int say(CommandContext<CommandSource> context, String message)
	{
		IBotClient bot = context.getSource().getBot();
		bot.sendChannelMessage(message);
		return 0;
	}
}
