package net.kardexo.bot.adapters.ts3.channel;

import com.github.theholywaffle.teamspeak3.TS3Api;
import net.kardexo.bot.domain.api.IChannel;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.domain.api.IConsoleChannel;

import java.util.Collections;
import java.util.List;

public class TS3ConsoleChannelAdapter extends AbstractTS3ChannelAdapter implements IConsoleChannel
{
	private final int clientId;
	
	public TS3ConsoleChannelAdapter(TS3Api api, int clientId)
	{
		super(api);
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
