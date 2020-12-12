package net.kardexo.ts3bot.commands.impl;

import java.util.Optional;

import com.github.manevolent.ts3j.api.Channel;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;
import net.kardexo.ts3bot.util.TS3Utils;

public class CommandMove
{
	private static final DynamicCommandExceptionType CHANNEL_NOT_FOUND = new DynamicCommandExceptionType(input -> new LiteralMessage("Could not find channel \"" + input + "\""));
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("move")
				.requires(source -> source.hasPermission("admin"))
				.executes(context -> move(context))
				.then(Commands.argument("channel", StringArgumentType.greedyString())
						.executes(context -> move(context, StringArgumentType.getString(context, "channel")))));
	}
	
	private static int move(CommandContext<CommandSource> context) throws CommandSyntaxException
	{
		return move(context, context.getSource().getClient().getChannelId());
	}
	
	private static int move(CommandContext<CommandSource> context, String name) throws CommandSyntaxException
	{
		Optional<Channel> channel = TS3Utils.findChannelByName(name);
		
		if(channel.isEmpty())
		{
			throw CHANNEL_NOT_FOUND.create(name);
		}
		
		return move(context, channel.get().getId());
	}
	
	private static int move(CommandContext<CommandSource> context, int channelId) throws CommandSyntaxException
	{
		TS3Utils.moveClient(TS3Bot.getInstance().getId(), channelId);
		return channelId;
	}
}
