package net.kardexo.bot.adapters.youtube;

import com.fasterxml.jackson.databind.JsonNode;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.kardexo.bot.domain.config.Config;
import net.kardexo.bot.services.api.IAPIKeyService;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static net.kardexo.bot.domain.Util.OBJECT_MAPPER;

public class YouTube
{
	private static final Logger logger = LoggerFactory.getLogger(YouTube.class);
	private static final URI API_URL = URI.create("https://www.googleapis.com/youtube/v3/");
	private static final int MAX_RESULTS = 50;
	private static final SimpleCommandExceptionType ERROR_LOADING_VIDEO = new SimpleCommandExceptionType(new LiteralMessage("Error loading video"));
	private static final SimpleCommandExceptionType ERROR_LOADING_PLAYLIST = new SimpleCommandExceptionType(new LiteralMessage("Error loading playlist"));
	private static final SimpleCommandExceptionType ERROR_LOADING_PLAYLIST_ITEMS = new SimpleCommandExceptionType(new LiteralMessage("Error loading playlist items"));
	private static final SimpleCommandExceptionType ERROR_LOADING_PLAYLIST_LENGTH = new SimpleCommandExceptionType(new LiteralMessage("Error loading playlist length"));
	public static final BiPredicate<JsonNode, Integer> MIN_DURATION_PREDICATE = (video, minSeconds) ->
	{
		Duration videoDuration = Duration.parse(video.path("contentDetails").path("duration").asText());
		Duration minVideoDuration = Duration.ofSeconds(minSeconds);
		return videoDuration.compareTo(minVideoDuration) > 0;
	};
	public static final BiPredicate<JsonNode, Integer> MAX_DURATION_PREDICATE = (video, maxSeconds) ->
	{
		Duration videoDuration = Duration.parse(video.path("contentDetails").path("duration").asText());
		Duration maxVideoDuration = Duration.ofSeconds(maxSeconds);
		return videoDuration.compareTo(maxVideoDuration) <= 0;
	};
	
	public static Predicate<JsonNode> createVideoDurationPredicate(int minDuration, int maxDuration)
	{
		return (video) -> MIN_DURATION_PREDICATE.test(video, minDuration) && MAX_DURATION_PREDICATE.test(video, maxDuration);
	}
	
	public static Predicate<JsonNode> createVideoDurationPredicate(Config config)
	{
		return YouTube.createVideoDurationPredicate(config.getMinVideoDurationYouTube(), config.getMaxVideoDurationYouTube());
	}
	
	public static JsonNode watch(IAPIKeyService apiKeyService, String id) throws CommandSyntaxException
	{
		return watch(apiKeyService, id, "snippet");
	}
	
	public static JsonNode watch(IAPIKeyService apiKeyService, String id, String part) throws CommandSyntaxException
	{
		try
		{
			URI uri = new URIBuilder(API_URL.resolve("videos"))
				.addParameter("id", id)
				.addParameter("part", part)
				.addParameter("key", apiKeyService.requestKey(IAPIKeyService.API_KEY_YOUTUBE))
				.build();
			
			JsonNode node = OBJECT_MAPPER.readTree(uri.toURL());
			JsonNode items = node.path("items");
			
			if(items.size() == 1)
			{
				return items.get(0);
			}
		}
		catch(Exception e)
		{
			logger.error("Error fetching youtube video {}", id, e);
		}
		
		throw ERROR_LOADING_VIDEO.create();
	}
	
	public static JsonNode latestVideo(IAPIKeyService apiKeyService, String userId, Predicate<JsonNode> predicate, int skip) throws CommandSyntaxException
	{
		JsonNode videos = playlistItems(apiKeyService, uploadsPlaylistId(apiKeyService, userId), null, "snippet", MAX_RESULTS).path("items");
		int index = 0;
		
		for(JsonNode video : videos)
		{
			if(predicate.test(watch(apiKeyService, videoId(video), "snippet,contentDetails")) && index++ >= skip)
			{
				return video;
			}
		}
		
		return videos.get(0);
	}
	
	public static JsonNode randomVideo(IAPIKeyService apiKeyService, String userId) throws CommandSyntaxException
	{
		String playlist = uploadsPlaylistId(apiKeyService, userId);
		long length = playlistLength(apiKeyService, playlist);
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
				playlistItems = playlistItems(apiKeyService, playlist, pageToken, null, MAX_RESULTS);
			}
			else
			{
				playlistItems = playlistItems(apiKeyService, playlist, pageToken, "snippet", last);
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
	
	public static String uploadsPlaylistId(IAPIKeyService apiKeyService, String userId) throws CommandSyntaxException
	{
		try
		{
			URI uri = new URIBuilder(API_URL.resolve("channels"))
				.addParameter("id", userId)
				.addParameter("part", "contentDetails")
				.addParameter("key", apiKeyService.requestKey(IAPIKeyService.API_KEY_YOUTUBE))
				.build();
			JsonNode node = OBJECT_MAPPER.readTree(uri.toURL());
			JsonNode items = node.path("items");
			
			if(items.size() == 1)
			{
				return items.get(0).path("contentDetails").path("relatedPlaylists").path("uploads").asText();
			}
		}
		catch(Exception e)
		{
			logger.error("Error fetching youtube upload playlist for user {}", userId, e);
		}
		
		throw ERROR_LOADING_PLAYLIST.create();
	}
	
	public static long playlistLength(IAPIKeyService apiKeyService, String playlist) throws CommandSyntaxException
	{
		try
		{
			URI uri = new URIBuilder(API_URL.resolve("playlists"))
				.addParameter("id", playlist)
				.addParameter("part", "contentDetails")
				.addParameter("key", apiKeyService.requestKey(IAPIKeyService.API_KEY_YOUTUBE))
				.build();
			JsonNode node = OBJECT_MAPPER.readTree(uri.toURL());
			JsonNode items = node.path("items");
			
			if(items.size() == 1)
			{
				return items.get(0).path("contentDetails").path("itemCount").asLong();
			}
		}
		catch(Exception e)
		{
			logger.error("Error fetching length for youtube playlist {}", playlist, e);
		}
		
		throw ERROR_LOADING_PLAYLIST_LENGTH.create();
	}
	
	public static JsonNode playlistItems(IAPIKeyService apiKeyService, String playlist, String pageToken, String part, int maxResults) throws CommandSyntaxException
	{
		try
		{
			URIBuilder builder = new URIBuilder(API_URL.resolve("playlistItems"))
				.addParameter("playlistId", playlist)
				.addParameter("maxResults", String.valueOf(maxResults))
				.addParameter("key", apiKeyService.requestKey(IAPIKeyService.API_KEY_YOUTUBE));
			
			if(part != null)
			{
				builder.addParameter("part", part);
			}
			
			if(pageToken != null)
			{
				builder.addParameter("pageToken", pageToken);
			}
			
			return OBJECT_MAPPER.readTree(builder.build().toURL());
		}
		catch(Exception e)
		{
			logger.error("Error fetching items for youtube playlist {}", playlist, e);
		}
		
		throw ERROR_LOADING_PLAYLIST_ITEMS.create();
	}
	
	public static String videoId(JsonNode video)
	{
		return video.path("snippet").path("resourceId").path("videoId").asText();
	}
}
