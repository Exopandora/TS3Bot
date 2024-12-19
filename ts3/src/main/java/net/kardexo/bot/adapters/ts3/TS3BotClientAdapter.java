package net.kardexo.bot.adapters.ts3;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.api.TextMessageTargetMode;
import net.kardexo.bot.adapters.ts3.channel.TS3PrivateChannelAdapter;
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
import java.util.Arrays;

public class TS3BotClientAdapter extends TS3ClientAdapter implements IBotClient
{
	private boolean isSilent;
	private int channelId;
	
	public TS3BotClientAdapter(TS3Api api, int clientId)
	{
		super(api, clientId);
		this.channelId = api.getClientInfo(clientId).getChannelId();
	}
	
	@Override
	public void sendPrivateMessage(IPrivateChannel channel, String message)
	{
		this.api.sendPrivateMessage(((TS3PrivateChannelAdapter) channel).getClientId(), message);
	}
	
	@Override
	public void sendServerMessage(IServerChannel channel, String message)
	{
		this.api.sendServerMessage(message);
	}
	
	@Override
	public void sendChannelMessage(IMessageChannel channel, String message)
	{
		this.api.sendTextMessage(TextMessageTargetMode.CHANNEL, -1, message);
	}
	
	@Override
	public void sendConsoleMessage(IConsoleChannel channel, String message)
	{
		System.out.println(message);
	}
	
	@Override
	public void ban(IServer server, @Nullable String reason, Duration duration, IClient client)
	{
		if(reason == null)
		{
			this.api.banClient(((TS3ClientAdapter) client).getClientId(), duration.toSeconds());
		}
		else
		{
			this.api.banClient(((TS3ClientAdapter) client).getClientId(), duration.toSeconds(), reason);
		}
	}
	
	@Override
	public void kick(IServer server, @Nullable String reason, IClient... clients)
	{
		int[] clientIds = Arrays.stream(clients)
			.mapToInt(client -> ((TS3ClientAdapter) client).getClientId())
			.toArray();
		
		if(reason == null)
		{
			this.api.kickClientFromServer(clientIds);
		}
		else
		{
			this.api.kickClientFromServer(reason, clientIds);
		}
	}
	
	@Override
	public void move(IClient client, IChannel channel)
	{
		if(!channel.equals(((TS3ClientAdapter) client).getChannel()))
		{
			this.api.moveClient(((TS3ClientAdapter) client).getClientId(), Integer.parseInt(channel.getId()));
		}
		
		if(client.equals(this))
		{
			this.channelId = Integer.parseInt(channel.getId());
		}
	}
	
	@Override
	public void disconnect()
	{
		this.api.logout();
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
		return new TS3PrivateChannelAdapter(this.api, this.clientId);
	}
	
	public IServer getServer()
	{
		return new TS3ServerAdapter(this.api, this.api.getServerInfo().getId());
	}
	
	public int getChannelId()
	{
		return this.channelId;
	}
}
