package net.kardexo.bot.adapters.string;

import net.kardexo.bot.domain.api.IStyle;

public class DiscordStyleFactory implements StyleFactory
{
	@Override
	public IStyle color(int color)
	{
		return DiscordStyle.COLOR;
	}
	
	@Override
	public IStyle bold()
	{
		return DiscordStyle.BOLD;
	}
	
	@Override
	public IStyle underlined()
	{
		return DiscordStyle.UNDERLINED;
	}
	
	@Override
	public IStyle italic()
	{
		return DiscordStyle.ITALIC;
	}
	
	@Override
	public IStyle strikethrough()
	{
		return DiscordStyle.STRIKETHROUGH;
	}
	
	@Override
	public int colorOverhead()
	{
		return 1;
	}
}
