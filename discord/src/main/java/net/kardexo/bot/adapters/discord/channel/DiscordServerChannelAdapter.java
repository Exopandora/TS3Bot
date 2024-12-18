package net.kardexo.bot.adapters.discord.channel;

import discord4j.core.object.entity.channel.MessageChannel;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.domain.api.IServerChannel;

import java.util.Collections;
import java.util.List;

public class DiscordServerChannelAdapter extends AbstractDiscordChannelAdapter implements IServerChannel
{
	public DiscordServerChannelAdapter(MessageChannel channel)
	{
		super(channel);
	}
	
	@Override
	public List<IClient> getClients()
	{
		return Collections.emptyList();
	}
	
	public MessageChannel getChannel()
	{
		return (MessageChannel) this.channel;
	}
	
	@Override
	public boolean equals(Object object)
	{
		if(!(object instanceof  DiscordServerChannelAdapter))
		{
			return false;
		}
		
		return super.equals(object);
	}
}
