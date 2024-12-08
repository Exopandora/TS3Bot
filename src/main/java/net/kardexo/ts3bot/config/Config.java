package net.kardexo.ts3bot.config;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import net.kardexo.ts3bot.api.LeagueOfLegends.Platform;

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
	@JsonProperty("default_watch2gether_bg_color")
	private String defaultW2GBGColor;
	@JsonProperty("default_watch2gether_bg_opacity")
	private int defaultW2GBGOpacity;
	@JsonProperty("league_of_legends_region")
	private Platform lolRegion;
	
	@JsonProperty("rules")
	private List<String> rules;
	
	@JsonProperty("bingo_items")
	private List<JsonNode> bingoItems;
	@JsonProperty("bingo_ticket_size")
	private int bingoTicketSize;
	
	@JsonProperty("shortcuts")
	private Shortcuts shortcuts;
	
	@JsonProperty("login_bonus")
	private int loginBonus;
	
	@JsonProperty("currency")
	private String currency;
	
	@JsonProperty("min_video_duration_youtube")
	private int minVideoDurationYouTube;
	@JsonProperty("max_video_duration_youtube")
	private int maxVideoDurationYouTube;
	
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
	
	public String getDefaultW2GBGColor()
	{
		return this.defaultW2GBGColor;
	}
	
	public int getDefaultW2GBGOpacity()
	{
		return this.defaultW2GBGOpacity;
	}
	
	public Platform getLoLPlatform()
	{
		return this.lolRegion;
	}
	
	public void setLoLRegion(Platform lolRegion)
	{
		this.lolRegion = lolRegion;
	}
	
	public List<String> getRules()
	{
		return this.rules;
	}
	
	public List<JsonNode> getBingoItems()
	{
		return this.bingoItems;
	}
	
	public int getBingoTicketSize()
	{
		return this.bingoTicketSize;
	}
	
	public Shortcuts getShortcuts()
	{
		return this.shortcuts;
	}
	
	public int getLoginBonus()
	{
		return this.loginBonus;
	}
	
	public String getCurrency()
	{
		return this.currency;
	}
	
	public int getMinVideoDurationYouTube()
	{
		return this.minVideoDurationYouTube;
	}
	
	public int getMaxVideoDurationYouTube()
	{
		return this.maxVideoDurationYouTube;
	}
	
	public static class Shortcuts
	{
		@JsonProperty("text")
		private Map<String, JsonNode> text;
		@JsonProperty("youtube")
		private Map<String, String> youtube;
		@JsonProperty("twitch")
		private Map<String, String> twitch;
		
		public Shortcuts()
		{
			super();
		}
		
		public Shortcuts(Map<String, JsonNode> text, Map<String, String> youtube, Map<String, String> twitch)
		{
			this.text = text;
			this.youtube = youtube;
			this.twitch = twitch;
		}
		
		public Map<String, JsonNode> getText()
		{
			return this.text;
		}
		
		public Map<String, String> getYoutube()
		{
			return this.youtube;
		}
		
		public Map<String, String> getTwitch()
		{
			return this.twitch;
		}
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
