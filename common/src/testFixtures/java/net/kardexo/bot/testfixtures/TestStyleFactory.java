package net.kardexo.bot.testfixtures;

import net.kardexo.bot.domain.chat.IStyle;
import net.kardexo.bot.domain.chat.IStyleFactory;

public class TestStyleFactory implements IStyleFactory
{
	@Override
	public IStyle color(int color)
	{
		return new TestStyle(String.format("#%02X%02X%02X", (color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF));
	}
	
	@Override
	public IStyle bold()
	{
		return TestStyle.BOLD;
	}
	
	@Override
	public IStyle underlined()
	{
		return TestStyle.UNDERLINED;
	}
	
	@Override
	public IStyle italic()
	{
		return TestStyle.ITALIC;
	}
	
	@Override
	public IStyle strikethrough()
	{
		return TestStyle.STRIKETHROUGH;
	}
	
	@Override
	public int colorOverhead()
	{
		return 0;
	}
}
