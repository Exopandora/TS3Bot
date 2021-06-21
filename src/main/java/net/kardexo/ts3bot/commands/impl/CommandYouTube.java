package net.kardexo.ts3bot.commands.impl;

import java.net.URI;
import java.util.Map.Entry;
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

public class CommandYouTube
{
	private static final String YOUTUBE_URL = "https://youtu.be/";
	private static final URI API_URL = URI.create("https://www.googleapis.com/youtube/v3/");
	private static final SimpleCommandExceptionType ERROR_FETCHING_DATA = new SimpleCommandExceptionType(new LiteralMessage("Error fetching data"));
	private static final int MAX_RESULTS = 50;
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		for(Entry<String, String> entry : TS3Bot.getInstance().getConfig().getShortcuts().getYoutube().entrySet())
		{
			dispatcher.register(Commands.literal(entry.getKey())
					.executes(context -> youtube(context, CommandYouTube.fetchLatestVideo(entry.getValue())))
					.then(Commands.literal("random")
							.executes(context -> youtube(context, CommandYouTube.fetchRandomVideo(entry.getValue())))));
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
	
	public static JsonNode fetchLatestVideo(String username) throws CommandSyntaxException
	{
		return CommandYouTube.fetchPlaylistItems(CommandYouTube.fetchUploadsPlaylistId(username), null, null, MAX_RESULTS).path("items").get(0);
	}
	
	public static JsonNode fetchRandomVideo(String username) throws CommandSyntaxException
	{
		String playlist = CommandYouTube.fetchUploadsPlaylistId(username);
		long length = CommandYouTube.fetchPlaylistLength(playlist);
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
				playlistItems = CommandYouTube.fetchPlaylistItems(playlist, pageToken, null, MAX_RESULTS);
			}
			else
			{
				playlistItems = CommandYouTube.fetchPlaylistItems(playlist, pageToken, "snippet", last);
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
		
		throw ERROR_FETCHING_DATA.create();
	}
	
	private static String fetchUploadsPlaylistId(String username) throws CommandSyntaxException
	{
		try
		{
			URI uri = new URIBuilder(API_URL.resolve("channels"))
				.addParameter("forUsername", username)
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
			e.printStackTrace();
		}
		
		throw ERROR_FETCHING_DATA.create();
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
			e.printStackTrace();
		}
		
		throw ERROR_FETCHING_DATA.create();
	}
	
	private static JsonNode fetchPlaylistItems(String playlist, String pageToken, String part, int maxResults) throws CommandSyntaxException
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
			e.printStackTrace();
		}
		
		throw ERROR_FETCHING_DATA.create();
	}
}
