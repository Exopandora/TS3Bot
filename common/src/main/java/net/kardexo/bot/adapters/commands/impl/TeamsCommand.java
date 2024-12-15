package net.kardexo.bot.adapters.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import net.kardexo.bot.adapters.commands.CommandSource;
import net.kardexo.bot.adapters.commands.Commands;
import net.kardexo.bot.adapters.commands.arguments.WordListArgumentType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.kardexo.bot.domain.Util.getClientNamesInChannel;

public class TeamsCommand
{
	private static final Dynamic2CommandExceptionType CANNOT_SPLIT = new Dynamic2CommandExceptionType((players, teamCount) -> new LiteralMessage("Cannot split " + players + " players into " + teamCount + " teams"));
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("teams")
			.then(Commands.argument("team_count", IntegerArgumentType.integer())
				.executes(context -> teams(context, IntegerArgumentType.getInteger(context, "team_count")))
				.then(Commands.argument("players", WordListArgumentType.list())
					.executes(context -> teams(context, IntegerArgumentType.getInteger(context, "team_count"), WordListArgumentType.getList(context, "players"))))));
	}
	
	private static int teams(CommandContext<CommandSource> context, int teamCount) throws CommandSyntaxException
	{
		return teams(context, teamCount, getClientNamesInChannel(context.getSource().getClient().getChannel()));
	}
	
	private static int teams(CommandContext<CommandSource> context, int teamCount, String[] words) throws CommandSyntaxException
	{
		return teams(context, teamCount, new ArrayList<String>(Arrays.asList(words)));
	}
	
	private static int teams(CommandContext<CommandSource> context, int teamCount, List<String> players) throws CommandSyntaxException
	{
		if(players.isEmpty() || players.size() % teamCount != 0)
		{
			throw CANNOT_SPLIT.create(players.size(), teamCount);
		}
		
		int teamSize = players.size() / teamCount;
		StringBuilder builder = new StringBuilder();
		
		for(int x = 0; x < teamCount; x++)
		{
			builder.append("\n#");
			builder.append((char) ((x % 65) + 65));
			builder.append(": ");
			
			for(int y = 0; y < teamSize; y++)
			{
				if(y > 0)
				{
					builder.append(", ");
				}
				
				builder.append(players.remove(context.getSource().getRandomSource().nextInt(players.size())));
			}
		}
		
		context.getSource().sendFeedback(builder.toString());
		return teamCount;
	}
}
