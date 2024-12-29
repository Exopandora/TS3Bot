package net.kardexo.bot.adapters.ts3.channel;

import com.github.theholywaffle.teamspeak3.TS3Api;
import net.kardexo.bot.adapters.ts3.TS3ClientAdapter;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.domain.api.IPrivateChannel;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TS3PrivateChannelAdapter extends AbstractTS3ChannelAdapter implements IPrivateChannel
{
	private final int clientId;
	
	public TS3PrivateChannelAdapter(TS3Api api, int clientId)
	{
		super(api);
		this.clientId = clientId;
	}
	
	@Override
	public String getName()
	{
		return this.api.getChannelInfo(this.clientId).getName();
	}
	
	@Override
	public String getId()
	{
		return String.valueOf(this.clientId);
	}
	
	@Override
	public List<IClient> getClients()
	{
		return Collections.singletonList(new TS3ClientAdapter(this.api, this.clientId));
	}
	
	public int getClientId()
	{
		return this.clientId;
	}
	
	@Override
	public boolean equals(Object object)
	{
		if(!(object instanceof TS3PrivateChannelAdapter that))
		{
			return false;
		}
		
		return this.clientId == that.clientId;
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(this.api, this.clientId);
	}
}
