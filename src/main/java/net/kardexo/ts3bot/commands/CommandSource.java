package net.kardexo.ts3bot.commands;

import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.manevolent.ts3j.api.Client;
import com.github.manevolent.ts3j.api.TextMessageTargetMode;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.util.TS3Utils;

public class CommandSource
{
	private final Client client;
	private final TextMessageTargetMode target;
	
	public CommandSource(Client client, TextMessageTargetMode target)
	{
		this.client = client;
		this.target = target;
	}
	
	public Client getClient()
	{
		return this.client;
	}
	
	public TextMessageTargetMode getTarget()
	{
		return this.target;
	}
	
	public void sendFeedback(String message)
	{
		TS3Utils.sendMessage(this.target, this.client, message);
	}
	
	public void sendPrivateMessage(String message)
	{
		TS3Utils.sendMessage(TextMessageTargetMode.CLIENT, this.client, message);
	}
	
	public boolean hasPermission(String permission)
	{
		JsonNode group = TS3Bot.getInstance().getConfig().getPermissions().get(permission);
		
		if(group != null)
		{
			String uid = this.client.getUniqueIdentifier();
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
