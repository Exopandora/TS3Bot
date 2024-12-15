package net.kardexo.bot.adapters.lol;

public enum Tier
{
	IRON("Iron", false),
	BRONZE("Bronze", false),
	SILVER("Silver", false),
	GOLD("Gold", false),
	PLATINUM("Platinum", false),
	EMERALD("Emerald", false),
	DIAMOND("Diamond", false),
	MASTER("Master", true),
	GRANDMASTER("Grandmaster", true),
	CHALLENGER("Challenger", true);
	
	public static final Tier HIGHEST = Tier.CHALLENGER;
	public static final Tier LOWEST = Tier.IRON;
	public static final Tier[] VALUES = Tier.values();
	
	private final String name;
	private final boolean isApexTier;
	
	private Tier(String name, boolean isApexTier)
	{
		this.name = name;
		this.isApexTier = isApexTier;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public boolean isApexTier()
	{
		return this.isApexTier;
	}
	
	public int rating(Rank rank)
	{
		return (this.ordinal() > 0 ? VALUES[this.ordinal() - 1].rating(Rank.I) : 0) + (this.isApexTier ? 1 : rank.getRating());
	}
	
	public Tier next()
	{
		return VALUES[(this.ordinal() + 1) % VALUES.length];
	}
}
