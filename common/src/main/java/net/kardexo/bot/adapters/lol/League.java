package net.kardexo.bot.adapters.lol;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class League implements Comparable<League>
{
	@JsonProperty("leagueId")
	private UUID leagueId;
	@JsonProperty("queueType")
	private QueueType queueType;
	@JsonProperty("tier")
	private Tier tier;
	@JsonProperty("rank")
	private Rank rank;
	@JsonProperty("summonerId")
	private String summonerId;
	@JsonProperty("summonerName")
	private String summonerName;
	@JsonProperty("leaguePoints")
	private int leaguePoints;
	@JsonProperty("wins")
	private int wins;
	@JsonProperty("losses")
	private int losses;
	@JsonProperty("veteran")
	private boolean veteran;
	@JsonProperty("inactive")
	private boolean inactive;
	@JsonProperty("freshBlood")
	private boolean freshBlood;
	@JsonProperty("hotStreak")
	private boolean hotStreak;
	
	public League()
	{
		super();
	}
	
	public League(Tier tier, Rank rank)
	{
		this.tier = tier;
		this.rank = rank;
	}
	
	public UUID getLeagueId()
	{
		return this.leagueId;
	}
	
	public QueueType getQueueType()
	{
		return this.queueType;
	}
	
	public Tier getTier()
	{
		return this.tier;
	}
	
	public Rank getRank()
	{
		return this.rank;
	}
	
	public String getSummonerId()
	{
		return this.summonerId;
	}
	
	public String getSummonerName()
	{
		return this.summonerName;
	}
	
	public int getLeaguePoints()
	{
		return this.leaguePoints;
	}
	
	public int getWins()
	{
		return this.wins;
	}
	
	public int getLosses()
	{
		return this.losses;
	}
	
	public boolean isVeteran()
	{
		return this.veteran;
	}
	
	public boolean isInactive()
	{
		return this.inactive;
	}
	
	public boolean isFreshBlood()
	{
		return this.freshBlood;
	}
	
	public boolean isHotStreak()
	{
		return this.hotStreak;
	}
	
	public int getRating()
	{
		return this.tier.rating(this.rank);
	}
	
	@Override
	public int compareTo(@NotNull League league)
	{
		if(this.queueType != null && league.getQueueType() != null)
		{
			return this.queueType.compareTo(league.getQueueType());
		}
		
		return 0;
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(this.tier.getName());
		
		if(this.rank != null && !this.tier.isApexTier())
		{
			builder.append(" ");
			builder.append(this.rank);
		}
		
		return builder.toString();
	}
}
