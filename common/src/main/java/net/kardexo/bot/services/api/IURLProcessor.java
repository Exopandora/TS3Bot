package net.kardexo.bot.services.api;

public interface IURLProcessor
{
	String process(String url);
	
	boolean isApplicable(String message);
}
