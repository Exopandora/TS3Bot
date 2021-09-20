package net.kardexo.ts3bot.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.util.Util;

public class LeagueOfLegends
{
	private static final String API_URL = "https://%s.api.riotgames.com/";
	private static final URI DDRAGON_API_URL = URI.create("https://ddragon.leagueoflegends.com/");
	private static final URI STATIC_DOC_API_URL = URI.create("http://static.developer.riotgames.com/docs/");
	private static final int HTTP_RETRIES = 10;
	
	public static String fetchVersion()
	{
		URI uri = DDRAGON_API_URL
			.resolve("api/versions.json");
		return fetch(uri).get(0).asText();
	}
	
	public static JsonNode fetchQueues()
	{
		URI uri = STATIC_DOC_API_URL
			.resolve("lol/queues.json");
		return fetch(uri);
	}
	
	public static JsonNode fetchChampions()
	{
		return fetchChampions(fetchVersion());
	}
	
	public static JsonNode fetchChampions(String version)
	{
		URI uri = DDRAGON_API_URL
			.resolve("cdn/" + version + "/data/en_US/champion.json");
		return fetch(uri);
	}
	
	public static JsonNode fetchChampion(String version, String champion)
	{
		URI uri = DDRAGON_API_URL
			.resolve("cdn/" + version + "/data/en_US/champion/" + champion + ".json");
		return fetch(uri);
	}
	
	public static JsonNode fetch(URI uri)
	{
		return fetch(uri, false);
	}
	
