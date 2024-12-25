package net.kardexo.bot.adapters.string;

import net.kardexo.bot.domain.api.IStyle;
import net.kardexo.bot.services.api.StyleFactory;

public class TS3StyleFactory implements StyleFactory
{
	@Override
	public IStyle color(int color)
	{
		return new TS3Style("color", String.format("#%02X%02X%02X", (color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF));
	}
	
	@Override
	public IStyle bold()
	{
		return TS3Style.BOLD;
	}
	
	@Override
	public IStyle underlined()
	{
		return TS3Style.UNDERLINED;
	}
	
	@Override
	public IStyle italic()
	{
		return TS3Style.ITALIC;
	}
	
	@Override
	public IStyle strikethrough()
	{
		return TS3Style.STRIKETHROUGH;
	}
	
	@Override
	public int colorOverhead()
	{
		return 27;
	}
}
