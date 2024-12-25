package net.kardexo.bot.services.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.kardexo.bot.domain.CommandSource;
import net.kardexo.bot.services.commands.Commands;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.domain.api.IServer;
import net.kardexo.bot.services.api.IPermissionService;
import org.jetbrains.annotations.Nullable;

public class KickAllCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher, IPermissionService permissionService)
	{
		dispatcher.register(Commands.literal("kickall")
			.requires(source -> source.getChannel().getServer() != null && permissionService.hasPermission(source.getClient(), "admin"))
			.executes(context -> kick(context, null))
			.then(Commands.argument("reason", StringArgumentType.greedyString())
				.executes(context -> kick(context, StringArgumentType.getString(context, "reason")))));
	}
	
	private static int kick(CommandContext<CommandSource> context, @Nullable String reason)
	{
		IBotClient bot = context.getSource().getBot();
		IServer server = context.getSource().getChannel().getServer();
		assert server != null;
		IClient[] clients = server.getClients().stream()
			.filter(client -> !client.equals(bot))
			.toArray(IClient[]::new);
		bot.kick(server, reason, clients);
		return clients.length;
	}
}
