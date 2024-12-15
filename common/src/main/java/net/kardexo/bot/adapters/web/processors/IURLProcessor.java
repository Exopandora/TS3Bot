package net.kardexo.bot.adapters.web.processors;

public interface IURLProcessor
{
	String process(String url);
	
	boolean isApplicable(String message);
}
