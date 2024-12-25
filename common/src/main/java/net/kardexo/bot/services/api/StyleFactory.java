package net.kardexo.bot.services.api;

import net.kardexo.bot.domain.api.IStyle;

public interface StyleFactory
{
	IStyle color(int color);
	
	IStyle bold();
	
	IStyle underlined();
	
	IStyle italic();
	
	IStyle strikethrough();
	
	int colorOverhead();
}
