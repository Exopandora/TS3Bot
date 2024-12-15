package net.kardexo.bot.domain.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public class APIKeyConfig
{
	@JsonProperty("tokens")
	private JsonNode tokens;
	@JsonProperty("limits")
	private List<Limit> limits;
	
	public APIKeyConfig()
	{
		super();
	}
	
	public APIKeyConfig(JsonNode tokens, List<Limit> limits)
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
