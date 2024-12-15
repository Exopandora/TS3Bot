package net.kardexo.bot.adapters.lol;

public enum Rank
{
	IV,
	III,
	II,
	I;
	
	public static final Rank HIGHEST = Rank.I;
	public static final Rank LOWEST = Rank.IV;
	public static final Rank[] VALUES = Rank.values();
	
	public int getRating()
	{
		return this.ordinal() + 1;
	}
	
	public Rank next()
	{
		return VALUES[(this.ordinal() + 1) % VALUES.length];
	}
}
