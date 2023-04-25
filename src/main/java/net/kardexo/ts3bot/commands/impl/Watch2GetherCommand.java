package net.kardexo.ts3bot.commands.impl;

import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.api.Watch2Gether;
import net.kardexo.ts3bot.api.YouTube;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;
import net.kardexo.ts3bot.message.URLMessageProcessor;
import net.kardexo.ts3bot.util.Util;

public class Watch2GetherCommand
{
	private static final String YOUTUBE_URL = "https://youtu.be/";
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		LiteralArgumentBuilder<CommandSource> command = Commands.literal("watch2gether")
			.executes(context -> watch2gether(context))
			.then(Commands.argument("share", StringArgumentType.greedyString())
				.executes(context -> watch2gether(context, Util.extract(StringArgumentType.getString(context, "share")))));
		
		for(Entry<String, String> entry : TS3Bot.getInstance().getConfig().getShortcuts().getYoutube().entrySet())
		{
			command = command.then(Commands.literal(entry.getKey())
				.executes(context -> watch2gether(context, YOUTUBE_URL + YouTube.latestVideo(entry.getValue()).path("snippet").path("resourceId").path("videoId").asText()))
				.then(Commands.literal("random")
					.executes(context -> watch2gether(context, YOUTUBE_URL + YouTube.randomVideo(entry.getValue()).path("snippet").path("resourceId").path("videoId").asText()))));
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
	
	public static int watch2gether(CommandContext<CommandSource> context, String share) throws CommandSyntaxException
	{
		JsonNode node = Watch2Gether.createRoom(share);
		StringBuilder builder = new StringBuilder(); 
		
		if(share != null && !share.isBlank())
		{
			String result = URLMessageProcessor.response(share, false);
			
			if(result != null)
			{
				builder.append(result + " ");
			}
		}
		
		builder.append(Watch2Gether.W2G_URL.resolve(node.path("streamkey").asText()));
		context.getSource().sendFeedback(builder.toString());
		return 0;
	}
}
