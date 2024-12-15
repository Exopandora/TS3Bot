package net.kardexo.bot.adapters.lol;

import java.net.URI;

public enum Region
{
	AMERICAS("americas"),
	ASIA("asia"),
	EUROPE("europe"),
	SEA("sea"),
	GARENA("garena"),
	PBE("pbe"),
	ESPORTS("esports");
	
	private final String id;
	
	private Region(String id)
	{
		this.id = id;
	}
	
	public String getId()
	{
		return this.id;
	}
	
	public URI getApiUrl()
	{
		return URI.create(String.format(LeagueOfLegends.API_URL, this.id));
	}
	
	@Override
	public String toString()
	{
		return this.id;
	}
}
