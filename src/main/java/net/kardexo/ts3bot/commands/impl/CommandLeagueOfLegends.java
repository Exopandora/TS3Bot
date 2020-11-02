package net.kardexo.ts3bot.commands.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
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
	
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final int MAX_RATING = Arrays.stream(Tier.VALUES).mapToInt(tier -> tier.hasRanks() ? Rank.VALUES.length : 1).sum();
	
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
		Map<Integer, List<JsonNode>> teamsMap = new HashMap<Integer, List<JsonNode>>();
		
		for(int x = 0; x < participants.size(); x++)
		{
			JsonNode participant = participants.get(x);
			List<JsonNode> team = teamsMap.computeIfAbsent(participant.path("teamId").asInt(), key -> new ArrayList<JsonNode>());
			team.add(participant);
		}
		
		List<Entry<Integer, List<JsonNode>>> teamsList = new ArrayList<Entry<Integer, List<JsonNode>>>(teamsMap.entrySet());
		teamsList.sort((a, b) -> Integer.compare(a.getKey(), b.getKey()));
		int[] teamRatings = new int[teamsList.size()];
		
		for(int x = 0; x < teamsList.size(); x++)
		{
			List<JsonNode> teamMembers = teamsList.get(x).getValue();
			String color = x % 2 == 0 ? "blue" : "red";
			
			if(x > 0)
			{
				builder.append("\n" + StringUtils.repeat("\t", 7) + "[b]VS[/b]");
			}
			
			if(!teamMembers.isEmpty())
			{
				int rankedTeamMembers = 0;
				
				for(JsonNode participant : teamMembers)
				{
					builder.append("\n[[color=" + color + "]" + CommandLeagueOfLegends.getChampionById(participant.path("championId").asLong(), champions) + "[/color]]");
					
					String summonerName = participant.path("summonerName").asText();
					
					if(summonerName.equalsIgnoreCase(username))
					{
						builder.append(" [b]" + summonerName + "[/b]");
					}
					else
					{
						builder.append(" " + summonerName);
					}
					
					if(!participant.path("bot").asBoolean())
					{
						String summonerId = CommandLeagueOfLegends.encodeSummonerName(participant.path("summonerId").asText());
						JsonNode leagues = CommandLeagueOfLegends.fetchLeague(summonerId, region);
						Optional<League> optional = CommandLeagueOfLegends.getHighestRank(leagues);
						
						if(optional.isPresent())
						{
							League league = optional.get();
							teamRatings[x] += league.getRating();
							rankedTeamMembers++;
							builder.append(" - " + league.toString());
						}
						else
						{
							builder.append(" - Unranked");
						}
						
						try
						{
							JsonNode mastery = CommandLeagueOfLegends.fetchChampionMastery(summonerId, participant.path("championId").asLong(), region);
							builder.append(" - " + mastery.path("championPoints").asInt() + " (" + mastery.path("championLevel").asInt() + ")");
						}
						catch(CommandSyntaxException e)
						{
							builder.append(" - 0 (1)");
						}
					}
				}
				
				teamRatings[x] = Math.round((float) teamRatings[x] / (float) rankedTeamMembers);
			}
			else
			{
				builder.append("\nNone");
			}
		}
		
		String ranks = Arrays.stream(teamRatings)
				.mapToObj(CommandLeagueOfLegends::ratingToLeague)
				.map(league -> league.isPresent() ? league.get().toString() : "Unranked")
				.collect(Collectors.joining(" - "));
		
		builder.append("\n" + match.path("gameMode").asText());
		builder.append(" - " + StringUtils.formatDuration(match.path("gameLength").asLong()));
		builder.append(" - Average Ranks: " + ranks);
		
		context.getSource().sendFeedback(builder.toString());
		return participants.size();
	}
	
	private static int history(CommandContext<CommandSource> context, CommandDispatcher<CommandSource> dispatcher, String username, Region region) throws CommandSyntaxException
	{
		JsonNode champions = CommandLeagueOfLegends.fetchChampions(CommandLeagueOfLegends.fetchVersion());
		JsonNode summoner = CommandLeagueOfLegends.fetchSummoner(username, region);
		JsonNode history = CommandLeagueOfLegends.fetchMatchHistory(summoner.path("accountId").asText(), region, 0, 15).path("matches");
		
		StringBuilder builder = new StringBuilder();
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		Date today = calendar.getTime();
		
		int wins = 0;
		int totalKills = 0;
		int totalDeaths = 0;
		int totalAssists = 0;
		long totalDuration = 0;
		long playtimeToday = 0;
		
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
			Date date = new Date(match.path("gameCreation").asLong());
			
			builder.append("\n[[color=" + (winner ? "green" : "#FF2345") + "]" + (x + 1) + "[/color]]");
			builder.append(" " + match.path("gameMode").asText());
			builder.append(" - " + DATE_FORMAT.format(date));
			builder.append(" - " + StringUtils.formatDuration(gameDuration));
			builder.append(" - " + kills + "/" + deaths + "/" + assists);
			builder.append(" - " + champion);
			
			totalKills += kills;
			totalDeaths += deaths;
			totalAssists += assists;
			totalDuration += gameDuration;
			
			if(!date.before(today))
			{
				playtimeToday += gameDuration;
			}
			
			if(winner)
			{
				wins++;
			}
		}
		
		double matches = history.size();
		
		builder.append("\nWinrate: " + Math.round(wins * 100 / matches) + "%");
		
		String kills = String.format(Locale.ENGLISH, "%.03f", totalKills / matches);
		String deaths = String.format(Locale.ENGLISH, "%.03f", totalDeaths / matches);
		String assists = String.format(Locale.ENGLISH, "%.03f", totalAssists / matches);
		
		builder.append(" - Average KDA: " + kills + "/" + deaths + "/" + assists);
		builder.append(" - Average Match Length: " + StringUtils.formatDuration((long) (totalDuration / matches)));
		builder.append(" - Playtime Today: " + StringUtils.formatDuration(playtimeToday));
		
		context.getSource().sendFeedback(builder.toString());
		
		return wins;
	}
	
	private static String fetchVersion() throws CommandSyntaxException
	{
		try
		{
			return OBJECT_MAPPER.readTree(new URL(DDRAGON_API_URL + "api/versions.json")).get(0).asText();
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
			return OBJECT_MAPPER.readTree(new URL(DDRAGON_API_URL + "cdn/" + version + "/data/en_US/champion.json"));
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
	
	private static JsonNode fetchChampionMastery(String summonderId, long championId, Region region) throws CommandSyntaxException
	{
		String query = region.getApiUrl() + "lol/champion-mastery/v4/champion-masteries/by-summoner/" + summonderId + "/by-champion/"+ championId + "?api_key=" + TS3Bot.getInstance().getConfig().getApiLeagueOfLegends();
		
		try
		{
			return OBJECT_MAPPER.readTree(new URL(query));
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
			return OBJECT_MAPPER.readTree(new URL(query));
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
			return OBJECT_MAPPER.readTree(new URL(query));
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
			return OBJECT_MAPPER.readTree(new URL(query));
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
			return OBJECT_MAPPER.readTree(new URL(query));
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
			return OBJECT_MAPPER.readTree(new URL(query));
		}
		catch(IOException e)
		{
			throw ERROR_FETCHING_DATA.create();
		}
	}
	
	private static Optional<League> getHighestRank(JsonNode leagues)
	{
		return StreamSupport.stream(leagues.spliterator(), false)
			.map(CommandLeagueOfLegends::getRankFromLeague)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.sorted()
			.findFirst();
	}
	
	private static Optional<League> getRankFromLeague(JsonNode league)
	{
		try
		{
			return Optional.of(OBJECT_MAPPER.treeToValue(league, League.class));
		}
		catch(Throwable e)
		{
			return Optional.empty();
		}
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
	
	private static Optional<League> ratingToLeague(int rating)
	{
		if(rating <= 0 || rating > MAX_RATING)
		{
			return Optional.empty();
		}
		
		return Optional.of(CommandLeagueOfLegends.leagueFromRating(Tier.LOWEST, Rank.LOWEST, rating));
	}
	
	private static League leagueFromRating(Tier tier, Rank rank, int rating)
	{
		if(rating == 1)
		{
			return new League(tier, rank);
		}
		
		if(tier.hasRanks())
		{
			if(rating < Rank.VALUES.length + 1)
			{
				return new League(tier, Rank.VALUES[Rank.VALUES.length - rating]);
			}
			else
			{
				return CommandLeagueOfLegends.leagueFromRating(tier.next(), Rank.LOWEST, rating - 4);
			}
		}
		
		return CommandLeagueOfLegends.leagueFromRating(tier.next(), null, rating - 1);
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
		
		public League(Tier tier, Rank rank)
		{
			this.tier = tier;
			this.rank = rank;
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
		
		public int getRating()
		{
			return this.tier.rating(this.rank);
		}
		
		@Override
		public int compareTo(League league)
		{
			int tier = league.getTier().compareTo(this.tier);
			
			if(tier == 0 && this.rank != null && league.getRank() != null)
			{
				return league.getRank().compareTo(this.rank);
			}
			
			return tier;
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
		I,
		II,
		III,
		IV;
		
		public static final Rank HIGHEST = Rank.I;
		public static final Rank LOWEST = Rank.IV;
		public static final Rank[] VALUES = Rank.values();
		
		public int getRating()
		{
			return VALUES.length - this.ordinal();
		}
		
		public Rank next()
		{
			return VALUES[(this.ordinal() + 1) % VALUES.length];
		}
	}
}
