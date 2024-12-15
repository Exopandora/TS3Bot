package net.kardexo.bot.adapters.lol;

public enum QueueType
{
	RANKED_SOLO_5x5("Ranked Solo/Duo"),
	RANKED_FLEX_SR("Ranked Flex");
	
	private final String name;
	
	private QueueType(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return this.name;
	}
}
