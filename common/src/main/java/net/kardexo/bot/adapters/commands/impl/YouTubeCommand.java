package net.kardexo.bot.adapters.commands.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.kardexo.bot.adapters.commands.CommandSource;
import net.kardexo.bot.adapters.commands.Commands;
import net.kardexo.bot.adapters.youtube.YouTube;
import net.kardexo.bot.domain.config.Config;
import net.kardexo.bot.services.api.IAPIKeyService;

import java.util.Map.Entry;

public class YouTubeCommand
{
	private static final String YOUTUBE_URL = "https://youtu.be/";
	
	public static void register(CommandDispatcher<CommandSource> dispatcher, Config config, IAPIKeyService apiKeyService)
	{
		for(Entry<String, String> entry : config.getShortcuts().getYoutube().entrySet())
		{
			dispatcher.register(Commands.literal(entry.getKey())
				.executes(context -> youtube(context, YouTube.latestVideo(apiKeyService, entry.getValue(), YouTube.createVideoDurationPredicate(config), 0)))
				.then(Commands.literal("random")
					.executes(context -> youtube(context, YouTube.randomVideo(apiKeyService, entry.getValue()))))
				.then(Commands.argument("skip", IntegerArgumentType.integer(1, 25))
					.executes(context -> youtube(context, YouTube.latestVideo(apiKeyService, entry.getValue(), YouTube.createVideoDurationPredicate(config), IntegerArgumentType.getInteger(context, "skip"))))));
		}
	}
	
	private static int youtube(CommandContext<CommandSource> context, JsonNode video)
	{
		JsonNode snippet = video.path("snippet");
		String channelTitle = snippet.path("channelTitle").asText();
		String title = snippet.path("title").asText();
		String videoId = snippet.path("resourceId").path("videoId").asText();
		
		if(channelTitle != null && !channelTitle.isEmpty() && title != null && !title.isEmpty() && videoId != null && !videoId.isEmpty())
		{
			context.getSource().sendFeedback(channelTitle + ": \"" + title + "\" " + YOUTUBE_URL + videoId);
		}
		
		return snippet.path("position").asInt() + 1;
	}
}
