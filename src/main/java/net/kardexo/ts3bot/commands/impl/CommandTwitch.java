package net.kardexo.ts3bot.commands.impl;

import java.net.HttpURLConnection;
import java.net.URL;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;

public class CommandTwitch
{
	private static final String BASE_URL = "https://api.twitch.tv/helix/streams?user_login=";
	private static final SimpleCommandExceptionType TWITCH_SERVICE_UNAVAILABLE = new SimpleCommandExceptionType(new LiteralMessage("Twitch service is currently unavailable"));
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("twitch")
				.then(Commands.argument("username", StringArgumentType.word())
						.executes(context -> twitch(context, StringArgumentType.getString(context, "username")))));
	}
	
	public static int twitch(CommandContext<CommandSource> context, String user) throws CommandSyntaxException
	{
		try
		{
			HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL + user).openConnection();
			connection.setRequestProperty("Client-ID", TS3Bot.getInstance().getConfig().getApiTwitchClientId());
			connection.setRequestProperty("Authorization", "Bearer " + TS3Bot.getInstance().getConfig().getApiTwitchOAuthToken());
			connection.connect();
			
			JsonNode node = new ObjectMapper().readTree(connection.getInputStream());
			JsonNode data = node.path("data");
			
			if(data != null && data.size() > 0)
			{
				JsonNode content = data.get(0);
				context.getSource().sendFeedback(content.path("user_name").asText() + " is live for " + content.path("viewer_count").asInt() + " viewers " + content.path("title") + " https://www.twitch.tv/" + user + "/");
			}
			else
			{
				context.getSource().sendFeedback(user + " is currently offline");
			}
			
			return 0;
		}
		catch(Exception e)
		{
			throw TWITCH_SERVICE_UNAVAILABLE.create();
		}
	}
}
