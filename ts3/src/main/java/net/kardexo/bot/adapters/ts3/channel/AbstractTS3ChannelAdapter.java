package net.kardexo.bot.adapters.ts3.channel;

import com.github.theholywaffle.teamspeak3.TS3Api;
import net.kardexo.bot.adapters.ts3.TS3ServerAdapter;
import net.kardexo.bot.domain.api.IChannel;
import net.kardexo.bot.domain.api.IServer;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractTS3ChannelAdapter implements IChannel
{
	protected final TS3Api api;
	private IServer server;
	
	protected AbstractTS3ChannelAdapter(TS3Api api)
	{
		this.api = api;
	}
	
	@Override
	public boolean isJoinable()
	{
		return false;
	}
	
	@Override
	public @NotNull IServer getServer()
	{
		if(this.server == null)
		{
			this.server = new TS3ServerAdapter(this.api, this.api.getServerInfo().getId());
		}
		
		return this.server;
	}
}
