package net.kardexo.bot.domain.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

public class ShortcutsConfig
{
	@JsonProperty("text")
	private Map<String, JsonNode> text;
	@JsonProperty("youtube")
	private Map<String, String> youtube;
	@JsonProperty("twitch")
	private Map<String, String> twitch;
	
	public ShortcutsConfig()
	{
		super();
	}
	
	public ShortcutsConfig(Map<String, JsonNode> text, Map<String, String> youtube, Map<String, String> twitch)
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
