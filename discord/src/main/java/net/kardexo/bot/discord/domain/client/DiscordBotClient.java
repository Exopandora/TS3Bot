package net.kardexo.bot.discord.domain.client;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import net.kardexo.bot.discord.domain.channel.DiscordMessageChannel;
import net.kardexo.bot.discord.domain.channel.DiscordPrivateChannel;
import net.kardexo.bot.discord.domain.channel.DiscordServerChannel;
import net.kardexo.bot.discord.domain.server.DiscordServer;
import net.kardexo.bot.domain.channel.IChannel;
import net.kardexo.bot.domain.channel.IConsoleChannel;
import net.kardexo.bot.domain.channel.IMessageChannel;
import net.kardexo.bot.domain.channel.IPrivateChannel;
import net.kardexo.bot.domain.channel.IServerChannel;
import net.kardexo.bot.domain.client.IBotClient;
import net.kardexo.bot.domain.client.IClient;
import net.kardexo.bot.domain.server.IServer;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public class DiscordBotClient extends DiscordClient implements IBotClient {
	private final GatewayDiscordClient gatewayDiscordClient;
	private boolean isSilent;
	
	public DiscordBotClient(GatewayDiscordClient gatewayDiscordClient) {
		super(gatewayDiscordClient.getSelf().block());
		this.gatewayDiscordClient = gatewayDiscordClient;
	}
	
	@Override
	public void sendPrivateMessage(IPrivateChannel channel, String message) {
		((DiscordPrivateChannel) channel).getChannel().createMessage(message).block();
	}
	
	@Override
	public void sendServerMessage(IServerChannel channel, String message) {
		((DiscordServerChannel) channel).getChannel().createMessage(message).block();
	}
	
	@Override
	public void sendChannelMessage(IMessageChannel channel, String message) {
		((DiscordMessageChannel) channel).getChannel().createMessage(message).block();
	}
	
	@Override
	public void sendConsoleMessage(IConsoleChannel channel, String message) {
		System.out.println(message);
	}
	
	@Override
	public void ban(IServer server, @Nullable String reason, Duration duration, IClient client) {
		Guild guild = ((DiscordServer) server).getGuild();
		if (reason == null) {
			guild.ban(((DiscordClient) client).getClientId()).block();
		} else {
			guild.ban(((DiscordClient) client).getClientId()).withReason(reason).block();
		}
	}
	
	@Override
	public void kick(IServer server, @Nullable String reason, IClient... clients) {
		Guild guild = ((DiscordServer) server).getGuild();
		if (reason == null) {
			for (IClient client : clients) {
				guild.kick(((DiscordClient) client).getClientId());
			}
		} else {
			for (IClient client : clients) {
				guild.kick(((DiscordClient) client).getClientId(), reason);
			}
		}
	}
	
	@Override
	public void move(IClient client, IChannel channel) {
		// NO-OP: clients cannot be moved in discord
	}
	
	@Override
	public void disconnect() {
		this.gatewayDiscordClient.logout();
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
		return new DiscordPrivateChannel(
			this.gatewayDiscordClient.getSelf().blockOptional().orElseThrow().getPrivateChannel().block()
		);
	}
	
	public GatewayDiscordClient getGatewayDiscordClient() {
		return this.gatewayDiscordClient;
	}
}
