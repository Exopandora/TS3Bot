package net.kardexo.ts3bot.commands.impl;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.Util;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;

public class CommandWatch2Gether
{
	private static final String BASE_URL = "https://www.watch2gether.com/rooms/";
	private static final DynamicCommandExceptionType NOT_A_YOUTUBE_URL = new DynamicCommandExceptionType(input -> new LiteralMessage("\"" + input + "\" is not a youtube url"));
	private static final Pattern YOUTUBE_URL = Pattern.compile("\\[URL\\]https?:\\/\\/([^\\.]+\\.)?youtube\\.[^ ]+v=[^ ]+\\[\\/URL\\]");
	private static final SimpleCommandExceptionType WATCH2GETHER_SERVICE_UNAVAILABLE = new SimpleCommandExceptionType(new LiteralMessage("Watch2Gether is currently unavailable"));
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		LiteralCommandNode<CommandSource> watch2gether = dispatcher.register(Commands.literal("watch2gether")
				.executes(context -> watch2gether(context))
				.then(Commands.argument("url", StringArgumentType.greedyString())
						.executes(context -> watch2gether(context, StringArgumentType.getString(context, "url")))));
		dispatcher.register(Commands.literal("w2g")
				.executes(context -> watch2gether(context))
				.redirect(watch2gether));
	}
	
	public static int watch2gether(CommandContext<CommandSource> context) throws CommandSyntaxException
	{
		return watch2gether(context, Commands.searchHistory(YOUTUBE_URL, TS3Bot.getInstance().getHistory()));
	}
	
	public static int watch2gether(CommandContext<CommandSource> context, String url) throws CommandSyntaxException
	{
		if(url != null && !YOUTUBE_URL.matcher(url).matches())
		{
			throw NOT_A_YOUTUBE_URL.create(url);
		}
		
		try
		{
			HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL + "create.json").openConnection();
			connection.setRequestMethod("POST");
			connection.addRequestProperty("api_key", TS3Bot.getInstance().getConfig().getApiWatch2Gether());
			connection.addRequestProperty("share", Util.extractURL(url));
			connection.connect();
			
			JsonNode node = new ObjectMapper().readTree(connection.getInputStream());
			
			context.getSource().sendFeedback(BASE_URL + node.path("streamkey").asText());
			return 0;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw WATCH2GETHER_SERVICE_UNAVAILABLE.create();
		}
	}
}
