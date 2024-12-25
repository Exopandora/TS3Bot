package net.kardexo.bot.services.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.kardexo.bot.domain.CommandSource;
import net.kardexo.bot.services.commands.Commands;
import net.kardexo.bot.services.commands.arguments.ClientArgumentType;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.domain.api.IServer;
import net.kardexo.bot.services.api.IPermissionService;
import org.jetbrains.annotations.Nullable;

public class KickCommand
{
	private static final DynamicCommandExceptionType INVALID_USER = new DynamicCommandExceptionType(user -> new LiteralMessage("User " + user + " cannot be kicked"));
	
	public static void register(CommandDispatcher<CommandSource> dispatcher, IBotClient bot, IPermissionService permissionService)
	{
		dispatcher.register(Commands.literal("kick")
			.requires(source -> source.getChannel().getServer() != null)
			.executes(context -> kick(context, context.getSource().getClient(), null))
			.then(Commands.argument("username", ClientArgumentType.client(bot))
				.requires(source -> permissionService.hasPermission(source.getClient(), "admin"))
				.executes(context -> kick(context, ClientArgumentType.getClient(context, "username"), null))
				.then(Commands.argument("reason", StringArgumentType.greedyString())
					.executes(context -> kick(context, ClientArgumentType.getClient(context, "username"), StringArgumentType.getString(context, "reason"))))));
	}
	
	private static int kick(CommandContext<CommandSource> context, IClient client, @Nullable String reason) throws CommandSyntaxException
	{
		if(context.getSource().getBot().equals(client))
		{
			throw INVALID_USER.create(client.getName());
		}
		
		IServer server = context.getSource().getChannel().getServer();
		assert server != null;
		context.getSource().getBot().kick(server, reason, client);
		return 0;
	}
}
