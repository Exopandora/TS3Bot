package net.kardexo.bot.services.commands.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.kardexo.bot.domain.CommandSource;
import net.kardexo.bot.services.commands.Commands;
import net.kardexo.bot.adapters.w2g.Watch2Gether;
import net.kardexo.bot.adapters.youtube.YouTube;
import net.kardexo.bot.domain.Util;
import net.kardexo.bot.domain.config.Config;
import net.kardexo.bot.services.api.IAPIKeyService;
import net.kardexo.bot.services.api.IURLMessageProcessor;

import java.util.Map.Entry;

public class Watch2GetherCommand
{
	private static final String YOUTUBE_URL = "https://youtu.be/";
	
	public static void register(CommandDispatcher<CommandSource> dispatcher, Config config, IAPIKeyService apiKeyService, IURLMessageProcessor urlMessageProcessor)
	{
		LiteralArgumentBuilder<CommandSource> command = Commands.literal("watch2gether")
			.executes(context -> watch2gether(context, apiKeyService, urlMessageProcessor, config.getDefaultW2GShare(), config.getDefaultW2GBGColor(), config.getDefaultW2GBGOpacity()))
			.then(Commands.argument("share", StringArgumentType.greedyString())
				.executes(context -> watch2gether(context, apiKeyService, urlMessageProcessor, Util.extractURL(StringArgumentType.getString(context, "share")), config.getDefaultW2GBGColor(), config.getDefaultW2GBGOpacity())));
		
		for(Entry<String, String> entry : config.getShortcuts().getYoutube().entrySet())
		{
			command = command.then(Commands.literal(entry.getKey())
				.executes(context -> watch2gether(context, apiKeyService, urlMessageProcessor, YOUTUBE_URL + YouTube.videoId(YouTube.latestVideo(apiKeyService, entry.getValue(), YouTube.createVideoDurationPredicate(config), 0)), config.getDefaultW2GBGColor(), config.getDefaultW2GBGOpacity()))
				.then(Commands.literal("random")
					.executes(context -> watch2gether(context, apiKeyService, urlMessageProcessor, YOUTUBE_URL + YouTube.videoId(YouTube.randomVideo(apiKeyService, entry.getValue())), config.getDefaultW2GBGColor(), config.getDefaultW2GBGOpacity())))
				.then(Commands.argument("skip", IntegerArgumentType.integer(1, 25))
					.executes(context -> watch2gether(context, apiKeyService, urlMessageProcessor, YOUTUBE_URL + YouTube.videoId(YouTube.latestVideo(apiKeyService, entry.getValue(), YouTube.createVideoDurationPredicate(config), IntegerArgumentType.getInteger(context, "skip"))), config.getDefaultW2GBGColor(), config.getDefaultW2GBGOpacity()))));
		}
		
		LiteralCommandNode<CommandSource> watch2gether = dispatcher.register(command);
		
		dispatcher.register(Commands.literal("w2g")
			.executes(context -> watch2gether(context, apiKeyService, urlMessageProcessor, config.getDefaultW2GShare(), config.getDefaultW2GBGColor(), config.getDefaultW2GBGOpacity()))
			.redirect(watch2gether));
	}
	
	public static int watch2gether(CommandContext<CommandSource> context, IAPIKeyService apiKeyService, IURLMessageProcessor urlMessageProcessor, String share, String bgColor, int bgOpacity) throws CommandSyntaxException
	{
		JsonNode node = Watch2Gether.createRoom(apiKeyService, share, bgColor, bgOpacity);
		StringBuilder builder = new StringBuilder(); 
		
		if(share != null && !share.isBlank())
		{
			String result = urlMessageProcessor.processMessage(share);
			
			if(result != null)
			{
				builder.append(result);
				builder.append(" ");
			}
		}
		
		builder.append(Watch2Gether.W2G_URL.resolve(node.path("streamkey").asText()));
		context.getSource().sendFeedback(builder.toString());
		return 0;
	}
}
