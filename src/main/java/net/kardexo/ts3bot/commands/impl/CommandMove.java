package net.kardexo.ts3bot.commands.impl;

import com.github.theholywaffle.teamspeak3.api.wrapper.Channel;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;

public class CommandMove
{
	private static final DynamicCommandExceptionType CHANNEL_NOT_FOUND = new DynamicCommandExceptionType(input -> new LiteralMessage("Could not find channel \"" + input + "\""));
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("move")
				.requires(source -> source.hasPermission("admin") && source.getClientInfo().getId() != TS3Bot.getInstance().getId())
				.executes(context -> move(context))
				.then(Commands.argument("channel", StringArgumentType.greedyString())
						.executes(context -> move(context, StringArgumentType.getString(context, "channel")))));
	}
	
	private static int move(CommandContext<CommandSource> context) throws CommandSyntaxException
	{
		return move(context, TS3Bot.getInstance().getApi().getChannelInfo(context.getSource().getClientInfo().getChannelId()).getName());
	}
	
	private static int move(CommandContext<CommandSource> context, String name) throws CommandSyntaxException
	{
		Channel channel = TS3Bot.getInstance().getApi().getChannelByNameExact(name, true);
		
		if(channel == null)
		{
			throw CHANNEL_NOT_FOUND.create(name);
		}
		
		if(channel.getId() != TS3Bot.getInstance().getApi().getClientInfo(TS3Bot.getInstance().getId()).getChannelId())
		{
			TS3Bot.getInstance().getApi().moveClient(TS3Bot.getInstance().getId(), channel.getId());
		}
		
		return channel.getId();
	}
}
