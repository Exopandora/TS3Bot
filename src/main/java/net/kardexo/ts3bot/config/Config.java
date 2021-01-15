package net.kardexo.ts3bot.config;

import java.io.File;
import java.util.List;
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
	
	@JsonProperty("api_keys")
	private Map<String, APIKey> apiKeys;
	
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
	
	public Map<String, APIKey> getApiKeys()
	{
		return this.apiKeys;
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
	
	public static class APIKey
	{
		@JsonProperty("tokens")
		private JsonNode tokens;
		@JsonProperty("limits")
		private List<Limit> limits;
		
		public APIKey()
		{
			super();
		}
		
		public APIKey(JsonNode tokens, List<Limit> limits)
		{
			this.tokens = tokens;
			this.limits = limits;
		}
		
		public JsonNode getTokens()
		{
			return this.tokens;
		}
		
		public List<Limit> getLimits()
		{
			return this.limits;
		}
		
		public static class Limit
		{
			@JsonProperty("limit")
			private int limit;
			@JsonProperty("duration")
			private long duration;
			
			public Limit()
			{
				super();
			}
			
			public Limit(int limit, long duration)
			{
				this.limit = limit;
				this.duration = duration;
			}
			
			public int getLimit()
			{
				return this.limit;
			}
			
			public long getDuration()
			{
				return this.duration;
			}
		}
	}
}
