package net.kardexo.bot.services.commands.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.kardexo.bot.adapters.lol.League;
import net.kardexo.bot.adapters.lol.LeagueOfLegends;
import net.kardexo.bot.adapters.lol.Platform;
import net.kardexo.bot.adapters.lol.Rank;
import net.kardexo.bot.adapters.lol.RiotId;
import net.kardexo.bot.adapters.lol.Tier;
import net.kardexo.bot.domain.CommandSource;
import net.kardexo.bot.domain.FormattedStringBuilder;
import net.kardexo.bot.domain.Util;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.domain.api.IStyle;
import net.kardexo.bot.domain.config.Config;
import net.kardexo.bot.domain.config.UserConfig;
import net.kardexo.bot.services.api.IAPIKeyService;
import net.kardexo.bot.services.api.IUserConfigService;
import net.kardexo.bot.services.commands.Commands;
import net.kardexo.bot.services.commands.arguments.RiotIdArgumentType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
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

import static net.kardexo.bot.domain.Util.OBJECT_MAPPER;

public class LeagueOfLegendsCommand
{
	private static final DynamicCommandExceptionType ERROR_FETCHING_DATA = new DynamicCommandExceptionType(message ->
	{
		return new LiteralMessage(message instanceof Throwable ? LeagueOfLegendsCommand.getRootThrowable((Throwable) message).getMessage() : "Error fetching data");
	});
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM");
	private static final int MAX_RATING = Arrays.stream(Tier.VALUES).mapToInt(tier -> tier.isApexTier() ? 1 : Rank.VALUES.length).sum();
	private static final IStyle RED_COLOR = IStyle.color("E62142");
	private static final IStyle BLUE_COLOR = IStyle.color("4788B6");
	private static final IStyle WIN_COLOR = IStyle.color("008000");
	private static final IStyle LOSS_COLOR = IStyle.color("FF2345");
	
	public static void register(CommandDispatcher<CommandSource> dispatcher, Config config, IAPIKeyService apiKeyService, IUserConfigService userConfigService)
	{
		dispatcher.register(Commands.literal("lol")
			.then(Commands.literal("match")
				.executes(context -> match(context, apiKeyService, riotIdForUser(context, config.getLoLPlatform(), userConfigService), config.getLoLPlatform()))
				.then(Commands.argument("riot_id", RiotIdArgumentType.greedy(config.getLoLPlatform()))
					.executes(context -> match(context, apiKeyService, RiotIdArgumentType.getRiotId(context, "riot_id"), config.getLoLPlatform()))))
			.then(Commands.literal("history")
				.executes(context -> history(context, apiKeyService, riotIdForUser(context, config.getLoLPlatform(), userConfigService), config.getLoLPlatform()))
				.then(Commands.argument("riot_id", RiotIdArgumentType.greedy(config.getLoLPlatform()))
					.executes(context -> history(context, apiKeyService, RiotIdArgumentType.getRiotId(context, "riot_id"), config.getLoLPlatform()))))
			.then(Commands.literal("lore")
				.then(Commands.argument("champion", StringArgumentType.greedyString())
					.executes(context -> lore(context, StringArgumentType.getString(context, "champion")))))
			.then(Commands.literal("alias")
				.then(Commands.argument("summoner", StringArgumentType.greedyString())
					.executes(context -> alias(context, userConfigService, StringArgumentType.getString(context, "summoner")))))
			.then(Commands.literal("build")
				.then(Commands.literal("for")
					.then(Commands.argument("champion", StringArgumentType.string())
						.executes(context -> build(context, apiKeyService, BuildSelector.MINE, StringArgumentType.getString(context, "champion"), riotIdForUser(context, config.getLoLPlatform(), userConfigService), config.getLoLPlatform()))
						.then(Commands.argument("riot_id", RiotIdArgumentType.greedy(config.getLoLPlatform()))
							.executes(context -> build(context, apiKeyService, BuildSelector.MINE, StringArgumentType.getString(context, "champion"), RiotIdArgumentType.getRiotId(context, "riot_id"), config.getLoLPlatform()))))
					.then(Commands.literal("enemy")
						.then(Commands.argument("champion", StringArgumentType.string())
							.executes(context -> build(context, apiKeyService, BuildSelector.ENEMY, StringArgumentType.getString(context, "champion"), riotIdForUser(context, config.getLoLPlatform(), userConfigService), config.getLoLPlatform()))
							.then(Commands.argument("riot_id", RiotIdArgumentType.greedy(config.getLoLPlatform()))
								.executes(context -> build(context, apiKeyService, BuildSelector.ENEMY, StringArgumentType.getString(context, "champion"), RiotIdArgumentType.getRiotId(context, "riot_id"), config.getLoLPlatform())))))
					.then(Commands.literal("ally")
						.then(Commands.argument("champion", StringArgumentType.string())
							.executes(context -> build(context, apiKeyService, BuildSelector.ALLY, StringArgumentType.getString(context, "champion"), riotIdForUser(context, config.getLoLPlatform(), userConfigService), config.getLoLPlatform()))
							.then(Commands.argument("riot_id", RiotIdArgumentType.greedy(config.getLoLPlatform()))
								.executes(context -> build(context, apiKeyService, BuildSelector.ALLY, StringArgumentType.getString(context, "champion"), RiotIdArgumentType.getRiotId(context, "riot_id"), config.getLoLPlatform()))))))));
	}
	
