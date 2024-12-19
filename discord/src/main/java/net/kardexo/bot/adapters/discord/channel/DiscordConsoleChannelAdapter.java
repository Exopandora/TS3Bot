package net.kardexo.bot.adapters.discord.channel;

import net.kardexo.bot.domain.api.IChannel;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.domain.api.IConsoleChannel;
import net.kardexo.bot.domain.api.IServer;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class DiscordConsoleChannelAdapter implements IConsoleChannel
{
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
	public @Nullable IServer getServer()
	{
		return null;
	}
	
	@Override
	public List<IChannel> getBroadcastChannels()
	{
		return Collections.emptyList();
	}
	
	@Override
	public boolean isJoinable()
	{
		return false;
	}
	
	@Override
	public boolean equals(Object object)
	{
		if(!(object instanceof DiscordConsoleChannelAdapter that))
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
