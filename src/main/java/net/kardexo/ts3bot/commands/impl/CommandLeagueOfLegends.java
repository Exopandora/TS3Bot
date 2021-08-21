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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

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
	private static final DynamicCommandExceptionType SUMMONER_NOT_FOUND = new DynamicCommandExceptionType(summoner -> new LiteralMessage("Could not find summoner " + summoner));
	private static final DynamicCommandExceptionType SUMMONER_NOT_IN_GAME = new DynamicCommandExceptionType(summoner -> new LiteralMessage(summoner + " is currently not in an active game"));
	private static final DynamicCommandExceptionType CHAMPION_NOT_FOUND = new DynamicCommandExceptionType(champion -> new LiteralMessage("Could not find champion " + champion));
	private static final SimpleCommandExceptionType ERROR_FETCHING_DATA = new SimpleCommandExceptionType(new LiteralMessage("Error fetching data"));
	
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
		CompletableFuture<JsonNode> champions = CompletableFuture.supplyAsync(LeagueOfLegends::fetchChampions);
		JsonNode summoner = LeagueOfLegends.fetchSummoner(username, region);
		
		if(summoner == null)
		{
			throw SUMMONER_NOT_FOUND.create(username);
		}
		
		JsonNode match = LeagueOfLegends.fetchActiveMatch(summoner.path("id").asText(), region);
		
		if(match == null)
		{
			throw SUMMONER_NOT_IN_GAME.create(username);
		}
		
		CompletableFuture<JsonNode> queues = CompletableFuture.supplyAsync(LeagueOfLegends::fetchQueues);
		StringBuilder builder = new StringBuilder();
		JsonNode participants = match.path("participants");
		List<List<CompletableFuture<SummonerOverview>>> teams = CommandLeagueOfLegends.groupAndLoad(participants, champions, region).entrySet().stream()
				.sorted((a, b) -> Integer.compare(a.getKey(), b.getKey()))
				.map(Entry::getValue)
				.collect(Collectors.toList());
		int[] teamRatings = new int[teams.size()];
		
		for(int x = 0; x < teams.size(); x++)
		{
			List<CompletableFuture<SummonerOverview>> teamMembers = teams.get(x);
			String color = x % 2 == 0 ? "blue" : "red";
			
			if(x > 0)
			{
				builder.append("\n" + Util.repeat("\t", 7) + "[b]VS[/b]");
			}
			
			if(!teamMembers.isEmpty())
			{
				int rankedTeamMembers = 0;
				
				for(CompletableFuture<SummonerOverview> participant : teamMembers)
				{
					try
					{
						SummonerOverview overview = participant.get();
						
						builder.append("\n[color=" + color + "]" + overview.champion() + "[/color]");
						
						if(overview.summonerName().equalsIgnoreCase(username))
						{
							builder.append(" [b]" + overview.summonerName() + "[/b]");
						}
						else
						{
							builder.append(" " + overview.summonerName());
						}
						
						Optional<League> league = overview.highestRank();
						
						if(league.isPresent())
						{
							teamRatings[x] += league.get().getRating();
							rankedTeamMembers++;
						}
						
						builder.append(" | " + overview.leagueString(league));
						builder.append(" | " + overview.masteryString());
					}
					catch(InterruptedException | ExecutionException e)
					{
						throw ERROR_FETCHING_DATA.create();
					}
				}
				
				teamRatings[x] = Math.round((float) teamRatings[x] / (float) rankedTeamMembers);
			}
			else
			{
				builder.append("\nNone");
			}
		}
		
		builder.append("\n");
		
		try
		{
			builder.append(CommandLeagueOfLegends.formatQueue(queues.get(), match.path("gameQueueConfigId").asInt()));
		}
		catch(InterruptedException | ExecutionException e)
		{
			e.printStackTrace();
		}
		
		String ranks = Arrays.stream(teamRatings)
				.mapToObj(CommandLeagueOfLegends::ratingToLeague)
				.map(league -> league.map(League::toString).orElse("Unranked"))
				.collect(Collectors.joining(" vs "));
		long gameLength = match.path("gameLength").asLong();
		
		builder.append(" | " + (gameLength < 0 ? "Loading Screen" : Util.formatDuration(gameLength)));
		builder.append(" | Average Ranks: " + ranks);
		
		context.getSource().sendFeedback(builder.toString());
		return participants.size();
	}
	
	private static CompletableFuture<SummonerOverview> loadParticipantAsync(JsonNode participant, CompletableFuture<JsonNode> champions, Region region)
	{
		long championId = participant.path("championId").asLong();
		CompletableFuture<String> championName = champions.thenApply(c -> CommandLeagueOfLegends.championById(championId, c));
		String summonerName = participant.path("summonerName").asText();
		
		if(!participant.path("bot").asBoolean())
		{
			String summonerId = LeagueOfLegends.encodeSummonerName(participant.path("summonerId").asText());
			CompletableFuture<JsonNode> league = CompletableFuture.supplyAsync(() -> LeagueOfLegends.fetchLeague(summonerId, region));
			CompletableFuture<JsonNode> mastery = CompletableFuture.supplyAsync(() -> LeagueOfLegends.fetchChampionMastery(summonerId, championId, region));
			return CompletableFuture.allOf(league, mastery).thenApply(__ -> new SummonerOverview(summonerName, championName.getNow(null), league.getNow(null), mastery.getNow(null)));
		}
		
		return championName.thenApply(c -> new SummonerOverview(summonerName, c, null, null));
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
		CompletableFuture<JsonNode> championsFuture = CompletableFuture.supplyAsync(LeagueOfLegends::fetchVersion).thenApply(version ->
		{
			return version != null ? LeagueOfLegends.fetchChampions(version) : null;
		});
		CompletableFuture<JsonNode> queuesFuture = CompletableFuture.supplyAsync(LeagueOfLegends::fetchQueues);
		CompletableFuture<JsonNode> summonerFuture = CompletableFuture.supplyAsync(() -> LeagueOfLegends.fetchSummoner(username, region));
		CompletableFuture<JsonNode> historyFuture = summonerFuture.thenApply(summoner ->
		{
			return summoner != null ? LeagueOfLegends.fetchMatchHistory(summoner.path("accountId").asText(), region, 0, 20) : null;
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
				matches.add(CompletableFuture.supplyAsync(() -> LeagueOfLegends.fetchMatch(match.path("gameId").asLong(), region)));
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
		String version = LeagueOfLegends.fetchVersion();
		
		if(version == null)
		{
			throw ERROR_FETCHING_DATA.create();
		}
		
		JsonNode champions = LeagueOfLegends.fetchChampions(version);
		
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
				JsonNode champ = LeagueOfLegends.fetchChampion(version, id);
				
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
}
