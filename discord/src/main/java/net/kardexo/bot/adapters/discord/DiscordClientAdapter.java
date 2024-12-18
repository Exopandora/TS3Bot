package net.kardexo.bot.adapters.discord;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.User;
import net.kardexo.bot.adapters.discord.channel.DiscordPrivateChannelAdapter;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.domain.api.IPrivateChannel;

public class DiscordClientAdapter implements IClient
{
	private final User user;
	
	public DiscordClientAdapter(User user)
	{
		this.user = user;
	}
	
	@Override
	public String getId()
	{
		return this.user.getId().asString();
	}
	
	@Override
	public String getName()
	{
		return this.user.getGlobalName().orElseGet(this.user::getUsername);
	}
	
	@Override
	public IPrivateChannel getPrivateChannel()
	{
		return this.user.getPrivateChannel().map(DiscordPrivateChannelAdapter::new).block();
	}
	
	public Snowflake getClientId()
	{
		return this.user.getId();
	}
	
	@Override
	public boolean equals(Object object)
	{
		if(!(object instanceof DiscordClientAdapter other))
		{
			return false;
		}
		
		return this.user.getId().equals(other.user.getId());
	}
	
	@Override
	public int hashCode()
	{
		return this.user.hashCode();
	}
}
