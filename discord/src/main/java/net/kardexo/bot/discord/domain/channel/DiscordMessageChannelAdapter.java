package net.kardexo.bot.discord.domain.channel;

import discord4j.core.object.entity.channel.GuildMessageChannel;
import net.kardexo.bot.domain.channel.IMessageChannel;

public class DiscordMessageChannelAdapter extends AbstractDiscordChannelAdapter implements IMessageChannel {
	public DiscordMessageChannelAdapter(GuildMessageChannel channel) {
		super(channel);
	}
	
	public GuildMessageChannel getChannel() {
		return (GuildMessageChannel) this.channel;
	}
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof DiscordMessageChannelAdapter)) {
			return false;
		}
		return super.equals(object);
	}
}
