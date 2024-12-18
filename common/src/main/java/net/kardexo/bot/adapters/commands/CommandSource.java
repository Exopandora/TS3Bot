package net.kardexo.bot.adapters.commands;

import net.kardexo.bot.domain.ChatHistory;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.api.IChannel;
import net.kardexo.bot.domain.api.IClient;

import java.util.Random;

public class CommandSource
{
	private final IBotClient bot;
	private final IChannel channel;
	private final IClient client;
	private final Random random;
	private final ChatHistory chatHistory;
	
	public CommandSource(IBotClient bot, IChannel channel, IClient client, ChatHistory chatHistory, Random random)
	{
		this.bot = bot;
		this.channel = channel;
		this.client = client;
		this.chatHistory = chatHistory;
		this.random = random;
	}
	
	public void sendFeedback(String message)
	{
		this.bot.sendMessage(this.channel, message);
	}
	
	public void sendPrivateMessage(String message)
	{
		this.bot.sendPrivateMessage(this.client.getPrivateChannel(), message);
	}
	
	public IBotClient getBot()
	{
		return this.bot;
	}
	
	public IChannel getChannel()
	{
		return this.channel;
	}
	
	public IClient getClient()
	{
		return this.client;
	}
	
	public ChatHistory getChatHistory()
	{
		return this.chatHistory;
	}
	
	public Random getRandomSource()
	{
		return this.random;
	}
}
