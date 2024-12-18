package net.kardexo.bot.adapters.ts3.channel;

import com.github.theholywaffle.teamspeak3.TS3Api;
import net.kardexo.bot.adapters.ts3.TS3ServerAdapter;
import net.kardexo.bot.domain.api.IChannel;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.domain.api.IConsoleChannel;
import net.kardexo.bot.domain.api.IServer;

import java.util.Collections;
import java.util.List;

public class TS3ConsoleChannelAdapter implements IConsoleChannel
{
	private final TS3Api api;
	private final int clientId;
	
	public TS3ConsoleChannelAdapter(TS3Api api, int clientId)
	{
		this.api = api;
		this.clientId = clientId;
	}
	
	@Override
	public String getId()
	{
		return "-1";
	}
	
	@Override
	public List<IClient> getClients()
	{
		return Collections.emptyList();
	}
	
	@Override
	public IServer getServer()
	{
		return new TS3ServerAdapter(this.api, this.api.getServerInfo().getId());
	}
	
	@Override
	public List<IChannel> getBroadcastChannels()
	{
		return Collections.singletonList(new TS3MessageChannelAdapter(this.api, this.api.getClientInfo(this.clientId).getChannelId()));
	}
	
	@Override
	public boolean equals(Object object)
	{
		if(!(object instanceof TS3ConsoleChannelAdapter that))
		{
			return false;
		}
		
		return this.getId().equals(that.getId());
	}
	
	@Override
	public int hashCode()
	{
		return this.getId().hashCode();
	}
}
