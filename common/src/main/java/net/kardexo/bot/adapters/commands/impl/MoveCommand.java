package net.kardexo.bot.adapters.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.kardexo.bot.adapters.commands.CommandSource;
import net.kardexo.bot.adapters.commands.Commands;
import net.kardexo.bot.adapters.commands.arguments.ChannelArgumentType;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.api.IChannel;
import net.kardexo.bot.services.api.IPermissionService;

public class MoveCommand
{
	private static final DynamicCommandExceptionType UNABLE_TO_JOIN_CHANNEL = new DynamicCommandExceptionType(channel -> new LiteralMessage("Unable to join channel " + channel));
	
	public static void register(CommandDispatcher<CommandSource> dispatcher, IBotClient bot, IPermissionService permissionService)
	{
		dispatcher.register(Commands.literal("move")
			.requires(source -> permissionService.hasPermission(source.getClient(), "admin"))
			.executes(context -> move(context, context.getSource().getChannel()))
			.then(Commands.argument("channel", ChannelArgumentType.channel(bot))
				.executes(context -> move(context, ChannelArgumentType.getChannel(context, "channel")))));
	}
	
	private static int move(CommandContext<CommandSource> context, IChannel channel) throws CommandSyntaxException
	{
		if(!channel.isJoinable())
		{
			throw UNABLE_TO_JOIN_CHANNEL.create(channel.getName());
		}
		
		context.getSource().getBot().move(context.getSource().getClient(), channel);
		return 0;
	}
}
