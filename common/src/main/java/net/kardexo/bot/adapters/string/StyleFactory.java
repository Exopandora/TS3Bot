package net.kardexo.bot.adapters.string;

import net.kardexo.bot.domain.api.IStyle;

public interface StyleFactory
{
	IStyle color(int color);
	
	IStyle bold();
	
	IStyle underlined();
	
	IStyle italic();
	
	IStyle strikethrough();
}
