package net.kardexo.ts3bot.commands.impl;

import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.api.YouTube;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;

public class YouTubeCommand
{
	private static final String YOUTUBE_URL = "https://youtu.be/";
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		for(Entry<String, String> entry : TS3Bot.getInstance().getConfig().getShortcuts().getYoutube().entrySet())
		{
			dispatcher.register(Commands.literal(entry.getKey())
				.executes(context -> youtube(context, YouTube.latestVideo(entry.getValue(), YouTube.MIN_DURATION_PREDICATE)))
				.then(Commands.literal("random")
					.executes(context -> youtube(context, YouTube.randomVideo(entry.getValue())))));
		}
	}
	
	private static int youtube(CommandContext<CommandSource> context, JsonNode video) throws CommandSyntaxException
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
