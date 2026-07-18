package net.kardexo.bot.teamspeak.domain.client;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.api.TextMessageTargetMode;
import net.kardexo.bot.domain.channel.IChannel;
import net.kardexo.bot.domain.channel.IConsoleChannel;
import net.kardexo.bot.domain.channel.IMessageChannel;
import net.kardexo.bot.domain.channel.IPrivateChannel;
import net.kardexo.bot.domain.channel.IServerChannel;
import net.kardexo.bot.domain.client.IBotClient;
import net.kardexo.bot.domain.client.IClient;
import net.kardexo.bot.domain.server.IServer;
import net.kardexo.bot.teamspeak.domain.channel.TeamSpeakPrivateChannelAdapter;
import net.kardexo.bot.teamspeak.domain.server.TeamSpeakServerAdapter;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Arrays;

public class TeamSpeakBotClientAdapter extends TeamSpeakClientAdapter implements IBotClient {
	private boolean isSilent;
	private int channelId;
	
	public TeamSpeakBotClientAdapter(TS3Api api, int clientId) {
		super(api, clientId);
		this.channelId = api.getClientInfo(clientId).getChannelId();
	}
	
	@Override
	public void sendPrivateMessage(IPrivateChannel channel, String message) {
		this.api.sendPrivateMessage(((TeamSpeakPrivateChannelAdapter) channel).getClientId(), message);
	}
	
	@Override
	public void sendServerMessage(IServerChannel channel, String message) {
		this.api.sendServerMessage(message);
	}
	
	@Override
	public void sendChannelMessage(IMessageChannel channel, String message) {
		this.api.sendTextMessage(TextMessageTargetMode.CHANNEL, -1, message);
	}
	
	@Override
	public void sendConsoleMessage(IConsoleChannel channel, String message) {
		System.out.println(message);
	}
	
	@Override
	public void ban(IServer server, @Nullable String reason, Duration duration, IClient client) {
		if (reason == null) {
			this.api.banClient(((TeamSpeakClientAdapter) client).getClientId(), duration.toSeconds());
		} else {
			this.api.banClient(((TeamSpeakClientAdapter) client).getClientId(), duration.toSeconds(), reason);
		}
	}
	
	@Override
	public void kick(IServer server, @Nullable String reason, IClient... clients) {
		int[] clientIds = Arrays.stream(clients)
			.mapToInt(client -> ((TeamSpeakClientAdapter) client).getClientId())
			.toArray();
		if (reason == null) {
			this.api.kickClientFromServer(clientIds);
		} else {
			this.api.kickClientFromServer(reason, clientIds);
		}
	}
	
	@Override
	public void move(IClient client, IChannel channel) {
		if (!channel.equals(((TeamSpeakClientAdapter) client).getChannel())) {
			this.api.moveClient(((TeamSpeakClientAdapter) client).getClientId(), Integer.parseInt(channel.getId()));
		}
		if (client.equals(this)) {
			this.channelId = Integer.parseInt(channel.getId());
		}
	}
	
	@Override
	public void disconnect() {
		this.api.logout();
	}
	
	@Override
	public boolean isSilent() {
		return this.isSilent;
	}
	
	@Override
	public void setSilent(boolean silent) {
		this.isSilent = silent;
	}
	
	@Override
	public IPrivateChannel getPrivateChannel() {
		return new TeamSpeakPrivateChannelAdapter(this.api, this.clientId);
	}
	
	public IServer getServer() {
		return new TeamSpeakServerAdapter(this.api, this.api.getServerInfo().getId());
	}
	
	public int getChannelId() {
		return this.channelId;
	}
}
