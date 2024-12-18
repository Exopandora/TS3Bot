package net.kardexo.bot.adapters.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.kardexo.bot.adapters.commands.CommandSource;
import net.kardexo.bot.adapters.commands.Commands;
import net.kardexo.bot.adapters.commands.arguments.ClientArgumentType;
import net.kardexo.bot.adapters.commands.arguments.DurationArgumentType;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.domain.api.IServer;
import net.kardexo.bot.services.api.IPermissionService;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public class BanCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher, IBotClient bot, IPermissionService permissionService)
	{
		dispatcher.register(Commands.literal("ban")
			.requires(source -> source.getChannel().getServer() != null && permissionService.hasPermission(source.getClient(), "admin") && !source.getClient().equals(source.getBot()))
			.then(Commands.argument("username", ClientArgumentType.client(bot))
				.then(Commands.argument("duration", DurationArgumentType.duration())
					.executes(context -> ban(context, ClientArgumentType.getClient(context, "username"), DurationArgumentType.getDuration(context, "duration"), null))
					.then(Commands.argument("reason", StringArgumentType.greedyString())
						.executes(context -> ban(context, ClientArgumentType.getClient(context, "username"), DurationArgumentType.getDuration(context, "duration"), StringArgumentType.getString(context, "reason")))))
				.then(Commands.literal("permanent")
					.executes(context -> ban(context, ClientArgumentType.getClient(context, "username"), Duration.ZERO, null))
					.then(Commands.argument("reason", StringArgumentType.greedyString())
						.executes(context -> ban(context, ClientArgumentType.getClient(context, "username"), Duration.ZERO, StringArgumentType.getString(context, "reason")))))));
	}
	
	private static int ban(CommandContext<CommandSource> context, IClient client, Duration duration, @Nullable String reason)
	{
		IServer server = context.getSource().getChannel().getServer();
		assert server != null;
		context.getSource().getBot().ban(server, reason, duration, client);
		return (int) duration.toSeconds();
	}
}