	private static int build(CommandContext<CommandSource> context, IAPIKeyService apiKeyService, BuildSelector selector, String champion, RiotId riotId, Platform platform) throws CommandSyntaxException
	{
		var itemsFuture = CompletableFuture.supplyAsync(wrapException(() -> LeagueOfLegends.fetchItems()));
		var build = CompletableFuture.supplyAsync(wrapException(() ->
		{
			var account = LeagueOfLegends.fetchAccount(apiKeyService, riotId, platform);
			
			if(account.hasNonNull("status"))
			{
				throw new RuntimeException("Could not find user " + riotId);
			}
			
			return account.path("puuid").asText();
		})).thenApplyAsync(wrapException(puuid ->
		{
			var matchIds = LeagueOfLegends.fetchMatchHistory(apiKeyService, puuid, platform.getRegion(), 0, 20);
			
			if(matchIds.isEmpty())
			{
				throw new RuntimeException(riotId + " has not played any games yet");
			}
			
			var matches = new ArrayList<CompletableFuture<JsonNode>>(matchIds.size());
			
			for(JsonNode matchId : matchIds)
			{
				matches.add(CompletableFuture.supplyAsync(wrapException(() -> LeagueOfLegends.fetchMatch(apiKeyService, matchId.asText(), platform.getRegion()))));
			}
			
			var targetChampion = LeagueOfLegendsCommand.normalizeChampionName(champion);
			
			for(int x = 0; x < matches.size(); x++)
			{
				var info = matches.get(x).join().path("info");
				var puuid2participant = new HashMap<String, JsonNode>();
				var participants = info.path("participants");
				
				for(var participant : participants)
				{
					var participantPuuid = participant.path("puuid").asText();
					
					if(participantPuuid != null)
					{
						puuid2participant.put(participantPuuid, participant);
					}
				}
				
				var player = puuid2participant.get(puuid);
				var team = player.path("teamId").asInt();
				
				switch(selector)
				{
					case MINE:
						if(targetChampion.equals(normalizeChampionName(player.path("championName").asText())))
						{
							String response = createBuildResponse(player, player, itemsFuture.join());
							context.getSource().sendFeedback(response);
							return x;
						}
						break;
					case ENEMY:
						for(var participant : participants)
						{
							if(player.equals(participant))
							{
								continue;
							}
							
							if(team != participant.path("teamId").asInt() && targetChampion.equals(normalizeChampionName(participant.path("championName").asText())))
							{
								String response = createBuildResponse(participant, player, itemsFuture.join());
								context.getSource().sendFeedback(response);
								return x;
							}
						}
						break;
					case ALLY:
						for(var participant : participants)
						{
							if(player.equals(participant))
							{
								continue;
							}
							
							if(team == participant.path("teamId").asInt() && targetChampion.equals(normalizeChampionName(participant.path("championName").asText())))
							{
								String response = createBuildResponse(participant, player, itemsFuture.join());
								context.getSource().sendFeedback(response);
								return x;
							}
						}
						break;
				}
			}
			
			switch(selector)
			{
				case MINE:
					throw new RuntimeException("Could not find build for " + champion + " in the match history for " + riotId);
				case ALLY:
					throw new RuntimeException("Could not find build for ally " + champion + " in the match history for " + riotId);
				case ENEMY:
					throw new RuntimeException("Could not find build for enemy " + champion + " in the match history for " + riotId);
				default:
					throw new RuntimeException("Invalid build selector " + selector);
			}
		}));
		
		try
		{
			return build.join();
		}
		catch(CancellationException | CompletionException e)
		{
			throw ERROR_FETCHING_DATA.create(e);
		}
	}
	
