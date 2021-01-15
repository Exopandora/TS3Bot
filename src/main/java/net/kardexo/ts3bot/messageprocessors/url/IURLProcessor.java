package net.kardexo.ts3bot.messageprocessors.url;

import net.kardexo.ts3bot.messageprocessors.IMessageProcessor;

public interface IURLProcessor extends IMessageProcessor
{
	boolean isApplicable(String message);
}
