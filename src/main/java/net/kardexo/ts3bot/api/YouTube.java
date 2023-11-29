package net.kardexo.ts3bot.api;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

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
	public static final Predicate<JsonNode> MIN_DURATION_PREDICATE = video ->
	{
		Duration videoDuration = Duration.parse(video.path("contentDetails").path("duration").asText());
		Duration minVideoDuration = Duration.ofSeconds(TS3Bot.getInstance().getConfig().getMinVideoDurationYouTube());
		return videoDuration.compareTo(minVideoDuration) > 0;
	};
	public static final Predicate<JsonNode> MAX_DURATION_PREDICATE = video ->
	{
		Duration videoDuration = Duration.parse(video.path("contentDetails").path("duration").asText());
		Duration maxVideoDuration = Duration.ofSeconds(TS3Bot.getInstance().getConfig().getMaxVideoDurationYouTube());
		return videoDuration.compareTo(maxVideoDuration) <= 0;
	};
	public static final Predicate<JsonNode> VIDEO_DURATION_PREDICATE = MIN_DURATION_PREDICATE.and(MAX_DURATION_PREDICATE);
	
	public static JsonNode watch(String id) throws CommandSyntaxException
	{
		return watch(id, "snippet");
	}
	
	public static JsonNode watch(String id, String part) throws CommandSyntaxException
	{
		try
		{
			URI uri = new URIBuilder(API_URL.resolve("videos"))
				.addParameter("id", id)
				.addParameter("part", part)
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
	
	public static JsonNode latestVideo(String userId, Predicate<JsonNode> predicate, int skip) throws CommandSyntaxException
	{
		JsonNode videos = playlistItems(uploadsPlaylistId(userId), null, "snippet", MAX_RESULTS).path("items");
		int index = 0;
		
		for(JsonNode video : videos)
		{
			if(predicate.test(watch(videoId(video), "snippet,contentDetails")) && index++ < skip)
			{
				return video;
			}
		}
		
		return videos.get(0);
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
	
	public static String videoId(JsonNode video)
	{
		return video.path("snippet").path("resourceId").path("videoId").asText();
	}
}