	private static int match(CommandContext<CommandSource> context, IAPIKeyService apiKeyService, RiotId riotId, Platform platform) throws CommandSyntaxException
	{
		var championsFuture = CompletableFuture.supplyAsync(wrapException(() -> LeagueOfLegends.fetchChampions()));
		var queuesFuture = CompletableFuture.supplyAsync(wrapException(LeagueOfLegends::fetchQueues));
		var match = CompletableFuture.supplyAsync(wrapException(() ->
		{
			var account = LeagueOfLegends.fetchAccount(apiKeyService, riotId, platform);
			
			if(account.hasNonNull("status"))
			{
				throw new RuntimeException("Could not find user " + riotId);
			}
			
			var puuid = account.path("puuid").asText();
			var activeMatch = LeagueOfLegends.fetchActiveMatch(apiKeyService, puuid, platform);
			
			if(activeMatch.hasNonNull("status") || activeMatch.isTextual())
			{
				throw new RuntimeException(riotId + " is currently not in an active game");
			}
			
			var participants = activeMatch.path("participants");
			var builder = new FormattedStringBuilder();
			var teams = LeagueOfLegendsCommand.groupAndLoad(apiKeyService, participants, championsFuture, platform).entrySet().stream()
				.sorted(Comparator.comparingInt(Entry::getKey))
				.map(Entry::getValue)
				.toList();
			var teamRatings = new int[teams.size()];
			
			for(int x = 0; x < teams.size(); x++)
			{
				var teamMembers = teams.get(x);
				var color = x % 2 == 0 ? BLUE_COLOR : RED_COLOR;
				
				if(x > 0)
				{
					builder.append("\n");
					builder.append("\t".repeat(7));
					builder.append("VS", IStyle.bold());
				}
				
				if(!teamMembers.isEmpty())
				{
					int rankedTeamMembers = 0;
					
					for(CompletableFuture<SummonerOverview> participant : teamMembers)
					{
						var overview = participant.join();
						builder.append("\n");
						builder.append(overview.champion(), color);
						
						if(puuid.equals(overview.puuid()))
						{
							builder.append(" ");
							builder.append(overview.riotId(), IStyle.underlined());
						}
						else
						{
							builder.append(" ");
							builder.append(overview.riotId());
						}
						
						var league = overview.highestRank();
						
						if(league.isPresent())
						{
							teamRatings[x] += league.get().getRating();
							rankedTeamMembers++;
						}
						
						builder.append(" | ");
						builder.append(league.map(League::toString).orElse("Unranked"));
						builder.append(" | ");
						builder.append(formatMastery(overview.mastery()));
					}
					
					teamRatings[x] = Math.round((float) teamRatings[x] / (float) rankedTeamMembers);
				}
				else
				{
					builder.append("\nNone");
				}
			}
			
			var ranks = Arrays.stream(teamRatings)
				.mapToObj(LeagueOfLegendsCommand::ratingToLeague)
				.map(league -> league.map(League::toString).orElse("Unranked"))
				.collect(Collectors.joining(" vs "));
			var gameLength = activeMatch.path("gameLength").asLong();
			var formattedGameLength = gameLength < 0 ? "Loading Screen" : Util.formatDuration(gameLength);
			var queue = LeagueOfLegendsCommand.formatQueue(queuesFuture.join(), activeMatch.path("gameQueueConfigId").asInt());
			
			builder.append("\n");
			builder.append(queue);
			builder.append(" | ");
			builder.append(formattedGameLength);
			builder.append(" | Average Ranks: ");
			builder.append(ranks);
			
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
	
	private static CompletableFuture<SummonerOverview> loadParticipantAsync(IAPIKeyService apiKeyService, JsonNode participant, CompletableFuture<JsonNode> championsFuture, Platform platform)
	{
		var championId = participant.path("championId").asLong();
		var championFuture = championsFuture.thenApply(champions -> LeagueOfLegendsCommand.championById(championId, champions));
		
		if(!participant.path("bot").asBoolean())
		{
			var summonerId = Util.urlEncode(participant.path("summonerId").asText());
			var puuid = participant.path("puuid").asText();
			var league = CompletableFuture.supplyAsync(wrapException(() -> LeagueOfLegends.fetchLeague(apiKeyService, summonerId, platform)));
			var mastery = CompletableFuture.supplyAsync(wrapException(() -> LeagueOfLegends.fetchChampionMastery(apiKeyService, puuid, championId, platform)));
			var accountFuture = CompletableFuture.supplyAsync(wrapException(() -> LeagueOfLegends.fetchAccount(apiKeyService, puuid, platform)));
			return CompletableFuture.allOf(championFuture, league, mastery, accountFuture).thenApply(__ ->
			{
				var account = accountFuture.join();
				var riotId = new RiotId(account.path("gameName").asText(), account.path("tagLine").asText());
				return new SummonerOverview(riotId, puuid, championFuture.join(), league.join(), mastery.join());
			});
		}
		
		return championFuture.thenApply(champion -> new SummonerOverview(new RiotId("Bot " + champion), null, champion, null, null));
	}
	
	private record SummonerOverview(RiotId riotId, String puuid, String champion, JsonNode league, JsonNode mastery)
	{
		public Optional<League> highestRank()
		{
			if(this.league != null)
			{
				return LeagueOfLegendsCommand.highestRank(this.league);
			}
			
			return Optional.empty();
		}
	}
	
	private static int history(CommandContext<CommandSource> context, IAPIKeyService apiKeyService, RiotId riotId, Platform platform) throws CommandSyntaxException
	{
		var queuesFuture = CompletableFuture.supplyAsync(wrapException(LeagueOfLegends::fetchQueues));
		var championsFuture = CompletableFuture.supplyAsync(wrapException(() -> LeagueOfLegends.fetchChampions()));
		var history = CompletableFuture.supplyAsync(wrapException(() ->
		{
			var account = LeagueOfLegends.fetchAccount(apiKeyService, riotId, platform);
			
			if(account.hasNonNull("status"))
			{
				throw new RuntimeException("Could not find user " + riotId);
			}
			
			return account.path("puuid").asText();
		})).thenApplyAsync(wrapException(puuid ->
		{
			var matchIds = LeagueOfLegends.fetchMatchHistory(apiKeyService, puuid, platform.getRegion(), 0, 20);
			
			if(matchIds.isEmpty())
			{
				throw new RuntimeException(riotId + " has not played any games yet");
			}
			
			var matches = new ArrayList<CompletableFuture<JsonNode>>(matchIds.size());
			
			for(JsonNode matchId : matchIds)
			{
				matches.add(CompletableFuture.supplyAsync(wrapException(() -> LeagueOfLegends.fetchMatch(apiKeyService, matchId.asText(), platform.getRegion()))));
			}
			
			var calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			
			var today = calendar.getTime();
			var builder = new FormattedStringBuilder();
			var champions = championsFuture.join();
			var queues = queuesFuture.join();
			var stats = new HistoryStats();
			
			builder.append("\nMatch history for ");
			builder.append(riotId);
			builder.append(":");
			
			for(int x = 0; x < matches.size(); x++)
			{
				var info = matches.get(x).join().path("info");
				var participant = LeagueOfLegendsCommand.findParticipant(info.path("participants"), puuid);
				var gameStart = info.path("gameStartTimestamp").asLong();
				var date = new Date(gameStart);
				var champion = LeagueOfLegendsCommand.championById(participant.path("championId").asLong(), champions);
				var queue = LeagueOfLegendsCommand.formatQueue(queues, info.path("queueId").asInt());
				var playTime = (info.has("gameEndTimestamp") ? (info.path("gameEndTimestamp").asLong() - gameStart) : info.path("gameDuration").asLong()) / 1000L;
				var kills = participant.path("kills").asInt();
				var deaths = participant.path("deaths").asInt();
				var assists = participant.path("assists").asInt();
				var winner = participant.path("win").asBoolean();
				var formattedDate = DATE_FORMAT.format(date);
				var formattedPlayTime = Util.formatDuration(playTime);
				var color = winner ? WIN_COLOR : LOSS_COLOR;
				
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
				
				builder.append("\n[");
				builder.append(x + 1, color);
				builder.append("] ");
				builder.append(formattedPlayTime);
				builder.append(" | ");
				builder.append(formattedDate);
				builder.append(" | ");
				builder.append(queue);
				builder.append(" | ");
				builder.append(kills);
				builder.append("/");
				builder.append(deaths);
				builder.append("/");
				builder.append(assists);
				builder.append(" | ");
				builder.append(champion);
			}
			
			var matchCount = (double) matches.size();
			var winrate = Math.round(stats.getWins() * 100 / matchCount);
			var kills = String.format(Locale.ENGLISH, "%.01f", stats.getKills() / matchCount);
			var deaths = String.format(Locale.ENGLISH, "%.01f", stats.getDeaths() / matchCount);
			var assists = String.format(Locale.ENGLISH, "%.01f", stats.getAssists() / matchCount);
			var averageMatchLength = Util.formatDuration((long) (stats.getPlaytime() / matchCount));
			var playTimeToday = Util.formatDuration(stats.getPlaytimeToday());
			
			builder.append("\nWin rate: ");
			builder.append(winrate);
			builder.append("%");
			builder.append(" | Average KDA: ");
			builder.append(kills);
			builder.append("/");
			builder.append(deaths);
			builder.append("/");
			builder.append(assists);
			builder.append(" | Average Match Length: ");
			builder.append(averageMatchLength);
			builder.append(" | Playtime Today: ");
			builder.append(playTimeToday);
			
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
		var future = CompletableFuture.supplyAsync(wrapException(() ->
		{
			var normal = LeagueOfLegendsCommand.normalizeChampionName(champion);
			var version = LeagueOfLegends.fetchVersion();
			var champions = LeagueOfLegends.fetchChampions(version);
			
			for(JsonNode node : champions.path("data"))
			{
				if(LeagueOfLegendsCommand.normalizeChampionName(node.path("name").asText()).equals(normal))
				{
					var id = node.path("id").asText();
					var champ = LeagueOfLegends.fetchChampion(version, id);
					var data = champ.path("data").path(id);
					var name = data.path("name").asText();
					var title = data.path("title").asText();
					var lore = data.path("lore").asText();
					var result = "\n" + name + " " + title + "\n" + lore;
					context.getSource().sendFeedback(result);
					return data.path("key").asInt();
				}
			}
			
			throw new RuntimeException("Could not find champion " + champion);
		}));
		
		try
		{
			return future.join();
		}
		catch(CancellationException | CompletionException e)
		{
			throw ERROR_FETCHING_DATA.create(e);
		}
	}
	
	private static int alias(CommandContext<CommandSource> context, IUserConfigService userConfigService, String alias)
	{
		IClient client = context.getSource().getClient();
		UserConfig config = userConfigService.getUserConfig(client.getId());
		config.setLeagueOfLegendsAlias(alias);
		userConfigService.saveUserConfig(client.getId(), config);
		return 1;
	}
	
	private static String normalizeChampionName(String champion)
	{
		if(champion == null)
		{
			return null;
		}
		
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
			.map(LeagueOfLegendsCommand::rankFromLeague)
			.filter(Objects::nonNull)
			.sorted()
			.findFirst();
	}
	
	private static League rankFromLeague(JsonNode league)
	{
		try
		{
			return OBJECT_MAPPER.treeToValue(league, League.class);
		}
		catch(Throwable e)
		{
			return null;
		}
	}
	
	public static Optional<League> ratingToLeague(int rating)
	{
		if(rating <= 0 || rating > MAX_RATING)
		{
			return Optional.empty();
		}
		
		return Optional.of(LeagueOfLegendsCommand.leagueFromRating(Tier.LOWEST, Rank.LOWEST, rating));
	}
	
	private static League leagueFromRating(Tier tier, Rank rank, int rating)
	{
		if(rating == 1)
		{
			return new League(tier, rank);
		}
		
		if(!tier.isApexTier())
		{
			if(rating - 1 < Rank.VALUES.length)
			{
				return new League(tier, Rank.VALUES[rating - 1]);
			}
			else
			{
				return LeagueOfLegendsCommand.leagueFromRating(tier.next(), Rank.LOWEST, rating - 4);
			}
		}
		
		return LeagueOfLegendsCommand.leagueFromRating(tier.next(), null, rating - 1);
	}
	
	private static String formatQueue(JsonNode queues, int queueId)
	{
		for(int x = 0; x < queues.size(); x++)
		{
			JsonNode queue = queues.get(x);
			
			if(queue.path("queueId").asInt() == queueId)
			{
				return queue.path("description").asText("Custom")
					.replaceFirst("^[0-9]v[0-9] ", "")
					.replaceFirst(" games$", "");
			}
		}
		
		return "Unknown Queue";
	}
	
	private static String formatMastery(JsonNode mastery)
	{
		if(mastery != null)
		{
			return mastery.path("championPoints").asInt() + " (" + mastery.path("championLevel").asInt() + ")";
		}
		
		return "0 (1)";
	}
	
	private static Map<Integer, List<CompletableFuture<SummonerOverview>>> groupAndLoad(IAPIKeyService apiKeyService, JsonNode participants, CompletableFuture<JsonNode> champions, Platform platform)
	{
		Map<Integer, List<CompletableFuture<SummonerOverview>>> map = new HashMap<Integer, List<CompletableFuture<SummonerOverview>>>();
		
		for(int x = 0; x < participants.size(); x++)
		{
			JsonNode participant = participants.get(x);
			List<CompletableFuture<SummonerOverview>> team = map.computeIfAbsent(participant.path("teamId").asInt(), key -> new ArrayList<CompletableFuture<SummonerOverview>>());
			team.add(LeagueOfLegendsCommand.loadParticipantAsync(apiKeyService, participant, champions, platform));
		}
		
		return map;
	}
	
	private static RiotId riotIdForUser(CommandContext<CommandSource> context, Platform platform, IUserConfigService userConfigService)
	{
		IClient client = context.getSource().getClient();
		UserConfig config = userConfigService.getUserConfig(client.getId());
		
		if(config.getLeagueOfLegendsAlias() != null)
		{
			return RiotId.parse(config.getLeagueOfLegendsAlias(), platform);
		}
		
		return RiotId.parse(client.getName(), platform);
	}
	
	private static String createBuildResponse(JsonNode participant, JsonNode player, JsonNode items)
	{
		List<Integer> itemIds = new ArrayList<Integer>(7);
		
		for(int x = 0; x < 7; x++)
		{
			int itemId = participant.path("item" + x).asInt(0);
			
			if(itemId > 0)
			{
				itemIds.add(itemId);
			}
		}
		
		String name = participant.path("summonerName").asText();
		
		if(itemIds.isEmpty())
		{
			return name + " bought no items";
		}
		
		StringBuilder response = new StringBuilder("Build for ");
		response.append(participant.path("championName").asText());
		response.append(" by ");
		response.append(name);
		response.append(" who had a score of ");
		response.append(participant.path("kills").asInt());
		response.append("/");
		response.append(participant.path("deaths").asInt());
		response.append("/");
		response.append(participant.path("assists").asInt());
		response.append(" where ");
		response.append(player.path("summonerName").asText());
		response.append(" played as ");
		response.append(player.path("championName").asText());
		response.append(":");
		
		var data = items.path("data");
		
		for(int x = 0; x < itemIds.size(); x++)
		{
			response.append("\n");
			response.append(x + 1);
			response.append(". ");
			response.append(data.path(itemIds.get(x).toString()).path("name").asText());
		}
		
		return response.toString();
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
	private interface ThrowableSupplier<T>
	{
		T get() throws Throwable;
	}
	
	@FunctionalInterface
	private interface ThrowableFunction<T, R>
	{
		R apply(T t) throws Throwable;
	}
	
	private enum BuildSelector
	{
		MINE,
		ENEMY,
		ALLY;
	}
}
