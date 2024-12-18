package net.kardexo.bot.services.api;

import net.kardexo.bot.domain.ChatHistory;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.api.IChannel;
import net.kardexo.bot.domain.api.IClient;

public interface IMessageProcessor
{
	void process(IBotClient bot, IChannel channel, IClient client, String message, ChatHistory chatHistory);
	
	boolean isApplicable(IBotClient bot, IChannel channel, IClient client, String message, ChatHistory chatHistory);
}
