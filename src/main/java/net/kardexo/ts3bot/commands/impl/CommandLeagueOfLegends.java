package net.kardexo.ts3bot.commands.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;
import net.kardexo.ts3bot.util.StringUtils;

public class CommandLeagueOfLegends
{
	private static final DynamicCommandExceptionType SUMMONER_NOT_FOUND = new DynamicCommandExceptionType(summoner -> new LiteralMessage("Could not find summoner " + summoner));
	private static final DynamicCommandExceptionType SUMMONER_NOT_IN_GAME = new DynamicCommandExceptionType(summoner -> new LiteralMessage(summoner + " is currently not in an active game"));
	private static final SimpleCommandExceptionType ERROR_FETCHING_DATA = new SimpleCommandExceptionType(new LiteralMessage("Error fetching data"));
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM");
	
	private static final String API_URL = "https://%s.api.riotgames.com/";
	private static final String DDRAGON_API_URL = "https://ddragon.leagueoflegends.com/";
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("lol")
				.then(Commands.literal("match")
						.then(Commands.argument("summoner", StringArgumentType.greedyString())
								.executes(context -> match(context, dispatcher, StringArgumentType.getString(context, "summoner"), TS3Bot.getInstance().getConfig().getLoLRegion()))))
				.then(Commands.literal("history")
						.then(Commands.argument("summoner", StringArgumentType.greedyString())
								.executes(context -> history(context, dispatcher, StringArgumentType.getString(context, "summoner"), TS3Bot.getInstance().getConfig().getLoLRegion())))));
	}
	
	private static int match(CommandContext<CommandSource> context, CommandDispatcher<CommandSource> dispatcher, String username, Region region) throws CommandSyntaxException
	{
		JsonNode champions = CommandLeagueOfLegends.fetchChampions(CommandLeagueOfLegends.fetchVersion());
		JsonNode summoner = CommandLeagueOfLegends.fetchSummoner(username, region);
		JsonNode match = CommandLeagueOfLegends.fetchActiveMatch(username, summoner.path("id").asText(), region);
		
		StringBuilder builder = new StringBuilder();
		JsonNode participants = match.path("participants");
		
		for(int x = 0; x < participants.size(); x++)
		{
			if(x == participants.size() / 2)
			{
				builder.append("\n\t\t\t\t\t\t\t\t\tVS");
			}
			
			JsonNode participant = participants.get(x);
			builder.append("\n[" + CommandLeagueOfLegends.getChampionById(participant.path("championId").asLong(), champions) + "]");
			builder.append(" " + participant.path("summonerName").asText());
			
			if(!participant.path("bot").asBoolean())
			{
				String summonerId = CommandLeagueOfLegends.encodeSummonerName(participant.path("summonerId").asText());
				
				JsonNode league = CommandLeagueOfLegends.fetchLeague(summonerId, region);
				builder.append(" - " + CommandLeagueOfLegends.getHighestRank(league));
				
				try
				{
					JsonNode mastery = CommandLeagueOfLegends.fetchMasteryPoints(summonerId, participant.path("championId").asLong(), region);
					builder.append(" - Level " + mastery.path("championLevel").asInt() + ", " + mastery.path("championPoints").asInt() + " Points");
				}
				catch(CommandSyntaxException e)
				{
					builder.append(" - Level 1, 0 Points");
				}
			}
		}
		
		context.getSource().sendFeedback(builder.toString());
		
		return participants.size();
	}
	
	private static int history(CommandContext<CommandSource> context, CommandDispatcher<CommandSource> dispatcher, String username, Region region) throws CommandSyntaxException
	{
		JsonNode champions = CommandLeagueOfLegends.fetchChampions(CommandLeagueOfLegends.fetchVersion());
		JsonNode summoner = CommandLeagueOfLegends.fetchSummoner(username, region);
		JsonNode history = CommandLeagueOfLegends.fetchMatchHistory(summoner.path("accountId").asText(), region, 0, 10).path("matches");
		
		StringBuilder builder = new StringBuilder();
		
		int wins = 0;
		int totalKills = 0;
		int totalDeaths = 0;
		int totalAssists = 0;
		long totalDuration = 0;
		
		for(int x = 0; x < history.size(); x++)
		{
			JsonNode match = CommandLeagueOfLegends.fetchMatch(history.get(x).path("gameId").asLong(), region);
			int participantId = CommandLeagueOfLegends.getParticinantId(match, summoner.path("id").asText());
			JsonNode participant = CommandLeagueOfLegends.getParticipant(match, participantId);
			JsonNode stats = participant.path("stats");
			String champion = CommandLeagueOfLegends.getChampionById(participant.path("championId").asLong(), champions);
			long gameDuration = match.path("gameDuration").asLong();
			boolean winner = stats.path("win").asBoolean();
			int kills = stats.path("kills").asInt();
			int deaths = stats.path("deaths").asInt();
			int assists = stats.path("assists").asInt();
			
			builder.append("\n[[color=" + (winner ? "green" : "#FF2345") + "]" + (x + 1) + "[/color]]");
			builder.append(" " + match.path("gameMode").asText());
			builder.append(" - " + DATE_FORMAT.format(new Date(match.path("gameCreation").asLong())));
			builder.append(" - " + StringUtils.formatDuration(gameDuration));
			builder.append(" - " + kills + "/" + deaths + "/" + assists);
			builder.append(" - " + champion);
			
			totalKills += kills;
			totalDeaths += deaths;
			totalAssists += assists;
			totalDuration += gameDuration;
			
			if(winner)
			{
				wins++;
			}
		}
		
		double matches = history.size();
		
		builder.append("\nWinrate: " + Math.round(wins * 100 / matches) + "%");
		builder.append(" - Average KAD: " + totalKills / matches + "/" + totalDeaths / matches + "/" + totalAssists / matches);
		builder.append(" - Average Match Length: " + StringUtils.formatDuration((long) (totalDuration / matches)));
		
		context.getSource().sendFeedback(builder.toString());
		
		return wins;
	}
	
	private static String fetchVersion() throws CommandSyntaxException
	{
		try
		{
			return new ObjectMapper().readTree(new URL(DDRAGON_API_URL + "api/versions.json")).get(0).asText();
		}
		catch(IOException e)
		{
			throw ERROR_FETCHING_DATA.create();
		}
	}
	
	private static JsonNode fetchChampions(String version) throws CommandSyntaxException
	{
		try
		{
			return new ObjectMapper().readTree(new URL(DDRAGON_API_URL + "cdn/" + version + "/data/en_US/champion.json"));
		}
		catch(IOException e)
		{
			throw ERROR_FETCHING_DATA.create();
		}
	}
	
	private static String getChampionById(long championId, JsonNode champions)
	{
		JsonNode data = champions.path("data");
		Iterator<JsonNode> iterator = data.elements();
		
		while(iterator.hasNext())
		{
			JsonNode champion = iterator.next();
			
			if(champion.path("key").asInt() == championId)
			{
				return champion.path("name").asText();
			}
		}
		
		return null;
	}
	
	private static JsonNode fetchMasteryPoints(String summonderId, long championId, Region region) throws CommandSyntaxException
	{
		String query = region.getApiUrl() + "lol/champion-mastery/v4/champion-masteries/by-summoner/" + summonderId + "/by-champion/"+ championId + "?api_key=" + TS3Bot.getInstance().getConfig().getApiLeagueOfLegends();
		
		try
		{
			return new ObjectMapper().readTree(new URL(query));
		}
		catch(IOException e)
		{
			throw ERROR_FETCHING_DATA.create();
		}
	}
	
	private static JsonNode fetchSummoner(String summonerName, Region region) throws CommandSyntaxException
	{
		String query = region.getApiUrl() + "lol/summoner/v4/summoners/by-name/" + CommandLeagueOfLegends.encodeSummonerName(summonerName) + "?api_key=" + TS3Bot.getInstance().getConfig().getApiLeagueOfLegends();
		
		try
		{
			return new ObjectMapper().readTree(new URL(query));
		}
		catch(IOException e)
		{
			throw SUMMONER_NOT_FOUND.create(summonerName);
		}
	}
	
	private static JsonNode fetchLeague(String summonerId, Region region) throws CommandSyntaxException
	{
		String query = region.getApiUrl() + "lol/league/v4/entries/by-summoner/" + summonerId + "?api_key=" + TS3Bot.getInstance().getConfig().getApiLeagueOfLegends();
		
		try
		{
			return new ObjectMapper().readTree(new URL(query));
		}
		catch(IOException e)
		{
			throw ERROR_FETCHING_DATA.create();
		}
	}
	
	private static JsonNode fetchActiveMatch(String summonerName, String summonerId, Region region) throws CommandSyntaxException
	{
		String query = region.getApiUrl() + "lol/spectator/v4/active-games/by-summoner/" + summonerId + "?api_key=" + TS3Bot.getInstance().getConfig().getApiLeagueOfLegends();
		
		try
		{
			return new ObjectMapper().readTree(new URL(query));
		}
		catch(IOException e)
		{
			throw SUMMONER_NOT_IN_GAME.create(summonerName);
		}
	}
	
	private static JsonNode fetchMatchHistory(String summonerId, Region region, int beginIndex, int endIndex) throws CommandSyntaxException
	{
		String query = region.getApiUrl() + "lol/match/v4/matchlists/by-account/" + summonerId + "?api_key=" + TS3Bot.getInstance().getConfig().getApiLeagueOfLegends() + "&beginIndex=" + beginIndex + "&endIndex=" + endIndex;
		
		try
		{
			return new ObjectMapper().readTree(new URL(query));
		}
		catch(IOException e)
		{
			throw ERROR_FETCHING_DATA.create();
		}
	}
	
	private static JsonNode fetchMatch(long matchId, Region region) throws CommandSyntaxException
	{
		String query = region.getApiUrl() + "lol/match/v4/matches/" + matchId + "?api_key=" + TS3Bot.getInstance().getConfig().getApiLeagueOfLegends();
		
		try
		{
			return new ObjectMapper().readTree(new URL(query));
		}
		catch(IOException e)
		{
			throw ERROR_FETCHING_DATA.create();
		}
	}
	
	private static String getHighestRank(JsonNode league)
	{
		return StreamSupport.stream(league.spliterator(), false)
			.map(node -> CommandLeagueOfLegends.trySupply(() -> new ObjectMapper().treeToValue(node, League.class)))
			.filter(Objects::nonNull)
			.sorted()
			.findFirst()
			.map(League::toString)
			.orElse("Unranked");
	}
	
	private static int getParticinantId(JsonNode match, String summonerId)
	{
		JsonNode participantIdentities = match.path("participantIdentities");
		
		for(int x = 0; x < participantIdentities.size(); x++)
		{
			JsonNode participant = participantIdentities.get(x);
			
			if(participant.path("player").path("summonerId").asText().equals(summonerId))
			{
				return participant.path("participantId").asInt();
			}
		}
		
		return -1;
	}
	
	private static JsonNode getParticipant(JsonNode match, int participantId)
	{
		JsonNode participants = match.path("participants");
		
		for(int x = 0; x < participants.size(); x++)
		{
			JsonNode participant = participants.get(x);
			
			if(participant.path("participantId").asInt() == participantId)
			{
				return participant;
			}
		}
		
		return MissingNode.getInstance();
	}
	
	private static String encodeSummonerName(String summonerName) throws CommandSyntaxException
	{
		try
		{
			return URLEncoder.encode(summonerName, "UTF-8");
		}
		catch(UnsupportedEncodingException e)
		{
			throw ERROR_FETCHING_DATA.create();
		}
	}
	
	private static <T> T trySupply(ThrowableSupplier<T> supplier)
	{
		try
		{
			return supplier.get();
		}
		catch(Throwable e)
		{
			return null;
		}
	}
	
	public static class League implements Comparable<League>
	{
		@JsonProperty("leagueId")
		private UUID leagueId;
		@JsonProperty("queueType")
		private Queue queueType;
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
		
		public UUID getLeagueId()
		{
			return this.leagueId;
		}
		
		public Queue getQueueType()
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
		
		@Override
		public int compareTo(League league)
		{
			int tier = league.getTier().compareTo(this.tier);
			
			if(tier == 0)
			{
				return league.getRank().compareTo(this.rank);
			}
			
			return tier;
		}
		
		@Override
		public String toString()
		{
			return this.tier.getName() + " " + this.rank;
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
		
		public String getApiUrl()
		{
			return String.format(API_URL, this.id);
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
	
	public static class RegionArumentType implements ArgumentType<Region>
	{
		public static RegionArumentType region()
		{
			return new RegionArumentType();
		}
		
		public static Region getRegion(CommandContext<?> context, String name)
		{
			return context.getArgument(name, Region.class);
		}
		
		@Override
		public Region parse(StringReader reader) throws CommandSyntaxException
		{
			reader.readUnquotedString();
			
			return null;
		}
	}
	
	public static enum Queue
	{
		RANKED_SOLO_5x5("Ranked Solo/Duo"),
		RANKED_FLEX_SR("Ranked Flex");
		
		private final String name;
		
		private Queue(String name)
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
		IRON("Iron"),
		BRONZE("Bronze"),
		SILVER("Silver"),
		GOLD("Gold"),
		PLATINUM("Platinum"),
		DIAMOND("Diamond"),
		MASTER("Master"),
		GRANDMASTER("Grandmaster"),
		CHALLENGER("Challenger");
		
		private final String name;
		
		private Tier(String name)
		{
			this.name = name;
		}
		
		public String getName()
		{
			return this.name;
		}
	}
	
	public static enum Rank
	{
		I,
		II,
		III,
		IV,
		V;
	}
	
	public static interface ThrowableSupplier<T>
	{
		T get() throws Throwable;
	}
}
