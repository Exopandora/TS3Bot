package net.kardexo.bot.discord.domain.channel;

import discord4j.core.object.entity.channel.PrivateChannel;
import net.kardexo.bot.domain.channel.IPrivateChannel;

public class DiscordPrivateChannel extends AbstractDiscordChannel implements IPrivateChannel {
	public DiscordPrivateChannel(PrivateChannel channel) {
		super(channel);
	}
	
	public PrivateChannel getChannel() {
		return (PrivateChannel) this.channel;
	}
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof DiscordPrivateChannel)) {
			return false;
		}
		return super.equals(object);
	}
}
