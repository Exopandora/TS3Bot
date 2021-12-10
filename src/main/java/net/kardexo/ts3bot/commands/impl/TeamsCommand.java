package net.kardexo.ts3bot.commands.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;

public class TeamsCommand
{
	private static final SimpleCommandExceptionType PLAYERS_EMPTY = new SimpleCommandExceptionType(new LiteralMessage("No players specified"));
	private static final Dynamic2CommandExceptionType CANNOT_SPLIT = new Dynamic2CommandExceptionType((players, teamCount) -> new LiteralMessage("Cannot split " + players + " players into " + teamCount + " teams"));
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("teams")
				.then(Commands.argument("team_count", IntegerArgumentType.integer())
						.executes(context -> teams(context, IntegerArgumentType.getInteger(context, "team_count")))
						.then(Commands.argument("players", StringArgumentType.greedyString())
								.executes(context -> teams(context, IntegerArgumentType.getInteger(context, "team_count"), StringArgumentType.getString(context, "players"))))));
	}
	
	private static int teams(CommandContext<CommandSource> context, int teamCount) throws CommandSyntaxException
	{
		return teams(context, teamCount, TS3Bot.getInstance().getApi().getClients().stream()
				.filter(client -> client.getChannelId() == context.getSource().getClientInfo().getChannelId() && client.getId() != TS3Bot.getInstance().getId())
				.map(client -> client.getNickname())
				.collect(Collectors.toList()));
	}
	
	private static int teams(CommandContext<CommandSource> context, int teamCount, String players) throws CommandSyntaxException
	{
		if(players.matches(" +"))
		{
			throw PLAYERS_EMPTY.create();
		}
		
		return teams(context, teamCount, new ArrayList<String>(Arrays.asList(players.split(" +"))));
	}
	
	private static int teams(CommandContext<CommandSource> context, int teamCount, List<String> players) throws CommandSyntaxException
	{
		if(players.size() % teamCount != 0)
		{
			throw CANNOT_SPLIT.create(players.size(), teamCount);
		}
		
		int teamSize = players.size() / teamCount;
		StringBuilder builder = new StringBuilder();
		
		for(int x = 0; x < teamCount; x++)
		{
			builder.append("\n#" + (char) ((x % 65) + 65) + ": ");
			
			for(int y = 0; y < teamSize; y++)
			{
				if(y > 0)
				{
					builder.append(", ");
				}
				
				builder.append(players.remove(TS3Bot.RANDOM.nextInt(players.size())));
			}
		}
		
		context.getSource().sendFeedback(builder.toString());
		return teamCount;
	}
}
