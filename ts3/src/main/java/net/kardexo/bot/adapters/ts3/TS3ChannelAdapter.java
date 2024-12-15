package net.kardexo.bot.adapters.ts3;

import com.github.theholywaffle.teamspeak3.TS3Api;
import net.kardexo.bot.domain.api.IChannel;
import net.kardexo.bot.domain.api.IClient;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TS3ChannelAdapter implements IChannel
{
	private final TS3Api api;
	private final int channelId;
	
	public TS3ChannelAdapter(TS3Api api, int channelId)
	{
		this.api = api;
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
			.filter(client -> client.getChannelId() == this.channelId)
			.map(client -> new TS3ClientAdapter(this.api, client.getId()))
			.collect(Collectors.toList());
	}
	
	@Override
	public boolean equals(Object object)
	{
		if(object == null || getClass() != object.getClass())
		{
			return false;
		}
		
		TS3ChannelAdapter other = (TS3ChannelAdapter) object;
		return this.channelId == other.channelId && Objects.equals(this.api, other.api);
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(this.api, this.channelId);
	}
}
