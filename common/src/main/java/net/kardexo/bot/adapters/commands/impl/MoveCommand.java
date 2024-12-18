package net.kardexo.bot.adapters.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.kardexo.bot.adapters.commands.CommandSource;
import net.kardexo.bot.adapters.commands.Commands;
import net.kardexo.bot.adapters.commands.arguments.ChannelArgumentType;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.api.IChannel;
import net.kardexo.bot.services.api.IPermissionService;

public class MoveCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher, IBotClient bot, IPermissionService permissionService)
	{
		dispatcher.register(Commands.literal("move")
			.requires(source -> bot.canMove() && permissionService.hasPermission(source.getClient(), "admin") && !source.getClient().equals(source.getBot()))
			.executes(context -> move(context, context.getSource().getChannel()))
			.then(Commands.argument("channel", ChannelArgumentType.channel(bot))
				.executes(context -> move(context, ChannelArgumentType.getChannel(context, "channel")))));
	}
	
	private static int move(CommandContext<CommandSource> context, IChannel channel)
	{
		context.getSource().getBot().move(context.getSource().getClient(), channel);
		return 0;
	}
}
