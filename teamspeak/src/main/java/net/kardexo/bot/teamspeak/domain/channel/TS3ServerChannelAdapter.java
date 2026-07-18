package net.kardexo.bot.teamspeak.domain.channel;

import com.github.theholywaffle.teamspeak3.TS3Api;
import net.kardexo.bot.domain.channel.IServerChannel;
import net.kardexo.bot.domain.client.IClient;

import java.util.Collections;
import java.util.List;

public class TS3ServerChannelAdapter extends AbstractTS3ChannelAdapter implements IServerChannel
{
	public TS3ServerChannelAdapter(TS3Api api)
	{
		super(api);
	}
	
	@Override
	public String getName()
	{
		return this.api.getServerInfo().getName();
	}
	
	@Override
	public String getId()
	{
		return "-1";
	}
	
	@Override
	public List<IClient> getClients()
	{
		return Collections.emptyList();
	}
}
