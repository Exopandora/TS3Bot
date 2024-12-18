package net.kardexo.bot.adapters.discord.channel;

import discord4j.core.object.entity.channel.GuildMessageChannel;
import net.kardexo.bot.domain.api.IMessageChannel;

public class DiscordMessageChannelAdapter extends AbstractDiscordChannelAdapter implements IMessageChannel
{
	public DiscordMessageChannelAdapter(GuildMessageChannel channel)
	{
		super(channel);
	}
	
	public GuildMessageChannel getChannel()
	{
		return (GuildMessageChannel) this.channel;
	}
	
	@Override
	public boolean equals(Object object)
	{
		if(!(object instanceof DiscordMessageChannelAdapter))
		{
			return false;
		}
		
		return super.equals(object);
	}
}
