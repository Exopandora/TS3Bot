package net.kardexo.ts3bot.commands.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
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
import net.kardexo.ts3bot.util.Util;

public class CommandLeagueOfLegends
{
	private static final DynamicCommandExceptionType SUMMONER_NOT_FOUND = new DynamicCommandExceptionType(summoner -> new LiteralMessage("Could not find summoner " + summoner));
	private static final DynamicCommandExceptionType SUMMONER_NOT_IN_GAME = new DynamicCommandExceptionType(summoner -> new LiteralMessage(summoner + " is currently not in an active game"));
	private static final DynamicCommandExceptionType CHAMPION_NOT_FOUND = new DynamicCommandExceptionType(champion -> new LiteralMessage("Could not find champion " + champion));
	private static final SimpleCommandExceptionType ERROR_FETCHING_DATA = new SimpleCommandExceptionType(new LiteralMessage("Error fetching data"));
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM");
	
	private static final String API_URL = "https://%s.api.riotgames.com/";
	private static final URI DDRAGON_API_URL = URI.create("https://ddragon.leagueoflegends.com/");
	private static final URI STATIC_DOC_API_URL = URI.create("http://static.developer.riotgames.com/docs/");
	
	private static final int MAX_RATING = Arrays.stream(Tier.VALUES).mapToInt(tier -> tier.hasRanks() ? Rank.VALUES.length : 1).sum();
	private static final int HTTP_RETRIES = 10;
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("lol")
				.then(Commands.literal("match")
						.executes(context -> match(context, context.getSource().getClientInfo().getNickname(), TS3Bot.getInstance().getConfig().getLoLRegion()))
						.then(Commands.argument("summoner", StringArgumentType.greedyString())
								.executes(context -> match(context, StringArgumentType.getString(context, "summoner"), TS3Bot.getInstance().getConfig().getLoLRegion()))))
				.then(Commands.literal("history")
						.executes(context -> history(context, context.getSource().getClientInfo().getNickname(), TS3Bot.getInstance().getConfig().getLoLRegion()))
						.then(Commands.argument("summoner", StringArgumentType.greedyString())
								.executes(context -> history(context, StringArgumentType.getString(context, "summoner"), TS3Bot.getInstance().getConfig().getLoLRegion()))))
				.then(Commands.literal("lore")
						.then(Commands.argument("champion", StringArgumentType.greedyString())
								.executes(context -> lore(context, StringArgumentType.getString(context, "champion"))))));
	}
	
	private static int match(CommandContext<CommandSource> context, String username, Region region) throws CommandSyntaxException
	{
		JsonNode champions = CommandLeagueOfLegends.fetchChampions(CommandLeagueOfLegends.fetchVersion());
		JsonNode summoner = CommandLeagueOfLegends.fetchSummoner(username, region);
		
		if(summoner == null)
		{
			throw SUMMONER_NOT_FOUND.create(username);
		}
		
		JsonNode match = CommandLeagueOfLegends.fetchActiveMatch(summoner.path("id").asText(), region);
		
		if(match == null)
		{
			throw SUMMONER_NOT_IN_GAME.create(username);
		}
		
		JsonNode queues = CommandLeagueOfLegends.fetchQueues();
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
				builder.append("\n" + Util.repeat("\t", 7) + "[b]VS[/b]");
			}
			
			if(!teamMembers.isEmpty())
			{
				int rankedTeamMembers = 0;
				
				for(JsonNode participant : teamMembers)
				{
					builder.append("\n[[color=" + color + "]" + CommandLeagueOfLegends.championById(participant.path("championId").asLong(), champions) + "[/color]]");
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
						Optional<League> optional = CommandLeagueOfLegends.highestRank(leagues);
						
						if(optional.isPresent())
						{
							League league = optional.get();
							teamRatings[x] += league.getRating();
							rankedTeamMembers++;
							builder.append(" | " + league.toString());
						}
						else
						{
							builder.append(" | Unranked");
						}
						
						JsonNode mastery = CommandLeagueOfLegends.fetchChampionMastery(summonerId, participant.path("championId").asLong(), region);
						
						if(mastery != null)
						{
							builder.append(" | " + mastery.path("championPoints").asInt() + " (" + mastery.path("championLevel").asInt() + ")");
						}
						else
						{
							builder.append(" | 0 (1)");
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
		
		String queue = CommandLeagueOfLegends.formatQueue(queues, match.path("gameQueueConfigId").asInt());
		String ranks = Arrays.stream(teamRatings)
				.mapToObj(CommandLeagueOfLegends::ratingToLeague)
				.map(league -> league.isPresent() ? league.get().toString() : "Unranked")
				.collect(Collectors.joining(" vs "));
		long gameLength = match.path("gameLength").asLong();
		
		builder.append("\n" + queue);
		builder.append(" | " + (gameLength < 0 ? "Loading Screen" : Util.formatDuration(gameLength)));
		builder.append(" | Average Ranks: " + ranks);
		
		context.getSource().sendFeedback(builder.toString());
		return participants.size();
	}
	
	private static int history(CommandContext<CommandSource> context, String username, Region region) throws CommandSyntaxException
	{
		CompletableFuture<JsonNode> championsFuture = CompletableFuture.supplyAsync(CommandLeagueOfLegends::fetchVersion).thenApply(version ->
		{
			return version != null ? CommandLeagueOfLegends.fetchChampions(version) : null;
		});
		CompletableFuture<JsonNode> queuesFuture = CompletableFuture.supplyAsync(CommandLeagueOfLegends::fetchQueues);
		CompletableFuture<JsonNode> summonerFuture = CompletableFuture.supplyAsync(() -> CommandLeagueOfLegends.fetchSummoner(username, region));
		CompletableFuture<JsonNode> historyFuture = summonerFuture.thenApply(summoner ->
		{
			return summoner != null ? CommandLeagueOfLegends.fetchMatchHistory(summoner.path("accountId").asText(), region, 0, 20) : null;
		});
		
		try
		{
			if(summonerFuture.get() == null)
			{
				championsFuture.cancel(true);
				historyFuture.cancel(true);
				queuesFuture.cancel(true);
				
				throw SUMMONER_NOT_FOUND.create(username);
			}
			
			if(championsFuture.get() == null || historyFuture.get() == null)
			{
				throw ERROR_FETCHING_DATA.create();
			}
			
			JsonNode champions = championsFuture.get();
			JsonNode summoner = summonerFuture.get();
			JsonNode history = historyFuture.get().path("matches");
			
			List<CompletableFuture<JsonNode>> matches = new ArrayList<CompletableFuture<JsonNode>>(history.size());
			
			for(JsonNode match : history)
			{
				matches.add(CompletableFuture.supplyAsync(() -> CommandLeagueOfLegends.fetchMatch(match.path("gameId").asLong(), region)));
			}
			
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
			
			JsonNode queues = queuesFuture.get();
			
			if(queues == null)
			{
				throw ERROR_FETCHING_DATA.create();
			}
			
			for(int x = 0; x < history.size(); x++)
			{
				JsonNode match = matches.get(x).get();
				
				if(match == null)
				{
					throw ERROR_FETCHING_DATA.create();
				}
				
				int participantId = CommandLeagueOfLegends.particinantId(match, summoner.path("id").asText());
				JsonNode participant = CommandLeagueOfLegends.participant(match, participantId);
				JsonNode stats = participant.path("stats");
				String champion = CommandLeagueOfLegends.championById(participant.path("championId").asLong(), champions);
				long gameDuration = match.path("gameDuration").asLong();
				boolean winner = stats.path("win").asBoolean();
				int kills = stats.path("kills").asInt();
				int deaths = stats.path("deaths").asInt();
				int assists = stats.path("assists").asInt();
				Date date = new Date(match.path("gameCreation").asLong());
				
				builder.append("\n[[color=" + (winner ? "green" : "#FF2345") + "]" + (x + 1) + "[/color]]");
				builder.append(" " + Util.formatDuration(gameDuration));
				builder.append(" | " + DATE_FORMAT.format(date));
				builder.append(" | " + CommandLeagueOfLegends.formatQueue(queues, match.path("queueId").asInt()));
				builder.append(" | " + kills + "/" + deaths + "/" + assists);
				builder.append(" | " + champion);
				
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
			
			double matchCount = history.size();
			String kills = String.format(Locale.ENGLISH, "%.01f", totalKills / matchCount);
			String deaths = String.format(Locale.ENGLISH, "%.01f", totalDeaths / matchCount);
			String assists = String.format(Locale.ENGLISH, "%.01f", totalAssists / matchCount);
			
			builder.append("\nWinrate: " + Math.round(wins * 100 / matchCount) + "%");
			builder.append(" | Average KDA: " + kills + "/" + deaths + "/" + assists);
			builder.append(" | Average Match Length: " + Util.formatDuration((long) (totalDuration / matchCount)));
			builder.append(" | Playtime Today: " + Util.formatDuration(playtimeToday));
			
			context.getSource().sendFeedback(builder.toString());
			return wins;
		}
		catch(InterruptedException | ExecutionException e)
		{
			throw ERROR_FETCHING_DATA.create();
		}
	}
	
	private static int lore(CommandContext<CommandSource> context, String champion) throws CommandSyntaxException
	{
		String normal = CommandLeagueOfLegends.normalizeChampionName(champion);
		String version = CommandLeagueOfLegends.fetchVersion();
		
		if(version == null)
		{
			throw ERROR_FETCHING_DATA.create();
		}
		
		JsonNode champions = CommandLeagueOfLegends.fetchChampions(version);
		
		if(champions == null)
		{
			throw ERROR_FETCHING_DATA.create();
		}
		
		Iterator<JsonNode> iterator = champions.path("data").iterator();
		
		while(iterator.hasNext())
		{
			JsonNode node = iterator.next();
			
			if(CommandLeagueOfLegends.normalizeChampionName(node.path("name").asText()).equals(normal))
			{
				String id = node.path("id").asText();
				JsonNode champ = CommandLeagueOfLegends.fetchChampion(version, id);
				
				if(champ == null)
				{
					throw ERROR_FETCHING_DATA.create();
				}
				
				JsonNode data = champ.path("data").path(id);
				StringBuilder builder = new StringBuilder();
				builder.append("\n" + data.path("name").asText() + " " + data.path("title").asText());
				builder.append("\n" + data.path("lore").asText());
				context.getSource().sendFeedback(builder.toString());
				
				return data.path("key").asInt();
			}
		}
		
		throw CHAMPION_NOT_FOUND.create(champion);
	}
	
	private static String normalizeChampionName(String champion)
	{
		return champion.replaceAll("[^A-Za-z]", "").toLowerCase();
	}
	
	private static String fetchVersion()
	{
		URI uri = DDRAGON_API_URL
			.resolve("api/versions.json");
		return CommandLeagueOfLegends.readJson(uri).get(0).asText();
	}
	
	private static JsonNode fetchQueues()
	{
		URI uri = STATIC_DOC_API_URL
			.resolve("lol/queues.json");
		return CommandLeagueOfLegends.readJson(uri);
	}
	
	private static JsonNode fetchChampions(String version)
	{
		URI uri = DDRAGON_API_URL
			.resolve("cdn/" + version + "/data/en_US/champion.json");
		return CommandLeagueOfLegends.readJson(uri);
	}
	
	private static JsonNode fetchChampion(String version, String champion) throws CommandSyntaxException
	{
		URI uri = DDRAGON_API_URL
			.resolve("cdn/" + version + "/data/en_US/" + champion + ".json");
		return CommandLeagueOfLegends.readJson(uri);
	}
	
	private static String championById(long championId, JsonNode champions)
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
		URI uri = region.getApiUrl()
			.resolve("lol/champion-mastery/v4/champion-masteries/by-summoner/" + summonderId + "/by-champion/" + championId);
		return CommandLeagueOfLegends.readJson(uri, true);
	}
	
	private static JsonNode fetchSummoner(String summonerName, Region region)
	{
		URI uri = region.getApiUrl()
			.resolve("lol/summoner/v4/summoners/by-name/" + CommandLeagueOfLegends.encodeSummonerName(summonerName));
		return CommandLeagueOfLegends.readJson(uri, true);
	}
	
	private static JsonNode fetchLeague(String summonerId, Region region) throws CommandSyntaxException
	{
		URI uri = region.getApiUrl()
			.resolve("lol/league/v4/entries/by-summoner/" + summonerId);
		return CommandLeagueOfLegends.readJson(uri, true);
	}
	
	private static JsonNode fetchActiveMatch(String summonerId, Region region) throws CommandSyntaxException
	{
		URI uri = region.getApiUrl()
			.resolve("lol/spectator/v4/active-games/by-summoner/" + summonerId);
		return CommandLeagueOfLegends.readJson(uri, true);
	}
	
	private static JsonNode fetchMatchHistory(String summonerId, Region region, int beginIndex, int endIndex)
	{
		try
		{
			URI uri = new URIBuilder(region.getApiUrl().resolve("lol/match/v4/matchlists/by-account/" + summonerId))
				.addParameter("beginIndex", String.valueOf(beginIndex))
				.addParameter("endIndex", String.valueOf(endIndex))
				.build();
			return CommandLeagueOfLegends.readJson(uri, true);
		}
		catch(URISyntaxException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static JsonNode fetchMatch(long matchId, Region region)
	{
		URI uri = region.getApiUrl()
			.resolve("lol/match/v4/matches/" + matchId);
		return CommandLeagueOfLegends.readJson(uri, true);
	}
	
	private static Optional<League> highestRank(JsonNode leagues)
	{
		return StreamSupport.stream(leagues.spliterator(), false)
			.map(CommandLeagueOfLegends::rankFromLeague)
			.filter(Objects::nonNull)
			.sorted()
			.findFirst();
	}
	
	private static League rankFromLeague(JsonNode league)
	{
		try
		{
			return TS3Bot.getInstance().getObjectMapper().treeToValue(league, League.class);
		}
		catch(Throwable e)
		{
			return null;
		}
	}
	
	private static int particinantId(JsonNode match, String summonerId)
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
	
	private static JsonNode participant(JsonNode match, int participantId)
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
	
	private static String encodeSummonerName(String summonerName)
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
	
	private static Optional<League> ratingToLeague(int rating)
	{
		if(rating <= 0 || rating > MAX_RATING)
		{
			return Optional.empty();
		}
		
		return Optional.of(CommandLeagueOfLegends.leagueFromRating(Tier.LOWEST, Rank.LOWEST, rating));
	}
	
	private static JsonNode readJson(URI uri)
	{
		return CommandLeagueOfLegends.readJson(uri, false);
	}
	
	private static JsonNode readJson(URI uri, boolean apikey)
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
	
	private static League leagueFromRating(Tier tier, Rank rank, int rating)
	{
		if(rating == 1)
		{
			return new League(tier, rank);
		}
		
		if(tier.hasRanks())
		{
			if(rating - 1 < Rank.VALUES.length)
			{
				return new League(tier, Rank.VALUES[rating - 1]);
			}
			else
			{
				return CommandLeagueOfLegends.leagueFromRating(tier.next(), Rank.LOWEST, rating - 4);
			}
		}
		
		return CommandLeagueOfLegends.leagueFromRating(tier.next(), null, rating - 1);
	}
	
	private static String formatQueue(JsonNode queues, int queueId)
	{
		for(int x = 0; x < queues.size(); x++)
		{
			JsonNode queue = queues.get(x);
			
			if(queue.path("queueId").asInt() == queueId)
			{
				return queue.path("description").asText().replaceFirst("^[0-9]v[0-9] ", "").replaceFirst(" games$", "");
			}
		}
		
		return "Unknown Queue";
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
