package net.kardexo.bot.adapters.web;

public interface IURLProcessor
{
	String process(String url);
	
	boolean isApplicable(String message);
}
