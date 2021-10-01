package net.kardexo.ts3bot.api;

import java.net.URI;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.http.client.utils.URIBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.kardexo.ts3bot.TS3Bot;

public class YouTube
{
	private static final URI API_URL = URI.create("https://www.googleapis.com/youtube/v3/");
	private static final int MAX_RESULTS = 50;
	private static final SimpleCommandExceptionType ERROR_LOADING_VIDEO = new SimpleCommandExceptionType(new LiteralMessage("Error loading video"));
	private static final SimpleCommandExceptionType ERROR_LOADING_PLAYLIST = new SimpleCommandExceptionType(new LiteralMessage("Error loading playlist"));
	private static final SimpleCommandExceptionType ERROR_LOADING_PLAYLIST_ITEMS = new SimpleCommandExceptionType(new LiteralMessage("Error loading playlist items"));
	private static final SimpleCommandExceptionType ERROR_LOADING_PLAYLIST_LENGTH = new SimpleCommandExceptionType(new LiteralMessage("Error loading playlist length"));
	
	public static JsonNode watch(String id, long timestamp) throws CommandSyntaxException
	{
		try
		{
			URI uri = new URIBuilder(API_URL.resolve("videos"))
				.addParameter("id", id)
				.addParameter("part", "snippet")
				.addParameter("key", TS3Bot.getInstance().getApiKeyManager().requestKey(TS3Bot.API_KEY_YOUTUBE))
				.build();
			
			JsonNode node = TS3Bot.getInstance().getObjectMapper().readTree(uri.toURL());
			JsonNode items = node.path("items");
			
			if(items.size() == 1)
			{
				return items.get(0);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		throw ERROR_LOADING_VIDEO.create();
	}
	
	public static JsonNode latestVideo(String userId) throws CommandSyntaxException
	{
		return playlistItems(uploadsPlaylistId(userId), null, "snippet", MAX_RESULTS).path("items").get(0);
	}
	
	public static JsonNode randomVideo(String userId) throws CommandSyntaxException
	{
		String playlist = uploadsPlaylistId(userId);
		long length = playlistLength(playlist);
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
				playlistItems = playlistItems(playlist, pageToken, null, MAX_RESULTS);
			}
			else
			{
				playlistItems = playlistItems(playlist, pageToken, "snippet", last);
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
		
		throw ERROR_LOADING_PLAYLIST_ITEMS.create();
	}
	
	public static String uploadsPlaylistId(String userId) throws CommandSyntaxException
	{
		try
		{
			URI uri = new URIBuilder(API_URL.resolve("channels"))
				.addParameter("id", userId)
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
		
		throw ERROR_LOADING_PLAYLIST.create();
	}
	
	public static long playlistLength(String playlist) throws CommandSyntaxException
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
		
		throw ERROR_LOADING_PLAYLIST_LENGTH.create();
	}
	
	public static JsonNode playlistItems(String playlist, String pageToken, String part, int maxResults) throws CommandSyntaxException
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
		
		throw ERROR_LOADING_PLAYLIST_ITEMS.create();
	}
}
