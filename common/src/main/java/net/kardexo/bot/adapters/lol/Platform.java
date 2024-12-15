package net.kardexo.bot.adapters.lol;

import java.net.URI;

public enum Platform
{
	BR("br1", Region.AMERICAS, "BR"),
	EUNE("eun1", Region.EUROPE, "EUNE"),
	EUW("euw1", Region.EUROPE, "EUW"),
	JP("jp1", Region.ASIA, "JP"),
	KR("kr", Region.ASIA, "KR"),
	LAN("la1", Region.AMERICAS, "LAN"),
	LAS("la2", Region.AMERICAS, "LAS"),
	NA("na1", Region.AMERICAS, "NA"),
	OCE("oc1", Region.SEA, "OCE"),
	TR("tr1", Region.EUROPE, "TR"),
	RU("ru", Region.EUROPE, "RU"),
	PH("ph2", Region.SEA, "PH"),
	SG("sg2", Region.SEA, "SG"),
	TH("th2", Region.SEA, "TH"),
	TW("tw2", Region.SEA, "TW"),
	VN("vn2", Region.SEA, "VN"),
	PBE("pbe", Region.PBE, "PBE");
	
	private final String id;
	private final String defaultTagLine;
	private final Region regionV5;
	
	private Platform(String id, Region region, String defaultTagLine)
	{
		this.id = id;
		this.regionV5 = region;
		this.defaultTagLine = defaultTagLine;
	}
	
	public String getId()
	{
		return this.id;
	}
	
	public Region getRegion()
	{
		return this.regionV5;
	}
	
	public String getDefaultTagLine()
	{
		return this.defaultTagLine;
	}
	
	public URI getApiUrl()
	{
		return URI.create(String.format(LeagueOfLegends.API_URL, this.id));
	}
	
	@Override
	public String toString()
	{
		return this.name();
	}
	
	public static Platform fromId(String input)
	{
		for(Platform platform : Platform.values())
		{
			if(platform.name().equals(input))
			{
				return platform;
			}
		}
		
		return null;
	}
	
	public static Platform fromTagLine(String tagLine)
	{
		for(Platform platform : Platform.values())
		{
			if(platform.getDefaultTagLine().equals(tagLine))
			{
				return platform;
			}
		}
		
		return null;
	}
}
