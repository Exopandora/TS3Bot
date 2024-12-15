package net.kardexo.bot.adapters.commands;

import net.kardexo.bot.domain.ChatHistory;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.domain.api.MessageTarget;

import java.util.Random;

public class CommandSource
{
	private final IBotClient bot;
	private final IClient client;
	private final MessageTarget target;
	private final Random random;
	private final ChatHistory chatHistory;
	
	public CommandSource(IBotClient bot, IClient client, MessageTarget target, ChatHistory chatHistory, Random random)
	{
		this.bot = bot;
		this.client = client;
		this.target = target;
		this.chatHistory = chatHistory;
		this.random = random;
	}
	
	public void sendFeedback(String message)
	{
		this.bot.sendMessage(this.target, this.client, message);
	}
	
	public void sendPrivateMessage(String message)
	{
		this.bot.sendPrivateMessage(this.client, message);
	}
	
	public IBotClient getBot()
	{
		return this.bot;
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
