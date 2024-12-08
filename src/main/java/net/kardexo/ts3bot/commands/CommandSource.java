package net.kardexo.ts3bot.commands;

import com.github.theholywaffle.teamspeak3.api.TextMessageTargetMode;
import com.github.theholywaffle.teamspeak3.api.wrapper.ClientInfo;
import net.kardexo.ts3bot.TS3Bot;

public class CommandSource
{
	private final ClientInfo clientInfo;
	private final TextMessageTargetMode target;
	private final TS3Bot bot;
	
	public CommandSource(TS3Bot bot, ClientInfo clientInfo, TextMessageTargetMode target)
	{
		this.clientInfo = clientInfo;
		this.target = target;
		this.bot = bot;
	}
	
	public ClientInfo getClientInfo()
	{
		return this.clientInfo;
	}
	
	public TextMessageTargetMode getTarget()
	{
		return this.target;
	}
	
	public void sendFeedback(String message)
	{
		if(this.getClientInfo().getId() == TS3Bot.getInstance().getId())
		{
			TS3Bot.LOGGER.info(message);
		}
		else
		{
			this.bot.getApi().sendTextMessage(this.target, this.getClientInfo().getId(), message);
		}
	}
	
	public void sendPrivateMessage(String message)
	{
		if(this.getClientInfo().getId() == TS3Bot.getInstance().getId())
		{
			TS3Bot.LOGGER.info(message);
		}
		else
		{
			this.bot.getApi().sendPrivateMessage(this.getClientInfo().getId(), message);
		}
	}
	
	public boolean hasPermission(String permission)
	{
		return this.bot.hasPermission(this.getClientInfo(), permission);
	}
}
