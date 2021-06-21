package net.kardexo.ts3bot.commands.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;
import net.kardexo.ts3bot.msgproc.URLMessageProcessor;
import net.kardexo.ts3bot.util.Util;

public class CommandWatch2Gether
{
	private static final URI API_URL = URI.create("https://w2g.tv/rooms/");
	private static final String YOUTUBE_URL = "https://youtu.be/";
	private static final SimpleCommandExceptionType WATCH2GETHER_SERVICE_UNAVAILABLE = new SimpleCommandExceptionType(new LiteralMessage("Watch2Gether is currently unavailable"));
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		LiteralArgumentBuilder<CommandSource> command = Commands.literal("watch2gether")
				.executes(context -> watch2gether(context))
				.then(Commands.argument("url", StringArgumentType.greedyString())
						.executes(context -> watch2gether(context, Util.extract(StringArgumentType.getString(context, "url")))));
		
		for(Entry<String, String> entry : TS3Bot.getInstance().getConfig().getShortcuts().getYoutube().entrySet())
		{
			command = command.then(Commands.literal(entry.getKey())
					.executes(context -> watch2gether(context, YOUTUBE_URL + CommandYouTube.fetchRandomVideo(entry.getValue()).path("snippet").path("resourceId").path("videoId").asText())));
		}
		
		LiteralCommandNode<CommandSource> watch2gether = dispatcher.register(command);
		
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
		try(CloseableHttpClient client = Util.httpClient())
		{
			Map<String, Object> watch2gether = new HashMap<String, Object>();
			watch2gether.put("w2g_api_key", TS3Bot.getInstance().getApiKeyManager().requestKey(TS3Bot.API_KEY_WATCH_2_GETHER));
			watch2gether.put("share", url);
			watch2gether.put("bg_color", TS3Bot.getInstance().getConfig().getDefaultW2GBGColor());
			watch2gether.put("bg_opacity", TS3Bot.getInstance().getConfig().getDefaultW2GBGOpacity());
			
			HttpPost httpPost = new HttpPost(API_URL.resolve("create.json"));
			httpPost.addHeader("Content-Type", "application/json");
			httpPost.setEntity(new StringEntity(TS3Bot.getInstance().getObjectMapper().writeValueAsString(watch2gether)));
			
			try(CloseableHttpResponse response = client.execute(httpPost))
			{
				JsonNode node = TS3Bot.getInstance().getObjectMapper().readTree(response.getEntity().getContent());
				StringBuilder builder = new StringBuilder(); 
				
				if(url != null && !url.isBlank())
				{
					String result = URLMessageProcessor.response(Util.wrap(url), false);
					
					if(result != null)
					{
						builder.append(result + " ");
					}
				}
				
				context.getSource().sendFeedback(builder.append(API_URL.resolve(node.path("streamkey").asText())).toString());
			}
		}
		catch(Exception e)
		{
			throw WATCH2GETHER_SERVICE_UNAVAILABLE.create();
		}
		
		return 0;
	}
}
