package net.kardexo.bot.domain.chat;

public interface IStyleFactory
{
	IStyle color(int color);
	
	IStyle bold();
	
	IStyle underlined();
	
	IStyle italic();
	
	IStyle strikethrough();
	
	int colorOverhead();
}
