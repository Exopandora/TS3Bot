package net.kardexo.ts3bot.commands.impl;

import java.net.URL;
import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;

public class CommandHeldDerSteine
{
	private static final String YOUTUBE_URL = "https://youtu.be/";
	private static final String API_URL = "https://www.googleapis.com/youtube/v3/";
	private static final SimpleCommandExceptionType ERROR_FETCHING_DATA = new SimpleCommandExceptionType(new LiteralMessage("Error fetching data"));
	private static final int MAX_RESULTS = 50;
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("held")
				.executes(context -> held(context)));
	}
	
	private static int held(CommandContext<CommandSource> context) throws CommandSyntaxException
	{
		String playlist = CommandHeldDerSteine.fetchUploadsPlaylistId();
		long length = CommandHeldDerSteine.fetchPlaylistLength(playlist);
		long index = ThreadLocalRandom.current().nextLong(length);
		long retrieved = 0;
		int last = (int) (index % MAX_RESULTS);
		JsonNode playlistItems = null;
		
		do
		{
			String pageToken = playlistItems != null ? playlistItems.path("nextPageToken").asText() : null;
			retrieved += MAX_RESULTS;
			
			if(retrieved < index)
			{
				playlistItems = CommandHeldDerSteine.fetchPlaylistItems(playlist, pageToken, null, MAX_RESULTS);
			}
			else
			{
				playlistItems = CommandHeldDerSteine.fetchPlaylistItems(playlist, pageToken, "snippet", last);
			}
		}
		while(retrieved < index && playlistItems != null && playlistItems.hasNonNull("nextPageToken"));
		
		if(playlistItems != null)
		{
			JsonNode items = playlistItems.path("items");
			
			if(items.size() == last)
			{
				JsonNode snippet = items.get(last - 1).path("snippet");
				String channelTitle = snippet.path("channelTitle").asText();
				String title = snippet.path("title").asText();
				String videoId = snippet.path("resourceId").path("videoId").asText();
				
				if(channelTitle != null && !channelTitle.isEmpty() && title != null && !title.isEmpty() && videoId != null && !videoId.isEmpty())
				{
					context.getSource().sendFeedback(channelTitle + ": \"" + title + "\" " + YOUTUBE_URL + videoId);
				}
			}
		}
		
		return (int) index;
	}
	
	private static String fetchUploadsPlaylistId() throws CommandSyntaxException
	{
		StringBuilder query = new StringBuilder(API_URL + "channels");
		
		query.append("?forUsername=HeldderSteine");
		query.append("&part=contentDetails");
		query.append("&key=" + TS3Bot.getInstance().getConfig().getApiYouTube());
		
		try
		{
			JsonNode node = new ObjectMapper().readTree(new URL(query.toString()));
			JsonNode items = node.path("items");
			
			if(items.size() == 1)
			{
				return items.get(0).path("contentDetails").path("relatedPlaylists").path("uploads").asText();
			}
		}
		catch(Exception e)
		{
			ERROR_FETCHING_DATA.create();
		}
		
		return null;
	}
	
	private static long fetchPlaylistLength(String playlist) throws CommandSyntaxException
	{
		StringBuilder query = new StringBuilder(API_URL + "playlists");
		
		query.append("?id=" + playlist);
		query.append("&part=contentDetails");
		query.append("&key=" + TS3Bot.getInstance().getConfig().getApiYouTube());
		
		try
		{
			JsonNode node = new ObjectMapper().readTree(new URL(query.toString()));
			JsonNode items = node.path("items");
			
			if(items.size() == 1)
			{
				return items.get(0).path("contentDetails").path("itemCount").asLong();
			}
		}
		catch(Exception e)
		{
			ERROR_FETCHING_DATA.create();
		}
		
		return 0;
	}
	
	private static JsonNode fetchPlaylistItems(String playlist, String pageToken, String part, int maxResults)
	{
		StringBuilder query = new StringBuilder(API_URL + "playlistItems");
		
		query.append("?playlistId=" + playlist);
		query.append("&key=" + TS3Bot.getInstance().getConfig().getApiYouTube());
		query.append("&maxResults=" + maxResults);
		
		if(part != null)
		{
			query.append("&part=" + part);
		}
		
		if(pageToken != null)
		{
			query.append("&pageToken=" + pageToken);
		}
		
		try
		{
			return new ObjectMapper().readTree(new URL(query.toString()));
		}
		catch(Exception e)
		{
			ERROR_FETCHING_DATA.create();
		}
		
		return null;
	}
}
