package net.kardexo.bot.discord.domain.channel;

import net.kardexo.bot.domain.channel.IChannel;
import net.kardexo.bot.domain.channel.IConsoleChannel;
import net.kardexo.bot.domain.client.IClient;
import net.kardexo.bot.domain.server.IServer;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class DiscordConsoleChannel implements IConsoleChannel {
	@Override
	public String getId() {
		return "-1";
	}
	
	@Override
	public List<IClient> getClients() {
		return Collections.emptyList();
	}
	
	@Override
	public @Nullable IServer getServer() {
		return null;
	}
	
	@Override
	public List<IChannel> getBroadcastChannels() {
		return Collections.emptyList();
	}
	
	@Override
	public boolean isJoinable() {
		return false;
	}
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof DiscordConsoleChannel that)) {
			return false;
		}
		return this.getId().equals(that.getId());
	}
	
	@Override
	public int hashCode() {
		return this.getId().hashCode();
	}
}
