package net.kardexo.ts3bot.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.URLs;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;

public class CommandSteam
{
	private static final DynamicCommandExceptionType NOT_A_STEAM_URL = new DynamicCommandExceptionType(input -> new LiteralMessage("\"" + input + "\" is not a steam url"));
	private static final String PREFIX = "steam://openurl/";
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("steam")
				.executes(context -> steam(context))
				.then(Commands.argument("url", StringArgumentType.greedyString())
						.executes(context -> steam(context, StringArgumentType.getString(context, "url")))));
	}
	
	private static int steam(CommandContext<CommandSource> context) throws CommandSyntaxException
	{
		String url = Commands.searchHistory(URLs::isSteam, TS3Bot.getInstance().getHistory());
		
		if(url == null)
		{
			return 0;
		}
		
		context.getSource().sendFeedback(PREFIX + URLs.extract(url));
		return 1;
	}
	
	private static int steam(CommandContext<CommandSource> context, String argument) throws CommandSyntaxException
	{
		if(!URLs.isSteam(argument))
		{
			throw NOT_A_STEAM_URL.create(argument);
		}
		
		context.getSource().sendFeedback(PREFIX + URLs.extract(argument));
		return 0;
	}
}
