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
	@JsonProperty("login_name")
	private String loginName;
	@JsonProperty("login_password")
	private String loginPassword;
	@JsonProperty("channel_name")
	private String channelName;
	
	@JsonProperty("virtual_server_id")
	private int virtualServerId;
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
	
	public String getLoginName()
	{
		return this.loginName;
	}
	
	public String getLoginPassword()
	{
		return this.loginPassword;
	}
	
	public String getChannelName()
	{
		return this.channelName;
	}
	
	public int getVirtualServerId()
	{
		return this.virtualServerId;
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
