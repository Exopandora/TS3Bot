package net.kardexo.bot.adapters.discord;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.GuildChannel;
import net.kardexo.bot.adapters.discord.channel.AbstractDiscordChannelAdapter;
import net.kardexo.bot.adapters.discord.channel.DiscordServerChannelAdapter;
import net.kardexo.bot.domain.api.IChannel;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.domain.api.IServer;
import net.kardexo.bot.domain.api.IServerChannel;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class DiscordServerAdapter implements IServer
{
	private final Guild guild;
	
	public DiscordServerAdapter(Guild guild)
	{
		this.guild = guild;
	}
	
	@Override
	public String getId()
	{
		return this.guild.getId().asString();
	}
	
	@Override
	public String getName()
	{
		return this.guild.getName();
	}
	
	@Override
	public Optional<IChannel> findChannelByName(String name)
	{
		GuildChannel guildChannel = this.guild.getChannels()
			.filter(channel -> channel.getName().equals(name))
			.blockFirst();
		return Optional.ofNullable(guildChannel)
			.map(AbstractDiscordChannelAdapter::of)
			.flatMap(Function.identity());
	}
	
	@Override
	public Optional<IChannel> findChannelById(String id)
	{
		return this.guild.getChannelById(Snowflake.of(id))
			.map(AbstractDiscordChannelAdapter::of)
			.blockOptional()
			.flatMap(Function.identity());
	}
	
	@Override
	public List<IClient> getClients()
	{
		return this.guild.getMembers().<IClient>map(DiscordClientAdapter::new).collectList().block();
	}
	
	@Override
	public List<IChannel> getChannels()
	{
		return this.guild.getChannels().map(AbstractDiscordChannelAdapter::of)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collectList()
			.block();
	}
	
	@Override
	public IServerChannel getServerChannel()
	{
		return new DiscordServerChannelAdapter(this.guild.getPublicUpdatesChannel().block());
	}
	
	public Guild getGuild()
	{
		return this.guild;
	}
}
