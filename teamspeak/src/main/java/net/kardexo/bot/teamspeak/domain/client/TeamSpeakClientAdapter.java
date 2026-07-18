package net.kardexo.bot.teamspeak.domain.client;

import com.github.theholywaffle.teamspeak3.TS3Api;
import net.kardexo.bot.domain.channel.IChannel;
import net.kardexo.bot.domain.channel.IPrivateChannel;
import net.kardexo.bot.domain.client.IClient;
import net.kardexo.bot.teamspeak.domain.channel.TeamSpeakMessageChannelAdapter;
import net.kardexo.bot.teamspeak.domain.channel.TeamSpeakPrivateChannelAdapter;

import java.util.Objects;

public class TeamSpeakClientAdapter implements IClient {
	protected final TS3Api api;
	protected final int clientId;
	private String uniqueId;
	
	public TeamSpeakClientAdapter(TS3Api api, int clientId) {
		this.api = api;
		this.clientId = clientId;
	}
	
	@Override
	public String getId() {
		if (this.uniqueId == null) {
			this.uniqueId = this.api.getClientInfo(this.clientId).getUniqueIdentifier();
		}
		return this.uniqueId;
	}
	
	@Override
	public String getName() {
		return this.api.getClientInfo(this.clientId).getNickname();
	}
	
	@Override
	public IPrivateChannel getPrivateChannel() {
		return new TeamSpeakPrivateChannelAdapter(this.api, this.clientId);
	}
	
	public int getClientId() {
		return this.clientId;
	}
	
	public IChannel getChannel() {
		return new TeamSpeakMessageChannelAdapter(this.api, this.api.getClientInfo(this.clientId).getChannelId());
	}
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof TeamSpeakClientAdapter other)) {
			return false;
		}
		return this.clientId == other.clientId;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.api, this.clientId);
	}
}
