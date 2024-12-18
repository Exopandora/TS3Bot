package net.kardexo.bot.adapters.discord.channel;

import discord4j.core.object.entity.channel.PrivateChannel;
import net.kardexo.bot.domain.api.IPrivateChannel;

public class DiscordPrivateChannelAdapter extends AbstractDiscordChannelAdapter implements IPrivateChannel
{
	public DiscordPrivateChannelAdapter(PrivateChannel channel)
	{
		super(channel);
	}
	
	public PrivateChannel getChannel()
	{
		return (PrivateChannel) this.channel;
	}
	
	@Override
	public boolean equals(Object object)
	{
		if(!(object instanceof DiscordPrivateChannelAdapter))
		{
			return false;
		}
		
		return super.equals(object);
	}
}
