package net.kardexo.bot.services.api;

import net.kardexo.bot.domain.ChatHistory;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.domain.api.MessageTarget;

public interface IMessageService
{
	void onMessage(IClient client, String message, MessageTarget target, ChatHistory chatHistory);
}
