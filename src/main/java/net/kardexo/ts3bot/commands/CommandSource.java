package net.kardexo.ts3bot.commands;

import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.TextMessageTargetMode;
import com.github.theholywaffle.teamspeak3.api.wrapper.ClientInfo;

import net.kardexo.ts3bot.TS3Bot;

public class CommandSource
{
	private final ClientInfo clientInfo;
	private final TextMessageTargetMode target;
	
	public CommandSource(TS3Query query, TS3Api api, ClientInfo clientInfo, TextMessageTargetMode target, List<String> history, int botId)
	{
		this.clientInfo = clientInfo;
		this.target = target;
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
		TS3Bot.getInstance().getApi().sendTextMessage(this.target, this.getClientInfo().getId(), message);
	}
	
	public void sendPrivateMessage(String message)
	{
		TS3Bot.getInstance().getApi().sendPrivateMessage(this.getClientInfo().getId(), message);
	}
	
	public boolean hasPermission(String permission)
	{
		JsonNode group = TS3Bot.getInstance().getConfig().getPermissions().get(permission);
		
		if(group != null)
		{
			String uid = this.getClientInfo().getUniqueIdentifier();
			Iterator<JsonNode> iterator = group.iterator();
			
			while(iterator.hasNext())
			{
				if(iterator.next().asText().equals(uid))
				{
					return true;
				}
			}
		}
		
		return false;
	}
}
