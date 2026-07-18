package net.kardexo.bot.teamspeak.domain.chat;

import net.kardexo.bot.domain.chat.IStyle;
import net.kardexo.bot.domain.chat.IStyleFactory;

public class TeamSpeakStyleFactory implements IStyleFactory
{
	@Override
	public IStyle color(int color)
	{
		return new TeamSpeakStyle("color", String.format("#%02X%02X%02X", (color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF));
	}
	
	@Override
	public IStyle bold()
	{
		return TeamSpeakStyle.BOLD;
	}
	
	@Override
	public IStyle underlined()
	{
		return TeamSpeakStyle.UNDERLINED;
	}
	
	@Override
	public IStyle italic()
	{
		return TeamSpeakStyle.ITALIC;
	}
	
	@Override
	public IStyle strikethrough()
	{
		return TeamSpeakStyle.STRIKETHROUGH;
	}
	
	@Override
	public int colorOverhead()
	{
		return 27;
	}
}
