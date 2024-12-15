package net.kardexo.bot.adapters.ts3;

import com.github.theholywaffle.teamspeak3.TS3Api;
import net.kardexo.bot.domain.api.IChannel;
import net.kardexo.bot.domain.api.IClient;

import java.util.Objects;

public class TS3ClientAdapter implements IClient
{
	protected final TS3Api api;
	protected final int clientId;
	private String uniqueId;
	
	public TS3ClientAdapter(TS3Api api, int clientId)
	{
		this.api = api;
		this.clientId = clientId;
	}
	
	@Override
	public String getId()
	{
		return String.valueOf(this.clientId);
	}
	
	@Override
	public String getUniqueId()
	{
		if(this.uniqueId == null)
		{
			this.uniqueId = this.api.getClientInfo(this.clientId).getUniqueIdentifier();
		}
		
		return this.uniqueId;
	}
	
	@Override
	public String getName()
	{
		return this.api.getClientInfo(this.clientId).getNickname();
	}
	
	@Override
	public IChannel getChannel()
	{
		return new TS3ChannelAdapter(this.api, this.api.getClientInfo(this.clientId).getChannelId());
	}
	
	@Override
	public boolean equals(Object object)
	{
		if(!(object instanceof TS3ClientAdapter other))
		{
			return false;
		}
		
		return this.clientId == other.clientId && Objects.equals(this.api, other.api);
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(this.api, this.clientId);
	}
}
