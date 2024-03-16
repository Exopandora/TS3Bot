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
	
	public static String fetchVersion() throws URISyntaxException, IOException
	{
		URI uri = DDRAGON_API_URL
			.resolve("api/versions.json");
		return fetch(uri).get(0).asText();
	}
	
	public static JsonNode fetchQueues() throws URISyntaxException, IOException
	{
		URI uri = STATIC_DOC_API_URL
			.resolve("lol/queues.json");
		return fetch(uri);
	}
	
	public static JsonNode fetchChampions() throws URISyntaxException, IOException
	{
		return fetchChampions(fetchVersion());
	}
	
	public static JsonNode fetchChampions(String version) throws URISyntaxException, IOException
	{
		URI uri = DDRAGON_API_URL
			.resolve("cdn/" + version + "/data/en_US/champion.json");
		return fetch(uri);
	}
	
	public static JsonNode fetchChampion(String version, String champion) throws URISyntaxException, IOException
	{
		URI uri = DDRAGON_API_URL
			.resolve("cdn/" + version + "/data/en_US/champion/" + champion + ".json");
		return fetch(uri);
	}
	
	public static JsonNode fetchItems() throws URISyntaxException, IOException
	{
		return fetchItems(fetchVersion());
	}
	
	public static JsonNode fetchItems(String version) throws URISyntaxException, IOException
	{
		URI uri = DDRAGON_API_URL
			.resolve("cdn/" + version + "/data/en_US/item.json");
		return fetch(uri);
	}
	
	public static JsonNode fetchMatchHistory(String puuid, Region region, int start, int count) throws URISyntaxException, IOException
	{
		URI uri = new URIBuilder(region.getApiUrl().resolve("lol/match/v5/matches/by-puuid/" + puuid + "/ids"))
			.addParameter("start", String.valueOf(start))
			.addParameter("count", String.valueOf(count))
			.build();
		return fetch(uri, true);
	}
	
	public static JsonNode fetchMatch(String matchId, Region region) throws URISyntaxException, IOException
	{
		URI uri = region.getApiUrl()
			.resolve("lol/match/v5/matches/" + matchId);
		return fetch(uri, true);
	}
	
	public static JsonNode fetchChampionMastery(String puuid, long championId, Platform platform) throws URISyntaxException, IOException
	{
		URI uri = platform.getApiUrl()
			.resolve("lol/champion-mastery/v4/champion-masteries/by-puuid/" + puuid + "/by-champion/" + championId);
		return fetch(uri, true);
	}
	
	public static JsonNode fetchAccount(RiotId riotId, Platform platform) throws URISyntaxException, IOException
	{
		URI uri = platform.getRegion().getApiUrl()
			.resolve("riot/account/v1/accounts/by-riot-id/" + encode(riotId.name()) + "/" + encode(riotId.tagLine()));
		return fetch(uri, true);
	}
	
	public static JsonNode fetchLeague(String summonerId, Platform platform) throws URISyntaxException, IOException
	{
		URI uri = platform.getApiUrl()
			.resolve("lol/league/v4/entries/by-summoner/" + summonerId);
		return fetch(uri, true);
	}
	
	public static JsonNode fetchActiveMatch(String puuid, Platform platform) throws URISyntaxException, IOException
	{
		URI uri = platform.getApiUrl()
			.resolve("lol/spectator/v4/active-games/by-summoner/" + encode(puuid));
		return fetch(uri, true);
	}
	
	private static JsonNode fetch(URI uri) throws URISyntaxException, IOException
	{
		return fetch(uri, false);
	}
	
	private static JsonNode fetch(URI uri, boolean apikey) throws URISyntaxException, IOException
	{
		try(CloseableHttpClient client = Util.httpClient())
		{
			HttpGet httpGet = new HttpGet(uri);
			httpGet.addHeader("User-Agent", TS3Bot.USER_AGENT);
			httpGet.addHeader("Accept-Charset", StandardCharsets.UTF_8.toString());
			
			if(apikey)
			{
				httpGet.addHeader("X-Riot-Token", TS3Bot.getInstance().getApiKeyManager().requestKey(TS3Bot.API_KEY_LEAGUE_OF_LEGENDS));
			}
			
			try(CloseableHttpResponse response = client.execute(httpGet))
			{
				return TS3Bot.getInstance().getObjectMapper().readTree(EntityUtils.toString(response.getEntity()));
			}
		}
	}
	
	public static String encode(String summonerName)
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
			
			if(this.rank != null && !this.tier.isApexTier())
			{
				builder.append(" " + this.rank);
			}
			
			return builder.toString();
		}
	}
	
	public static enum Platform
	{
		BR("br1", Region.AMERICAS, "BR"), 
		EUNE("eun1", Region.EUROPE, "EUNE"),
		EUW("euw1", Region.EUROPE, "EUW"),
		JP("jp1", Region.ASIA, "JP"),
		KR("kr", Region.ASIA, "KR"),
		LAN("la1", Region.AMERICAS, "LAN"),
		LAS("la2", Region.AMERICAS, "LAS"),
		NA("na1", Region.AMERICAS, "NA"),
		OCE("oc1", Region.SEA, "OCE"),
		TR("tr1", Region.EUROPE, "TR"),
		RU("ru", Region.EUROPE, "RU"),
		PH("ph2", Region.SEA, "PH"),
		SG("sg2", Region.SEA, "SG"),
		TH("th2", Region.SEA, "TH"),
		TW("tw2", Region.SEA, "TW"),
		VN("vn2", Region.SEA, "VN"),
		PBE("pbe", Region.PBE, "PBE");
		
		private final String id;
		private final String defaultTagLine;
		private final Region regionV5;
		
		private Platform(String id, Region region, String defaultTagLine)
		{
			this.id = id;
			this.regionV5 = region;
			this.defaultTagLine = defaultTagLine;
		}
		
		public String getId()
		{
			return this.id;
		}
		
		public Region getRegion()
		{
			return this.regionV5;
		}
		
		public String getDefaultTagLine()
		{
			return this.defaultTagLine;
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
		
		public static Platform fromId(String input)
		{
			for(Platform platform : Platform.values())
			{
				if(platform.name().equals(input))
				{
					return platform;
				}
			}
			
			return null;
		}
		
		public static Platform fromTagLine(String tagLine)
		{
			for(Platform platform : Platform.values())
			{
				if(platform.getDefaultTagLine().equals(tagLine))
				{
					return platform;
				}
			}
			
			return null;
		}
	}
	
	public static enum Region
	{
		AMERICAS("americas"),
		ASIA("asia"),
		EUROPE("europe"),
		SEA("sea"),
		GARENA("garena"),
		PBE("pbe"),
		ESPORTS("esports");
		
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
			return this.id;
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
		IRON("Iron", false),
		BRONZE("Bronze", false),
		SILVER("Silver", false),
		GOLD("Gold", false),
		PLATINUM("Platinum", false),
		EMERALD("Emerald", false),
		DIAMOND("Diamond", false),
		MASTER("Master", true),
		GRANDMASTER("Grandmaster", true),
		CHALLENGER("Challenger", true);
		
		public static final Tier HIGHEST = Tier.CHALLENGER;
		public static final Tier LOWEST = Tier.IRON;
		public static final Tier[] VALUES = Tier.values();
		
		private final String name;
		private final boolean isApexTier;
		
		private Tier(String name, boolean isApexTier)
		{
			this.name = name;
			this.isApexTier = isApexTier;
		}
		
		public String getName()
		{
			return this.name;
		}
		
		public boolean isApexTier()
		{
			return this.isApexTier;
		}
		
		public int rating(Rank rank)
		{
			return (this.ordinal() > 0 ? VALUES[this.ordinal() - 1].rating(Rank.I) : 0) + (this.isApexTier ? 1 : rank.getRating());
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
	
	public static record RiotId(String name, String tagLine)
	{
		@Override
		public String toString()
		{
			return this.name + "#" + this.tagLine;
		}
		
		public static RiotId parse(String riotId, Platform defaultPlatform)
		{
			int index = riotId.lastIndexOf('#');
			
			if(index > 0 && riotId.length() - index - 1 <= 5 && index + 2 < riotId.length())
			{
				String tagLine = riotId.substring(index + 1);
				
				if(tagLine.matches("[0-9a-zA-Z]{3,5}") || Platform.fromTagLine(tagLine) != null)
				{
					return new RiotId(riotId.substring(0, index), tagLine);
				}
			}
			
			return new RiotId(riotId, defaultPlatform.getDefaultTagLine());
		}
	}
}
