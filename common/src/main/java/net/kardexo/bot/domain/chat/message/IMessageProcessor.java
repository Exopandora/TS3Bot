package net.kardexo.bot.domain.chat.message;

import net.kardexo.bot.domain.channel.IChannel;
import net.kardexo.bot.domain.chat.ChatHistory;
import net.kardexo.bot.domain.client.IBotClient;
import net.kardexo.bot.domain.client.IClient;

public interface IMessageProcessor {
	void process(IBotClient bot, IChannel channel, IClient client, String message, ChatHistory chatHistory);
	
	boolean isApplicable(IBotClient bot, IChannel channel, IClient client, String message, ChatHistory chatHistory);
}
