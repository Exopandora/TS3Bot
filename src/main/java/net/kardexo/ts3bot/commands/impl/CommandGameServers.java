package net.kardexo.ts3bot.commands.impl;

import java.io.IOException;
import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;
import net.kardexo.ts3bot.gameservers.GameServerManager;

public class CommandGameServers
{
	private static final DynamicCommandExceptionType SERVER_NOT_FOUND = new DynamicCommandExceptionType(server -> new LiteralMessage("Could not find server " + server));
	private static final DynamicCommandExceptionType SERVER_NOT_RUNNING = new DynamicCommandExceptionType(server -> new LiteralMessage("Server " + server + " is not running"));
	private static final DynamicCommandExceptionType SERVER_ARLREADY_RUNNING = new DynamicCommandExceptionType(server -> new LiteralMessage("Server " + server + " is already running"));
	private static final DynamicCommandExceptionType COULD_NOT_START_SERVER = new DynamicCommandExceptionType(server -> new LiteralMessage("Could not start server " + server));
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("gameservers")
				.requires(source -> source.hasPermission("admin"))
				.then(Commands.literal("start")
						.then(Commands.argument("server", StringArgumentType.word())
								.executes(context -> start(context, dispatcher, StringArgumentType.getString(context, "server")))))
				.then(Commands.literal("stop")
						.then(Commands.argument("server", StringArgumentType.word())
								.executes(context -> stop(context, dispatcher, StringArgumentType.getString(context, "server")))))
				.then(Commands.literal("list")
						.executes(context -> list(context, dispatcher))));
	}
	
	private static int start(CommandContext<CommandSource> context, CommandDispatcher<CommandSource> dispatcher, String server) throws CommandSyntaxException
	{
		GameServerManager manager = TS3Bot.getInstance().getGameserverManager();
		
		if(!manager.getAvailableServerIds().contains(server))
		{
			throw SERVER_NOT_FOUND.create(server);
		}
		
		if(manager.isServerRunning(server))
		{
			throw SERVER_ARLREADY_RUNNING.create(server);
		}
		
		try
		{
			manager.startServer(server);
		}
		catch(IOException | InterruptedException e)
		{
			throw COULD_NOT_START_SERVER.create(server);
		}
		
		context.getSource().sendFeedback("Starting gameserver " + server);
		return 0;
	}
	
	private static int stop(CommandContext<CommandSource> context, CommandDispatcher<CommandSource> dispatcher, String server) throws CommandSyntaxException
	{
		GameServerManager manager = TS3Bot.getInstance().getGameserverManager();
		
		if(!manager.getRunningServerIds().contains(server))
		{
			throw SERVER_NOT_FOUND.create(server);
		}
		
		if(!manager.isServerRunning(server))
		{
			throw SERVER_NOT_RUNNING.create(server);
		}
		
		manager.stopServer(server);
		context.getSource().sendFeedback("Stopping gameserver " + server);
		return 0;
	}
	
	private static int list(CommandContext<CommandSource> context, CommandDispatcher<CommandSource> dispatcher) throws CommandSyntaxException
	{
		GameServerManager manager = TS3Bot.getInstance().getGameserverManager();
		StringBuilder builder = new StringBuilder();
		
		builder.append("\nRunning gameservers:");
		CommandGameServers.appendServers(builder, manager.getRunningServerIds());
		
		builder.append("\nAvailable gameservers:");
		CommandGameServers.appendServers(builder, manager.getAvailableServerIds());
		
		context.getSource().sendFeedback(builder.toString());
		return 0;
	}
	
	private static void appendServers(StringBuilder builder, List<String> servers)
	{
		if(servers.isEmpty())
		{
			builder.append("\n\t(None)");
		}
		else
		{
			for(String server : servers)
			{
				builder.append("\n\t" + server);
			}
		}
	}
}
