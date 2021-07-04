package net.kardexo.ts3bot.message.url;

public interface IURLProcessor
{
	String process(String url);
	
	boolean isApplicable(String message);
}
