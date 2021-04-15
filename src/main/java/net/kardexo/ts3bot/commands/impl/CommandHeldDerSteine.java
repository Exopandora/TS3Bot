package net.kardexo.ts3bot.commands.impl;

import java.net.URI;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.http.client.utils.URIBuilder;

import com.fasterxml.jackson.databind.JsonNode;
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
	private static final URI API_URL = URI.create("https://www.googleapis.com/youtube/v3/");
	private static final SimpleCommandExceptionType ERROR_FETCHING_DATA = new SimpleCommandExceptionType(new LiteralMessage("Error fetching data"));
	private static final int MAX_RESULTS = 50;
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("held")
				.executes(context -> held(context)));
	}
	
	private static int held(CommandContext<CommandSource> context) throws CommandSyntaxException
	{
		JsonNode video = CommandHeldDerSteine.fetchRandomVideo();
		
		if(video != null)
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
		
		return 0;
	}
	
	public static JsonNode fetchRandomVideo() throws CommandSyntaxException
	{
		String playlist = CommandHeldDerSteine.fetchUploadsPlaylistId();
		long length = CommandHeldDerSteine.fetchPlaylistLength(playlist);
		long index = ThreadLocalRandom.current().nextLong(length);
		long fetched = 0;
		
		JsonNode playlistItems = null;
		int last = (int) (index % MAX_RESULTS);
		
		do
		{
			String pageToken = playlistItems != null ? playlistItems.path("nextPageToken").asText() : null;
			fetched += MAX_RESULTS;
			
			if(fetched < index)
			{
				playlistItems = CommandHeldDerSteine.fetchPlaylistItems(playlist, pageToken, null, MAX_RESULTS);
			}
			else
			{
				playlistItems = CommandHeldDerSteine.fetchPlaylistItems(playlist, pageToken, "snippet", last);
			}
		}
		while(fetched < index && playlistItems != null && playlistItems.hasNonNull("nextPageToken"));
		
		if(playlistItems != null)
		{
			JsonNode items = playlistItems.path("items");
			
			if(items.size() == last)
			{
				return items.get(last - 1);
			}
		}
		
		return null;
	}
	
	private static String fetchUploadsPlaylistId() throws CommandSyntaxException
	{
		try
		{
			URI uri = new URIBuilder(API_URL.resolve("channels"))
				.addParameter("forUsername", "HeldderSteine")
				.addParameter("part", "contentDetails")
				.addParameter("key", TS3Bot.getInstance().getApiKeyManager().requestKey(TS3Bot.API_KEY_YOUTUBE))
				.build();
			JsonNode node = TS3Bot.getInstance().getObjectMapper().readTree(uri.toURL());
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
		try
		{
			URI uri = new URIBuilder(API_URL.resolve("playlists"))
				.addParameter("id", playlist)
				.addParameter("part", "contentDetails")
				.addParameter("key", TS3Bot.getInstance().getApiKeyManager().requestKey(TS3Bot.API_KEY_YOUTUBE))
				.build();
			JsonNode node = TS3Bot.getInstance().getObjectMapper().readTree(uri.toURL());
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
		try
		{
			URIBuilder builder = new URIBuilder(API_URL.resolve("playlistItems"))
				.addParameter("playlistId", playlist)
				.addParameter("maxResults", String.valueOf(maxResults))
				.addParameter("key", TS3Bot.getInstance().getApiKeyManager().requestKey(TS3Bot.API_KEY_YOUTUBE));
			
			if(part != null)
			{
				builder.addParameter("part", part);
			}
			
			if(pageToken != null)
			{
				builder.addParameter("pageToken", pageToken);
			}
			
			return TS3Bot.getInstance().getObjectMapper().readTree(builder.build().toURL());
		}
		catch(Exception e)
		{
			ERROR_FETCHING_DATA.create();
		}
		
		return null;
	}
}
