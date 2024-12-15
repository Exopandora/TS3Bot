package net.kardexo.bot.domain.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserConfig
{
	@JsonProperty("league_of_legends_alias")
	private String leagueOfLegendsAlias = null;
	
	public String getLeagueOfLegendsAlias()
	{
		return this.leagueOfLegendsAlias;
	}
	
	public void setLeagueOfLegendsAlias(String leagueOfLegendsAlias)
	{
		this.leagueOfLegendsAlias = leagueOfLegendsAlias;
	}
}
