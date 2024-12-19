package net.kardexo.bot.adapters.discord;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import net.kardexo.bot.adapters.discord.channel.DiscordMessageChannelAdapter;
import net.kardexo.bot.adapters.discord.channel.DiscordPrivateChannelAdapter;
import net.kardexo.bot.adapters.discord.channel.DiscordServerChannelAdapter;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.api.IChannel;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.domain.api.IConsoleChannel;
import net.kardexo.bot.domain.api.IMessageChannel;
import net.kardexo.bot.domain.api.IPrivateChannel;
import net.kardexo.bot.domain.api.IServer;
import net.kardexo.bot.domain.api.IServerChannel;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public class DiscordBotClientAdapter extends DiscordClientAdapter implements IBotClient
{
	private final GatewayDiscordClient gatewayDiscordClient;
	private boolean isSilent;
	
	public DiscordBotClientAdapter(GatewayDiscordClient gatewayDiscordClient)
	{
		super(gatewayDiscordClient.getSelf().block());
		this.gatewayDiscordClient = gatewayDiscordClient;
	}
	
	@Override
	public void sendPrivateMessage(IPrivateChannel channel, String message)
	{
		((DiscordPrivateChannelAdapter) channel).getChannel().createMessage(message).block();
	}
	
	@Override
	public void sendServerMessage(IServerChannel channel, String message)
	{
		((DiscordServerChannelAdapter) channel).getChannel().createMessage(message).block();
	}
	
	@Override
	public void sendChannelMessage(IMessageChannel channel, String message)
	{
		((DiscordMessageChannelAdapter) channel).getChannel().createMessage(message).block();
	}
	
	@Override
	public void sendConsoleMessage(IConsoleChannel channel, String message)
	{
		System.out.println(message);
	}
	
	@Override
	public void ban(IServer server, @Nullable String reason, Duration duration, IClient client)
	{
		Guild guild = ((DiscordServerAdapter) server).getGuild();
		
		if(reason == null)
		{
			guild.ban(((DiscordClientAdapter) client).getClientId()).block();
		}
		else
		{
			guild.ban(((DiscordClientAdapter) client).getClientId()).withReason(reason).block();
		}
	}
	
	@Override
	public void kick(IServer server, @Nullable String reason, IClient... clients)
	{
		Guild guild = ((DiscordServerAdapter) server).getGuild();
		
		if(reason == null)
		{
			for(IClient client : clients)
			{
				guild.kick(((DiscordClientAdapter) client).getClientId());
			}
		}
		else
		{
			for(IClient client : clients)
			{
				guild.kick(((DiscordClientAdapter) client).getClientId(), reason);
			}
		}
	}
	
	@Override
	public void move(IClient client, IChannel channel)
	{
		// NO-OP: clients cannot be moved in discord
	}
	
	@Override
	public void disconnect()
	{
		this.gatewayDiscordClient.logout();
	}
	
	@Override
	public boolean isSilent()
	{
		return this.isSilent;
	}
	
	@Override
	public void setSilent(boolean silent)
	{
		this.isSilent = silent;
	}
	
	@Override
	public IPrivateChannel getPrivateChannel()
	{
		return new DiscordPrivateChannelAdapter(this.gatewayDiscordClient.getSelf().blockOptional().orElseThrow().getPrivateChannel().block());
	}
	
	public GatewayDiscordClient getGatewayDiscordClient()
	{
		return this.gatewayDiscordClient;
	}
}
