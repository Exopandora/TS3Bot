package net.kardexo.ts3bot.config;

import java.io.File;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import net.kardexo.ts3bot.commands.impl.CommandLeagueOfLegends.Region;

public class Config
{
	@JsonProperty("host_address")
	private String hostAddress;
	@JsonProperty("host_port")
	private int hostPort;
	@JsonProperty("identity")
	private String identity;
	@JsonProperty("nickname")
	private String nickname;
	@JsonProperty("server_password")
	private String serverPassword;
	@JsonProperty("channel_name")
	private String channelName;
	@JsonProperty("channel_password")
	private String channelPassword;
	
	@JsonProperty("chat_history_size")
	private int chatHistorySize;
	
	@JsonProperty("api_watch_2_gether")
	private String apiWatch2Gether;
	@JsonProperty("api_twitch_client_id")
	private String apiTwitchClientId;
	@JsonProperty("api_twitch_oauth_token")
	private String apiTwitchOAuthToken;
	@JsonProperty("api_youtube")
	private String apiYouTube;
	@JsonProperty("api_twitter_bearer_token")
	private String apiTwitterBearerToken;
	@JsonProperty("api_league_of_legends")
	private String apiLeagueOfLegends;
	
	@JsonProperty("permissions")
	private JsonNode permissions;
	
	@JsonProperty("default_watch2gether_share")
	private String defaultW2GShare;
	@JsonProperty("league_of_legends_region")
	private Region lolRegion;
	
	@JsonProperty("gameservers")
	private Map<String, File> gameservers;
	
	public Config()
	{
		super();
	}
	
	public String getHostAddress()
	{
		return this.hostAddress;
	}
	
	public int getHostPort()
	{
		return this.hostPort;
	}
	
	public String getIdentity()
	{
		return this.identity;
	}
	
	public String getNickname()
	{
		return this.nickname;
	}
	
	public String getServerPassword()
	{
		return this.serverPassword;
	}
	
	public String getChannelName()
	{
		return this.channelName;
	}
	
	public String getChannelPassword()
	{
		return this.channelPassword;
	}
	
	public int getChatHistorySize()
	{
		return this.chatHistorySize;
	}
	
	public String getApiWatch2Gether()
	{
		return this.apiWatch2Gether;
	}
	
	public String getApiTwitchClientId()
	{
		return this.apiTwitchClientId;
	}
	
	public String getApiTwitchOAuthToken()
	{
		return this.apiTwitchOAuthToken;
	}
	
	public String getApiYouTube()
	{
		return this.apiYouTube;
	}
	
	public String getApiTwitterBearerToken()
	{
		return this.apiTwitterBearerToken;
	}
	
	public String getApiLeagueOfLegends()
	{
		return this.apiLeagueOfLegends;
	}
	
	public JsonNode getPermissions()
	{
		return this.permissions;
	}
	
	public String getDefaultW2GShare()
	{
		return this.defaultW2GShare;
	}
	
	public void setDefaultW2GShare(String defaultW2GShare)
	{
		this.defaultW2GShare = defaultW2GShare;
	}
	
	public Region getLoLRegion()
	{
		return this.lolRegion;
	}
	
	public void setLoLRegion(Region lolRegion)
	{
		this.lolRegion = lolRegion;
	}
	
	public Map<String, File> getGameservers()
	{
		return this.gameservers;
	}
}
