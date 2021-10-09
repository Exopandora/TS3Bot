package net.kardexo.ts3bot.commands.impl;

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
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.api.LeagueOfLegends;
import net.kardexo.ts3bot.api.LeagueOfLegends.League;
import net.kardexo.ts3bot.api.LeagueOfLegends.Rank;
import net.kardexo.ts3bot.api.LeagueOfLegends.Region;
import net.kardexo.ts3bot.api.LeagueOfLegends.Tier;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;
import net.kardexo.ts3bot.util.Util;

public class CommandLeagueOfLegends
{
	private static final DynamicCommandExceptionType ERROR_FETCHING_DATA = new DynamicCommandExceptionType(message ->
	{
		return new LiteralMessage(message != null && message instanceof Throwable ? CommandLeagueOfLegends.getRootThrowable((Throwable) message).getMessage() : "Error fetching data");
	});
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM");
	private static final int MAX_RATING = Arrays.stream(Tier.VALUES).mapToInt(tier -> tier.hasRanks() ? Rank.VALUES.length : 1).sum();
	
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
		var championsFuture = CompletableFuture.supplyAsync(wrapException(() -> LeagueOfLegends.fetchChampions()));
		var queuesFuture = CompletableFuture.supplyAsync(wrapException(LeagueOfLegends::fetchQueues));
		var match = CompletableFuture.supplyAsync(wrapException(() ->
		{
			var summoner = LeagueOfLegends.fetchSummoner(username, region);
			
			if(summoner.hasNonNull("status"))
			{
				throw new RuntimeException("Could not find summoner " + username);
			}
			
			var activeMatch = LeagueOfLegends.fetchActiveMatch(summoner.path("id").asText(), region);
			
			if(activeMatch.hasNonNull("status"))
			{
				throw new RuntimeException(username + " is currently not in an active game");
			}
			
			var participants = activeMatch.path("participants");
			var builder = new StringBuilder();
			var teams = CommandLeagueOfLegends.groupAndLoad(participants, championsFuture, region).entrySet().stream()
					.sorted((a, b) -> Integer.compare(a.getKey(), b.getKey()))
					.map(Entry::getValue)
					.collect(Collectors.toList());
			var teamRatings = new int[teams.size()];
			
			for(int x = 0; x < teams.size(); x++)
			{
				var teamMembers = teams.get(x);
				var color = x % 2 == 0 ? "blue" : "red";
				
				if(x > 0)
				{
					builder.append("\n" + Util.repeat("\t", 7) + "[b]VS[/b]");
				}
				
				if(!teamMembers.isEmpty())
				{
					int rankedTeamMembers = 0;
					
					for(CompletableFuture<SummonerOverview> participant : teamMembers)
					{
						var overview = participant.join();
						
						builder.append("\n[color=" + color + "]" + overview.champion() + "[/color]");
						
						if(overview.summonerName().equalsIgnoreCase(username))
						{
							builder.append(" [b]" + overview.summonerName() + "[/b]");
						}
						else
						{
							builder.append(" " + overview.summonerName());
						}
						
						var league = overview.highestRank();
						
						if(league.isPresent())
						{
							teamRatings[x] += league.get().getRating();
							rankedTeamMembers++;
						}
						
						builder.append(" | " + overview.leagueString(league));
						builder.append(" | " + overview.masteryString());
					}
					
					teamRatings[x] = Math.round((float) teamRatings[x] / (float) rankedTeamMembers);
				}
				else
				{
					builder.append("\nNone");
				}
			}
			
			var ranks = Arrays.stream(teamRatings)
					.mapToObj(CommandLeagueOfLegends::ratingToLeague)
					.map(league -> league.map(League::toString).orElse("Unranked"))
					.collect(Collectors.joining(" vs "));
			var gameLength = activeMatch.path("gameLength").asLong();
			var formattedGameLength = gameLength < 0 ? "Loading Screen" : Util.formatDuration(gameLength);
			var queue = CommandLeagueOfLegends.formatQueue(queuesFuture.join(), activeMatch.path("gameQueueConfigId").asInt());
			
			builder.append("\n" + queue);
			builder.append(" | " + formattedGameLength);
			builder.append(" | Average Ranks: " + ranks);
			
			context.getSource().sendFeedback(builder.toString());
			return participants.size();
		}));
		
