package net.kardexo.bot.adapters.lol;

public record RiotId(String name, String tagLine)
{
	public RiotId(String name)
	{
		this(name, null);
	}
	
	@Override
	public String toString()
	{
		String result = this.name;
		
		if(this.tagLine != null && !this.tagLine.isBlank())
		{
			result += "#" + this.tagLine;
		}
		
		return result;
	}
	
	public static RiotId parse(String riotId, Platform defaultPlatform)
	{
		int index = riotId.lastIndexOf('#');
		
		if(index > 0 && riotId.length() - index - 1 <= 5 && index + 2 < riotId.length())
		{
			String tagLine = riotId.substring(index + 1);
			
			if(tagLine.matches("[0-9a-zA-Z]{3,5}") || Platform.fromTagLine(tagLine) != null)
			{
				return new RiotId(riotId.substring(0, index), tagLine);
			}
		}
		
		return new RiotId(riotId, defaultPlatform.getDefaultTagLine());
	}
}
