package net.kardexo.bot.adapters.lol;

import com.fasterxml.jackson.databind.JsonNode;
import net.kardexo.bot.domain.Util;
import net.kardexo.bot.services.api.IAPIKeyService;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import static net.kardexo.bot.domain.Util.OBJECT_MAPPER;

public class LeagueOfLegends
{
	public static final String API_URL = "https://%s.api.riotgames.com/";
	public static final URI DDRAGON_API_URL = URI.create("https://ddragon.leagueoflegends.com/");
	public static final URI STATIC_DOC_API_URL = URI.create("http://static.developer.riotgames.com/docs/");
	
	public static String fetchVersion() throws IOException
	{
		URI uri = DDRAGON_API_URL
			.resolve("api/versions.json");
		return fetch(uri).get(0).asText();
	}
	
	public static JsonNode fetchQueues() throws IOException
	{
		URI uri = STATIC_DOC_API_URL
			.resolve("lol/queues.json");
		return fetch(uri);
	}
	
	public static JsonNode fetchChampions() throws IOException
	{
		return fetchChampions(fetchVersion());
	}
	
	public static JsonNode fetchChampions(String version) throws IOException
	{
		URI uri = DDRAGON_API_URL
			.resolve("cdn/" + version + "/data/en_US/champion.json");
		return fetch(uri);
	}
	
	public static JsonNode fetchChampion(String version, String champion) throws IOException
	{
		URI uri = DDRAGON_API_URL
			.resolve("cdn/" + version + "/data/en_US/champion/" + champion + ".json");
		return fetch(uri);
	}
	
	public static JsonNode fetchItems() throws IOException
	{
		return fetchItems(fetchVersion());
	}
	
	public static JsonNode fetchItems(String version) throws IOException
	{
		URI uri = DDRAGON_API_URL
			.resolve("cdn/" + version + "/data/en_US/item.json");
		return fetch(uri);
	}
	
	public static JsonNode fetchMatchHistory(IAPIKeyService apiKeyService, String puuid, Region region, int start, int count) throws URISyntaxException, IOException
	{
		URI uri = new URIBuilder(region.getApiUrl().resolve("lol/match/v5/matches/by-puuid/" + puuid + "/ids"))
			.addParameter("start", String.valueOf(start))
			.addParameter("count", String.valueOf(count))
			.build();
		return fetch(apiKeyService, uri);
	}
	
	public static JsonNode fetchMatch(IAPIKeyService apiKeyService, String matchId, Region region) throws IOException
	{
		URI uri = region.getApiUrl()
			.resolve("lol/match/v5/matches/" + matchId);
		return fetch(apiKeyService, uri);
	}
	
	public static JsonNode fetchChampionMastery(IAPIKeyService apiKeyService, String puuid, long championId, Platform platform) throws IOException
	{
		URI uri = platform.getApiUrl()
			.resolve("lol/champion-mastery/v4/champion-masteries/by-puuid/" + puuid + "/by-champion/" + championId);
		return fetch(apiKeyService, uri);
	}
	
	public static JsonNode fetchAccount(IAPIKeyService apiKeyService, RiotId riotId, Platform platform) throws IOException
	{
		URI uri = platform.getRegion().getApiUrl()
			.resolve("riot/account/v1/accounts/by-riot-id/" + Util.urlEncode(riotId.name()) + "/" + Util.urlEncode(riotId.tagLine()));
		return fetch(apiKeyService, uri);
	}
	
	public static JsonNode fetchAccount(IAPIKeyService apiKeyService, String puuid, Platform platform) throws IOException
	{
		URI uri = platform.getRegion().getApiUrl()
			.resolve("riot/account/v1/accounts/by-puuid/" + Util.urlEncode(puuid));
		return fetch(apiKeyService, uri);
	}
	
	public static JsonNode fetchLeague(IAPIKeyService apiKeyService, String puuid, Platform platform) throws IOException
	{
		URI uri = platform.getApiUrl()
			.resolve("lol/league/v4/entries/by-puuid/" + puuid);
		return fetch(apiKeyService, uri);
	}
	
	public static JsonNode fetchActiveMatch(IAPIKeyService apiKeyService, String puuid, Platform platform) throws IOException
	{
		URI uri = platform.getApiUrl()
			.resolve("lol/spectator/v5/active-games/by-summoner/" + Util.urlEncode(puuid));
		return fetch(apiKeyService, uri);
	}
	
	private static JsonNode fetch(URI uri) throws IOException
	{
		return fetch(null, uri);
	}
	
	private static JsonNode fetch(@Nullable IAPIKeyService apiKeyService, URI uri) throws IOException
	{
		try(CloseableHttpClient client = Util.httpClient())
		{
			HttpGet httpGet = new HttpGet(uri);
			httpGet.addHeader("User-Agent", Util.USER_AGENT);
			httpGet.addHeader("Accept-Charset", StandardCharsets.UTF_8.toString());
			
			if(apiKeyService != null)
			{
				httpGet.addHeader("X-Riot-Token", apiKeyService.requestKey(IAPIKeyService.API_KEY_LEAGUE_OF_LEGENDS));
			}
			
			try(CloseableHttpResponse response = client.execute(httpGet))
			{
				return OBJECT_MAPPER.readTree(EntityUtils.toString(response.getEntity()));
			}
		}
	}
}