		try
		{
			return match.join();
		}
		catch(CancellationException | CompletionException e)
		{
			throw ERROR_FETCHING_DATA.create(e);
		}
	}
	
	private static CompletableFuture<SummonerOverview> loadParticipantAsync(JsonNode participant, CompletableFuture<JsonNode> championsFuture, Region region)
	{
		var championId = participant.path("championId").asLong();
		var championFuture = championsFuture.thenApply(champions -> CommandLeagueOfLegends.championById(championId, champions));
		var summonerName = participant.path("summonerName").asText();
		
		if(!participant.path("bot").asBoolean())
		{
			var summonerId = LeagueOfLegends.encodeSummonerName(participant.path("summonerId").asText());
			var league = CompletableFuture.supplyAsync(wrapException(() -> LeagueOfLegends.fetchLeague(summonerId, region)));
			var mastery = CompletableFuture.supplyAsync(wrapException(() -> LeagueOfLegends.fetchChampionMastery(summonerId, championId, region)));
			return CompletableFuture.allOf(championFuture, league, mastery).thenApply(__ -> new SummonerOverview(summonerName, championFuture.join(), league.join(), mastery.join()));
		}
		
		return championFuture.thenApply(champion -> new SummonerOverview(summonerName, champion, null, null));
	}
	
	private static record SummonerOverview(String summonerName, String champion, JsonNode league, JsonNode mastery)
	{
		public String masteryString()
		{
			if(this.mastery != null)
			{
				return this.mastery.path("championPoints").asInt() + " (" + this.mastery.path("championLevel").asInt() + ")";
			}
			
			return "0 (1)";
		}
		
		public Optional<League> highestRank()
		{
			if(this.league != null)
			{
				return CommandLeagueOfLegends.highestRank(this.league);
			}
			
			return Optional.empty();
		}
		
		public String leagueString(Optional<League> league)
		{
			if(league.isPresent())
			{
				return league.get().toString();
			}
			
			return "Unranked";
		}
	}
	
	private static int history(CommandContext<CommandSource> context, String username, Region region) throws CommandSyntaxException
	{
		var queuesFuture = CompletableFuture.supplyAsync(wrapException(LeagueOfLegends::fetchQueues));
		var championsFuture = CompletableFuture.supplyAsync(wrapException(() -> LeagueOfLegends.fetchChampions()));
		var history = CompletableFuture.supplyAsync(wrapException(() ->
		{
			var summoner = LeagueOfLegends.fetchSummoner(username, region);
			
			if(summoner.hasNonNull("status"))
			{
				throw new RuntimeException("Could not find summoner " + summoner);
			}
			
			return summoner.path("puuid").asText();
		})).thenApplyAsync(wrapException(puuid ->
		{
			var matchIds = LeagueOfLegends.fetchMatchHistory(puuid, region.getRegionV5(), 0, 20);
			var matches = new ArrayList<CompletableFuture<JsonNode>>(matchIds.size());
			
			for(JsonNode matchId : matchIds)
			{
				matches.add(CompletableFuture.supplyAsync(wrapException(() -> LeagueOfLegends.fetchMatch(matchId.asText(), region.getRegionV5()))));
			}
			
			var calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			
			var today = calendar.getTime();
			var builder = new StringBuilder();
			var champions = championsFuture.join();
			var queues = queuesFuture.join();
			var stats = new HistoryStats();
			
			for(int x = 0; x < matches.size(); x++)
			{
				var info = matches.get(x).join().path("info");
				var participant = CommandLeagueOfLegends.findParticipant(info.path("participants"), puuid);
				var gameStart = info.path("gameStartTimestamp").asLong();
				var gameEnd = info.path("gameEndTimestamp").asLong();
				var date = new Date(gameStart);
				var champion = CommandLeagueOfLegends.championById(participant.path("championId").asLong(), champions);
				var queue = CommandLeagueOfLegends.formatQueue(queues, info.path("queueId").asInt());
				var playTime = (gameEnd - gameStart) / 1000L;
				var kills = participant.path("kills").asInt();
				var deaths = participant.path("deaths").asInt();
				var assists = participant.path("assists").asInt();
				var winner = participant.path("win").asBoolean();
				var formattedDate = DATE_FORMAT.format(date);
				var fomattedPlayTime = Util.formatDuration(playTime);
				var color = winner ? "green" : "#FF2345";
				
				if(winner)
				{
					stats.addWin();
				}
				
				if(!date.before(today))
				{
					stats.addPlaytimeToday(playTime);
				}
				
				stats.addPlaytime(playTime);
				stats.addKills(kills);
				stats.addDeaths(deaths);
				stats.addAssists(assists);
				
				builder.append("\n[[color=" + color + "]" + (x + 1) + "[/color]]");
				builder.append(" " + fomattedPlayTime);
				builder.append(" | " + formattedDate);
				builder.append(" | " + queue);
				builder.append(" | " + kills + "/" + deaths + "/" + assists);
				builder.append(" | " + champion);
			}
			
			var matchCount = (double) matches.size();
			var winrate = Math.round(stats.getWins() * 100 / matchCount);
			var kills = String.format(Locale.ENGLISH, "%.01f", stats.getKills() / matchCount);
			var deaths = String.format(Locale.ENGLISH, "%.01f", stats.getDeaths() / matchCount);
			var assists = String.format(Locale.ENGLISH, "%.01f", stats.getAssists() / matchCount);
			var averageMatchLength = Util.formatDuration((long) (stats.getPlaytime() / matchCount));
			var playTimeToday = Util.formatDuration(stats.getPlaytimeToday());
			
			builder.append("\nWinrate: " + winrate + "%");
			builder.append(" | Average KDA: " + kills + "/" + deaths + "/" + assists);
			builder.append(" | Average Match Length: " + averageMatchLength);
			builder.append(" | Playtime Today: " + playTimeToday);
			
			context.getSource().sendFeedback(builder.toString());
			return stats.getWins();
		}));
		
		try
		{
			return history.join();
		}
		catch(CancellationException | CompletionException e)
		{
			throw ERROR_FETCHING_DATA.create(e);
		}
	}
	
	private static JsonNode findParticipant(JsonNode participants, String puuid)
	{
		for(JsonNode participant : participants)
		{
			if(participant.path("puuid").asText().equals(puuid))
			{
				return participant;
			}
		}
		
		return MissingNode.getInstance();
	}
	
	private static int lore(CommandContext<CommandSource> context, String champion) throws CommandSyntaxException
	{
		var result = CompletableFuture.supplyAsync(wrapException(() ->
		{
			var normal = CommandLeagueOfLegends.normalizeChampionName(champion);
			var version = LeagueOfLegends.fetchVersion();
			var champions = LeagueOfLegends.fetchChampions(version);
			var iterator = champions.path("data").iterator();
			
			while(iterator.hasNext())
			{
				var node = iterator.next();
				
				if(CommandLeagueOfLegends.normalizeChampionName(node.path("name").asText()).equals(normal))
				{
					var id = node.path("id").asText();
					var champ = LeagueOfLegends.fetchChampion(version, id);
					var data = champ.path("data").path(id);
					var builder = new StringBuilder();
					
					builder.append("\n" + data.path("name").asText() + " " + data.path("title").asText());
					builder.append("\n" + data.path("lore").asText());
					context.getSource().sendFeedback(builder.toString());
					
					return data.path("key").asInt();
				}
			}
			
			throw new RuntimeException("Could not find champion " + champion);
		}));
		
		try
		{
			return result.join();
		}
		catch(CancellationException | CompletionException e)
		{
			throw ERROR_FETCHING_DATA.create(e);
		}
	}
	
	private static String normalizeChampionName(String champion)
	{
		return champion.replaceAll("[^A-Za-z]", "").toLowerCase();
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
	
	private static Map<Integer, List<CompletableFuture<SummonerOverview>>> groupAndLoad(JsonNode participants, CompletableFuture<JsonNode> champions, Region region)
	{
		Map<Integer, List<CompletableFuture<SummonerOverview>>> map = new HashMap<Integer, List<CompletableFuture<SummonerOverview>>>();
		
		for(int x = 0; x < participants.size(); x++)
		{
			JsonNode participant = participants.get(x);
			List<CompletableFuture<SummonerOverview>> team = map.computeIfAbsent(participant.path("teamId").asInt(), key -> new ArrayList<CompletableFuture<SummonerOverview>>());
			team.add(CommandLeagueOfLegends.loadParticipantAsync(participant, champions, region));
		}
		
		return map;
	}
	
	private static class HistoryStats
	{
		private int wins;
		private int kills = 0;
		private int deaths = 0;
		private int assists = 0;
		private long playtime = 0;
		private long playtimeToday = 0;
		
		public int getWins()
		{
			return this.wins;
		}
		
		public void addWin()
		{
			this.wins++;
		}
		
		public int getKills()
		{
			return this.kills;
		}
		
		public void addKills(int kills)
		{
			this.kills += kills;
		}
		
		public int getDeaths()
		{
			return this.deaths;
		}
		
		public void addDeaths(int deaths)
		{
			this.deaths += deaths;
		}
		
		public int getAssists()
		{
			return this.assists;
		}
		
		public void addAssists(int assists)
		{
			this.assists += assists;
		}
		
		public long getPlaytime()
		{
			return this.playtime;
		}
		
		public void addPlaytime(long playtime)
		{
			this.playtime += playtime;
		}
		
		public long getPlaytimeToday()
		{
			return this.playtimeToday;
		}
		
		public void addPlaytimeToday(long playtimeToday)
		{
			this.playtimeToday += playtimeToday;
		}
	}
	
	private static Throwable getRootThrowable(Throwable throwable)
	{
		Throwable result = throwable;
		
		while(result.getCause() != null)
		{
			result = result.getCause();
		}
		
		return result;
	}
	
	private static <T> Supplier<T> wrapException(ThrowableSupplier<T> supplier)
	{
		return () ->
		{
			try
			{
				return supplier.get();
			}
			catch(Throwable e)
			{
				throw new RuntimeException(e);
			}
		};
	}
	
	private static <T, R> Function<T, R> wrapException(ThrowableFunction<T, R> function)
	{
		return t ->
		{
			try
			{
				return function.apply(t);
			}
			catch(Throwable e)
			{
				throw new RuntimeException(e);
			}
		};
	}
	
	@FunctionalInterface
	private static interface ThrowableSupplier<T>
	{
		T get() throws Throwable;
	}
	
	@FunctionalInterface
	private static interface ThrowableFunction<T, R>
	{
		R apply(T t) throws Throwable;
	}
}
