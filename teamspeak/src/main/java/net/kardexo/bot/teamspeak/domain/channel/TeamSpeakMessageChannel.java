package net.kardexo.bot.teamspeak.domain.channel;

import com.github.theholywaffle.teamspeak3.TS3Api;
import net.kardexo.bot.domain.channel.IMessageChannel;
import net.kardexo.bot.domain.client.IClient;
import net.kardexo.bot.teamspeak.domain.client.TeamSpeakClient;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TeamSpeakMessageChannel extends AbstractTeamSpeakChannel implements IMessageChannel {
	private final int channelId;
	
	public TeamSpeakMessageChannel(TS3Api api, int channelId) {
		super(api);
		this.channelId = channelId;
	}
	
	@Override
	public String getName() {
		return this.api.getChannelInfo(this.channelId).getName();
	}
	
	@Override
	public String getId() {
		return String.valueOf(this.channelId);
	}
	
	@Override
	public List<IClient> getClients() {
		return this.api.getClients().stream()
			.filter(client -> client.getChannelId() == this.channelId && client.isRegularClient())
			.map(client -> new TeamSpeakClient(this.api, client.getId()))
			.collect(Collectors.toList());
	}
	
	@Override
	public boolean isJoinable() {
		return true;
	}
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof TeamSpeakMessageChannel other)) {
			return false;
		}
		return this.channelId == other.channelId;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.api, this.channelId);
	}
}
