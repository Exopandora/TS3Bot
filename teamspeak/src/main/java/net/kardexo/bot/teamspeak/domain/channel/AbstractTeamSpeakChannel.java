package net.kardexo.bot.teamspeak.domain.channel;

import com.github.theholywaffle.teamspeak3.TS3Api;
import net.kardexo.bot.domain.channel.IChannel;
import net.kardexo.bot.domain.server.IServer;
import net.kardexo.bot.teamspeak.domain.server.TeamSpeakServer;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractTeamSpeakChannel implements IChannel {
	protected final TS3Api api;
	private IServer server;
	
	protected AbstractTeamSpeakChannel(TS3Api api) {
		this.api = api;
	}
	
	@Override
	public boolean isJoinable() {
		return false;
	}
	
	@Override
	public @NotNull IServer getServer() {
		if (this.server == null) {
			this.server = new TeamSpeakServer(this.api, this.api.getServerInfo().getId());
		}
		return this.server;
	}
}
