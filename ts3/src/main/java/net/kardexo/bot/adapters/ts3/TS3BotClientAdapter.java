package net.kardexo.bot.adapters.ts3;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.api.TextMessageTargetMode;
import com.github.theholywaffle.teamspeak3.api.exception.TS3CommandFailedException;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.api.IChannel;
import net.kardexo.bot.domain.api.IClient;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TS3BotClientAdapter extends TS3ClientAdapter implements IBotClient
{
	private boolean isSilent;
	
	public TS3BotClientAdapter(TS3Api api, int clientId)
	{
		super(api, clientId);
	}
	
	@Override
	public void sendPrivateMessage(IClient client, String message)
	{
		if(client instanceof IBotClient)
		{
			this.sendConsoleMessage(message);
		}
		else
		{
			this.api.sendPrivateMessage(Integer.parseInt(client.getId()), message);
		}
	}
	
	@Override
	public void sendChannelMessage(String message)
	{
		this.api.sendTextMessage(TextMessageTargetMode.CHANNEL, -1, message);
	}
	
	@Override
	public void sendServerMessage(String message)
	{
		this.api.sendServerMessage(message);
	}
	
	@Override
	public void sendConsoleMessage(String message)
	{
		System.out.println(message);
	}
	
	@Override
	public Optional<IChannel> findChannelByName(String name)
	{
		try
		{
			int channelId = this.api.getChannelByNameExact(name, true).getId();
			return Optional.of(new TS3ChannelAdapter(this.api, channelId));
		}
		catch(TS3CommandFailedException e)
		{
			return Optional.empty();
		}
	}
	
	@Override
	public Optional<IChannel> findChannelById(String id)
	{
		try
		{
			int channelId = this.api.getChannelInfo(Integer.parseInt(id)).getId();
			return Optional.of(new TS3ChannelAdapter(this.api, channelId));
		}
		catch(TS3CommandFailedException e)
		{
			return Optional.empty();
		}
	}
	
	@Override
	public Optional<IClient> findClientByName(String name)
	{
		try
		{
			int clientId = this.api.getClientByNameExact(name, true).getId();
			return Optional.of(new TS3ClientAdapter(this.api, clientId));
		}
		catch(TS3CommandFailedException e)
		{
			return Optional.empty();
		}
	}
	
	@Override
	public Optional<IClient> findClientById(String id)
	{
		try
		{
			return Optional.of(new TS3ClientAdapter(this.api, Integer.parseInt(id)));
		}
		catch(TS3CommandFailedException e)
		{
			return Optional.empty();
		}
	}
	
	@Override
	public List<IClient> getClients()
	{
		return this.api.getClients().stream()
			.map(client -> new TS3ClientAdapter(this.api, client.getId()))
			.collect(Collectors.toList());
	}
	
	@Override
	public List<IChannel> getChannels()
	{
		return this.api.getChannels().stream()
			.map(channel -> new TS3ChannelAdapter(this.api, channel.getId()))
			.collect(Collectors.toList());
	}
	
	@Override
	public void ban(@Nullable String reason, Duration duration, IClient client)
	{
		if(reason == null)
		{
			this.api.banClient(Integer.parseInt(client.getId()), duration.toSeconds());
		}
		else
		{
			this.api.banClient(Integer.parseInt(client.getId()), duration.toSeconds(), reason);
		}
	}
	
	@Override
	public void kick(@Nullable String reason, IClient... clients)
	{
		int[] clientIds = Arrays.stream(clients)
			.mapToInt(client -> Integer.parseInt(client.getId()))
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
		if(!client.getChannel().equals(channel))
		{
			this.api.moveClient(Integer.parseInt(client.getId()), Integer.parseInt(channel.getId()));
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
}
