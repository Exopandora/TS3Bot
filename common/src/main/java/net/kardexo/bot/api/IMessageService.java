package net.kardexo.bot.api;

import net.kardexo.bot.domain.channel.IChannel;
import net.kardexo.bot.domain.chat.ChatHistory;
import net.kardexo.bot.domain.client.IClient;

public interface IMessageService
{
	void onMessage(IChannel channel, IClient client, String message, ChatHistory chatHistory);
}
