package net.kardexo.bot.adapters.ts3.channel;

import com.github.theholywaffle.teamspeak3.TS3Api;
import net.kardexo.bot.adapters.ts3.TS3ServerAdapter;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.domain.api.IServer;
import net.kardexo.bot.domain.api.IServerChannel;

import java.util.Collections;
import java.util.List;

public class TS3ServerChannelAdapter implements IServerChannel
{
	private final TS3Api api;
	
	public TS3ServerChannelAdapter(TS3Api api)
	{
		this.api = api;
	}
	
	@Override
	public String getName()
	{
		return this.api.getServerInfo().getName();
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
	public boolean isJoinable()
	{
		return false;
	}
}
