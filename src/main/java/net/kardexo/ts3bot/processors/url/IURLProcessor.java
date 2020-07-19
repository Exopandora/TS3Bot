package net.kardexo.ts3bot.processors.url;

public interface IURLProcessor
{
	String process(String url);
	
	boolean isApplicable(String message);
}
