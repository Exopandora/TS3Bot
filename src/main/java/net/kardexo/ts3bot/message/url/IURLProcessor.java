package net.kardexo.ts3bot.msgproc.url;

public interface IURLProcessor
{
	String process(String url);
	
	boolean isApplicable(String message);
}
