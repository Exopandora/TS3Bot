package net.kardexo.bot.adapters.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.kardexo.bot.adapters.commands.CommandSource;
import net.kardexo.bot.adapters.commands.Commands;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.services.api.IPermissionService;
import org.jetbrains.annotations.Nullable;

public class KickAllCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher, IPermissionService permissionService)
	{
		dispatcher.register(Commands.literal("kickall")
			.requires(source -> permissionService.hasPermission(source.getClient(), "admin"))
			.executes(context -> kick(context, null))
			.then(Commands.argument("reason", StringArgumentType.greedyString())
				.executes(context -> kick(context, StringArgumentType.getString(context, "reason")))));
	}
	
	private static int kick(CommandContext<CommandSource> context, @Nullable String reason)
	{
		IBotClient bot = context.getSource().getBot();
		IClient[] clients = bot.getClients().stream()
			.filter(client -> !client.equals(bot))
			.toArray(IClient[]::new);
		bot.kick(reason, clients);
		return clients.length;
	}
}
