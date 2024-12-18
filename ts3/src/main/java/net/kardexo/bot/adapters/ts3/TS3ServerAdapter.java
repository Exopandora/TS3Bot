package net.kardexo.bot.adapters.ts3;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.api.exception.TS3CommandFailedException;
import net.kardexo.bot.adapters.ts3.channel.TS3MessageChannelAdapter;
import net.kardexo.bot.adapters.ts3.channel.TS3ServerChannelAdapter;
import net.kardexo.bot.domain.api.IChannel;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.domain.api.IServer;
import net.kardexo.bot.domain.api.IServerChannel;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TS3ServerAdapter implements IServer
{
	private final TS3Api api;
	private final int serverId;
	
	public TS3ServerAdapter(TS3Api api, int serverId)
	{
		this.api = api;
		this.serverId = serverId;
	}
	
	@Override
	public String getId()
	{
		return this.api.getServerInfo().getUniqueIdentifier();
	}
	
	@Override
	public String getName()
	{
		return this.api.getServerInfo().getName();
	}
	
	@Override
	public Optional<IChannel> findChannelByName(String name)
	{
		try
		{
			int channelId = this.api.getChannelByNameExact(name, true).getId();
			return Optional.of(new TS3MessageChannelAdapter(this.api, channelId));
		}
		catch(TS3CommandFailedException e)
		{
			return Optional.empty();
		}
	}
	
	@Override
	public Optional<IChannel> findChannelById(String id)
	{
		try
		{
			int channelId = this.api.getChannelInfo(Integer.parseInt(id)).getId();
			return Optional.of(new TS3MessageChannelAdapter(this.api, channelId));
		}
		catch(TS3CommandFailedException e)
		{
			return Optional.empty();
		}
	}
	
	@Override
	public List<IClient> getClients()
	{
		return this.api.getClients().stream()
			.map(client -> new TS3ClientAdapter(this.api, client.getId()))
			.collect(Collectors.toList());
	}
	
	@Override
	public List<IChannel> getChannels()
	{
		return this.api.getChannels().stream()
			.map(channel -> new TS3MessageChannelAdapter(this.api, channel.getId()))
			.collect(Collectors.toList());
	}
	
	@Override
	public IServerChannel getServerChannel()
	{
		return new TS3ServerChannelAdapter(this.api);
	}
	
	public int getServerId()
	{
		return this.serverId;
	}
}
