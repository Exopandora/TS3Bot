package net.kardexo.bot.discord.domain.chat;

import net.kardexo.bot.domain.chat.IStyle;
import net.kardexo.bot.domain.chat.IStyleFactory;

public class DiscordStyleFactory implements IStyleFactory
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
