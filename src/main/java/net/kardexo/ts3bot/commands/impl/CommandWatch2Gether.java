package net.kardexo.ts3bot.commands.impl;

import java.net.HttpURLConnection;
import java.net.URL;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;
import net.kardexo.ts3bot.util.URLs;

public class CommandWatch2Gether
{
	private static final String API_URL = "https://w2g.tv/rooms/";
	private static final String YOUTUBE_URL = "https://youtu.be/";
	private static final SimpleCommandExceptionType WATCH2GETHER_SERVICE_UNAVAILABLE = new SimpleCommandExceptionType(new LiteralMessage("Watch2Gether is currently unavailable"));
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		LiteralCommandNode<CommandSource> watch2gether = dispatcher.register(Commands.literal("watch2gether")
				.executes(context -> watch2gether(context))
				.then(Commands.argument("url", StringArgumentType.greedyString())
						.executes(context -> watch2gether(context, URLs.extract(StringArgumentType.getString(context, "url")))))
				.then(Commands.literal("held")
						.executes(context -> watch2gether(context, YOUTUBE_URL + CommandHeldDerSteine.fetchRandomVideo().path("snippet").path("resourceId").path("videoId").asText()))));
		dispatcher.register(Commands.literal("w2g")
				.executes(context -> watch2gether(context))
				.redirect(watch2gether));
	}
	
	public static int watch2gether(CommandContext<CommandSource> context) throws CommandSyntaxException
	{
		return watch2gether(context, TS3Bot.getInstance().getConfig().getDefaultW2GShare());
	}
	
	public static int watch2gether(CommandContext<CommandSource> context, String url) throws CommandSyntaxException
	{
		try
		{
			HttpURLConnection connection = (HttpURLConnection) new URL(API_URL + "create.json").openConnection();
			byte[] content = TS3Bot.getInstance().getObjectMapper().writeValueAsBytes(new Watch2Gether(TS3Bot.getInstance().getApiKeyManager().requestKey(TS3Bot.API_KEY_WATCH_2_GETHER), url));
			
			try
			{
				connection.setDoOutput(true);
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Content-Type", "application/json");
				connection.setRequestProperty("Content-Length", String.valueOf(content.length));
				connection.getOutputStream().write(content);
				connection.connect();
				
				JsonNode node = TS3Bot.getInstance().getObjectMapper().readTree(connection.getInputStream());
				StringBuilder builder = new StringBuilder(); 
				
				if(url != null && !url.isBlank())
				{
					String response = TS3Bot.getInstance().generateResponseMessage(URLs.wrap(url), false);
					
					if(response != null)
					{
						builder.append(response + " ");
					}
				}
				
				builder.append(API_URL + node.path("streamkey").asText());
				context.getSource().sendFeedback(builder.toString());
			}
			finally
			{
				connection.disconnect();
			}
		}
		catch(Exception e)
		{
			throw WATCH2GETHER_SERVICE_UNAVAILABLE.create();
		}
		
		return 0;
	}
	
	public static class Watch2Gether
	{
		@JsonProperty("w2g_api_key")
		private String apiKey;
		@JsonProperty("share")
		private String share;
		
		public Watch2Gether()
		{
			super();
		}
		
		public Watch2Gether(String apiKey, String share)
		{
			this.apiKey = apiKey;
			this.share = share;
		}
		
		public String getApiKey()
		{
			return this.apiKey;
		}
		
		public void setApiKey(String apiKey)
		{
			this.apiKey = apiKey;
		}
		
		public String getShare()
		{
			return this.share;
		}
		
		public void setShare(String share)
		{
			this.share = share;
		}
	}
}
