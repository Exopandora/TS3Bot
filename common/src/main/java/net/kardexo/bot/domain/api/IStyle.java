package net.kardexo.bot.domain.api;

import net.kardexo.bot.services.api.StyleFactory;

import java.util.ServiceLoader;

public interface IStyle
{
	StyleFactory INSTANCE = ServiceLoader.load(StyleFactory.class).findFirst().orElseThrow();
	
	default String apply(String string)
	{
		return this.apply(new StringBuilder(), string).toString();
	}
	
	StringBuilder apply(StringBuilder builder, String string);
	
	static IStyle color(String hex)
	{
		return color(Integer.parseInt(hex, 16));
	}
	
	static IStyle color(int color)
	{
		return INSTANCE.color(color);
	}
	
	static IStyle bold()
	{
		return INSTANCE.bold();
	}
	
	static IStyle underlined()
	{
		return INSTANCE.underlined();
	}
	
	static IStyle italic()
	{
		return INSTANCE.italic();
	}
	
	static IStyle strikethrough()
	{
		return INSTANCE.strikethrough();
	}
	
	static int colorOverhead()
	{
		return INSTANCE.colorOverhead();
	}
}
