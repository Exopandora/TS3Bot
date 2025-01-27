package net.kardexo.bot.adapters.ts3.channel;

import com.github.theholywaffle.teamspeak3.TS3Api;
import net.kardexo.bot.adapters.ts3.TS3ClientAdapter;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.domain.api.IMessageChannel;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TS3MessageChannelAdapter extends AbstractTS3ChannelAdapter implements IMessageChannel
{
	private final int channelId;
	
	public TS3MessageChannelAdapter(TS3Api api, int channelId)
	{
		super(api);
		this.channelId = channelId;
	}
	
	@Override
	public String getName()
	{
		return this.api.getChannelInfo(this.channelId).getName();
	}
	
	@Override
	public String getId()
	{
		return String.valueOf(this.channelId);
	}
	
	@Override
	public List<IClient> getClients()
	{
		return this.api.getClients().stream()
			.filter(client -> client.getChannelId() == this.channelId && client.isRegularClient())
			.map(client -> new TS3ClientAdapter(this.api, client.getId()))
			.collect(Collectors.toList());
	}
	
	@Override
	public boolean isJoinable()
	{
		return true;
	}
	
	@Override
	public boolean equals(Object object)
	{
		if(!(object instanceof TS3MessageChannelAdapter other))
		{
			return false;
		}
		
		return this.channelId == other.channelId;
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(this.api, this.channelId);
	}
}
