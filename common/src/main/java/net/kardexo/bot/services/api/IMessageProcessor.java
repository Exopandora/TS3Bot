package net.kardexo.bot.services.api;

import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.domain.ChatHistory;
import net.kardexo.bot.domain.api.MessageTarget;

public interface IMessageProcessor
{
	void process(IBotClient bot, String message, IClient client, MessageTarget target, ChatHistory chatHistory);
	
	boolean isApplicable(IBotClient bot, String message, IClient client, MessageTarget target, ChatHistory chatHistory);
}
