package net.kardexo.bot.teamspeak.domain.server;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.api.exception.TS3CommandFailedException;
import net.kardexo.bot.domain.channel.IChannel;
import net.kardexo.bot.domain.channel.IServerChannel;
import net.kardexo.bot.domain.client.IClient;
import net.kardexo.bot.domain.server.IServer;
import net.kardexo.bot.teamspeak.domain.channel.TeamSpeakMessageChannelAdapter;
import net.kardexo.bot.teamspeak.domain.channel.TeamSpeakServerChannelAdapter;
import net.kardexo.bot.teamspeak.domain.client.TeamSpeakClientAdapter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TeamSpeakServerAdapter implements IServer
{
	private final TS3Api api;
	private final int serverId;
	
	public TeamSpeakServerAdapter(TS3Api api, int serverId)
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
			return Optional.of(new TeamSpeakMessageChannelAdapter(this.api, channelId));
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
			return Optional.of(new TeamSpeakMessageChannelAdapter(this.api, channelId));
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
			.map(client -> new TeamSpeakClientAdapter(this.api, client.getId()))
			.collect(Collectors.toList());
	}
	
	@Override
	public List<IChannel> getChannels()
	{
		return this.api.getChannels().stream()
			.map(channel -> new TeamSpeakMessageChannelAdapter(this.api, channel.getId()))
			.collect(Collectors.toList());
	}
	
	@Override
	public IServerChannel getServerChannel()
	{
		return new TeamSpeakServerChannelAdapter(this.api);
	}
	
	public int getServerId()
	{
		return this.serverId;
	}
}
