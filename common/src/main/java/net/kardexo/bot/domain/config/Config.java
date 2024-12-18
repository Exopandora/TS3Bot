package net.kardexo.bot.domain.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import net.kardexo.bot.adapters.lol.Platform;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Config
{
	@JsonProperty("virtual_server_id")
	private int virtualServerId;
	@JsonProperty("chat_history_size")
	private int chatHistorySize;
	
	@JsonProperty("api_keys")
	private Map<String, APIKeyConfig> apiKeys;
	
	@JsonProperty("permissions")
	private Map<String, Set<String>> permissions;
	
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
	private ShortcutsConfig shortcuts;
	
	@JsonProperty("login_bonus")
	private int loginBonus;
	
	@JsonProperty("currency")
	private String currency;
	
	@JsonProperty("min_video_duration_youtube")
	private int minVideoDurationYouTube;
	@JsonProperty("max_video_duration_youtube")
	private int maxVideoDurationYouTube;
	
	protected Config()
	{
		super();
	}
	
	public int getVirtualServerId()
	{
		return this.virtualServerId;
	}
	
	public int getChatHistorySize()
	{
		return this.chatHistorySize;
	}
	
	public Map<String, APIKeyConfig> getApiKeys()
	{
		return this.apiKeys;
	}
	
	public Map<String, Set<String>> getPermissions()
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
	
	public ShortcutsConfig getShortcuts()
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
	
	@JsonIgnore
	public abstract boolean isEmbedsEnabled();
}