	public static JsonNode fetch(URI uri, boolean apikey)
	{
		try(CloseableHttpClient client = Util.httpClient())
		{
			URIBuilder builder = new URIBuilder(uri);
			
			if(apikey)
			{
				builder.addParameter("api_key", TS3Bot.getInstance().getApiKeyManager().requestKey(TS3Bot.API_KEY_LEAGUE_OF_LEGENDS));
			}
			
			HttpGet httpGet = new HttpGet(builder.build());
			httpGet.addHeader("User-Agent", TS3Bot.USER_AGENT);
			httpGet.addHeader("Accept-Charset", StandardCharsets.UTF_8.toString());
			
			for(int x = 0; x < HTTP_RETRIES; x++)
			{
				try(CloseableHttpResponse response = client.execute(httpGet))
				{
					JsonNode node = TS3Bot.getInstance().getObjectMapper().readTree(EntityUtils.toString(response.getEntity()));
					
					if(!node.hasNonNull("status"))
					{
						return node;
					}
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static JsonNode fetchMatchHistory(String summonerId, Region region, int beginIndex, int endIndex)
	{
		try
		{
			URI uri = new URIBuilder(region.getApiUrl().resolve("lol/match/v4/matchlists/by-account/" + summonerId))
				.addParameter("beginIndex", String.valueOf(beginIndex))
				.addParameter("endIndex", String.valueOf(endIndex))
				.build();
			return fetch(uri, true);
		}
		catch(URISyntaxException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static JsonNode fetchMatch(long matchId, Region region)
	{
		URI uri = region.getApiUrl()
			.resolve("lol/match/v4/matches/" + matchId);
		return fetch(uri, true);
	}
	
	public static JsonNode fetchChampionMastery(String summonderId, long championId, Region region)
	{
		URI uri = region.getApiUrl()
			.resolve("lol/champion-mastery/v4/champion-masteries/by-summoner/" + summonderId + "/by-champion/" + championId);
		return fetch(uri, true);
	}
	
	public static JsonNode fetchSummoner(String summonerName, Region region)
	{
		URI uri = region.getApiUrl()
			.resolve("lol/summoner/v4/summoners/by-name/" + encodeSummonerName(summonerName));
		return fetch(uri, true);
	}
	
	public static JsonNode fetchLeague(String summonerId, Region region)
	{
		URI uri = region.getApiUrl()
			.resolve("lol/league/v4/entries/by-summoner/" + summonerId);
		return fetch(uri, true);
	}
	
	public static JsonNode fetchActiveMatch(String summonerId, Region region)
	{
		URI uri = region.getApiUrl()
			.resolve("lol/spectator/v4/active-games/by-summoner/" + summonerId);
		return fetch(uri, true);
	}
	
	public static String encodeSummonerName(String summonerName)
	{
		try
		{
			return URLEncoder.encode(summonerName, "UTF-8");
		}
		catch(UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static class League implements Comparable<League>
	{
		@JsonProperty("leagueId")
		private UUID leagueId;
		@JsonProperty("queueType")
		private QueueType queueType;
		@JsonProperty("tier")
		private Tier tier;
		@JsonProperty("rank")
		private Rank rank;
		@JsonProperty("summonerId")
		private String summonerId;
		@JsonProperty("summonerName")
		private String summonerName;
		@JsonProperty("leaguePoints")
		private int leaguePoints;
		@JsonProperty("wins")
		private int wins;
		@JsonProperty("losses")
		private int losses;
		@JsonProperty("veteran")
		private boolean veteran;
		@JsonProperty("inactive")
		private boolean inactive;
		@JsonProperty("freshBlood")
		private boolean freshBlood;
		@JsonProperty("hotStreak")
		private boolean hotStreak;
		
		public League()
		{
			super();
		}
		
		public League(Tier tier, Rank rank)
		{
			this.tier = tier;
			this.rank = rank;
		}
		
		public UUID getLeagueId()
		{
			return this.leagueId;
		}
		
		public QueueType getQueueType()
		{
			return this.queueType;
		}
		
		public Tier getTier()
		{
			return this.tier;
		}
		
		public Rank getRank()
		{
			return this.rank;
		}
		
		public String getSummonerId()
		{
			return this.summonerId;
		}
		
		public String getSummonerName()
		{
			return this.summonerName;
		}
		
		public int getLeaguePoints()
		{
			return this.leaguePoints;
		}
		
		public int getWins()
		{
			return this.wins;
		}
		
		public int getLosses()
		{
			return this.losses;
		}
		
		public boolean isVeteran()
		{
			return this.veteran;
		}
		
		public boolean isInactive()
		{
			return this.inactive;
		}
		
		public boolean isFreshBlood()
		{
			return this.freshBlood;
		}
		
		public boolean isHotStreak()
		{
			return this.hotStreak;
		}
		
		public int getRating()
		{
			return this.tier.rating(this.rank);
		}
		
		@Override
		public int compareTo(League league)
		{
			if(this.queueType != null && league != null && league.getQueueType() != null)
			{
				return this.queueType.compareTo(league.getQueueType());
			}
			
			return 0;
		}
		
		@Override
		public String toString()
		{
			StringBuilder builder = new StringBuilder(this.tier.getName());
			
			if(this.rank != null && this.tier.hasRanks())
			{
				builder.append(" " + this.rank);
			}
			
			return builder.toString();
		}
	}
	
	public static enum Region
	{
		BR("br1"), 
		EUNE("eun1"),
		EUW("euw1"),
		JP("jp1"),
		KR("kr"),
		LAN("la1"),
		LAS("la2"),
		NA("na1"),
		OCE("oc1"),
		TR("tr1"),
		RU("ru");
		
		private final String id;
		
		private Region(String id)
		{
			this.id = id;
		}
		
		public String getId()
		{
			return this.id;
		}
		
		public URI getApiUrl()
		{
			return URI.create(String.format(API_URL, this.id));
		}
		
		@Override
		public String toString()
		{
			return this.name();
		}
		
		public static Region parse(String input)
		{
			for(Region region : Region.values())
			{
				if(region.name().equals(input))
				{
					return region;
				}
			}
			
			return null;
		}
	}
	
	public static enum QueueType
	{
		RANKED_SOLO_5x5("Ranked Solo/Duo"),
		RANKED_FLEX_SR("Ranked Flex");
		
		private final String name;
		
		private QueueType(String name)
		{
			this.name = name;
		}
		
		public String getName()
		{
			return this.name;
		}
	}
	
	public static enum Tier
	{
		IRON("Iron", true),
		BRONZE("Bronze", true),
		SILVER("Silver", true),
		GOLD("Gold", true),
		PLATINUM("Platinum", true),
		DIAMOND("Diamond", true),
		MASTER("Master", false),
		GRANDMASTER("Grandmaster", false),
		CHALLENGER("Challenger", false);
		
		public static final Tier HIGHEST = Tier.CHALLENGER;
		public static final Tier LOWEST = Tier.IRON;
		public static final Tier[] VALUES = Tier.values();
		
		private final String name;
		private final boolean hasRanks;
		
		private Tier(String name, boolean hasRanks)
		{
			this.name = name;
			this.hasRanks = hasRanks;
		}
		
		public String getName()
		{
			return this.name;
		}
		
		public boolean hasRanks()
		{
			return this.hasRanks;
		}
		
		public int rating(Rank rank)
		{
			return (this.ordinal() > 0 ? VALUES[this.ordinal() - 1].rating(Rank.I) : 0) + (this.hasRanks ? rank.getRating() : 1);
		}
		
		public Tier next()
		{
			return VALUES[(this.ordinal() + 1) % VALUES.length];
		}
	}
	
	public static enum Rank
	{
		IV,
		III,
		II,
		I;
		
		public static final Rank HIGHEST = Rank.I;
		public static final Rank LOWEST = Rank.IV;
		public static final Rank[] VALUES = Rank.values();
		
		public int getRating()
		{
			return this.ordinal() + 1;
		}
		
		public Rank next()
		{
			return VALUES[(this.ordinal() + 1) % VALUES.length];
		}
	}
}
