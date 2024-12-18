package net.kardexo.bot.services.api;

import net.kardexo.bot.domain.ChatHistory;
import net.kardexo.bot.domain.api.IChannel;
import net.kardexo.bot.domain.api.IClient;

public interface IMessageService
{
	void onMessage(IChannel channel, IClient client, String message, ChatHistory chatHistory);
}
