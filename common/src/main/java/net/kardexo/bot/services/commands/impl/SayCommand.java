package net.kardexo.bot.services.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.kardexo.bot.domain.CommandSource;
import net.kardexo.bot.services.commands.Commands;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.api.IChannel;
import net.kardexo.bot.domain.api.IConsoleChannel;

import java.util.List;

public class SayCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("say")
			.requires(source -> source.getChannel() instanceof IConsoleChannel && source.getClient().equals(source.getBot()))
			.then(Commands.argument("message", StringArgumentType.greedyString())
				.executes(context -> say(context, StringArgumentType.getString(context, "message")))));
	}
	
	private static int say(CommandContext<CommandSource> context, String message)
	{
		IBotClient bot = context.getSource().getBot();
		IConsoleChannel consoleChannel = (IConsoleChannel) context.getSource().getChannel();
		List<IChannel> broadcastChannels = consoleChannel.getBroadcastChannels();
		
		for(IChannel channel : broadcastChannels)
		{
			bot.sendMessage(channel, message);
		}
		
		return broadcastChannels.size();
	}
}
