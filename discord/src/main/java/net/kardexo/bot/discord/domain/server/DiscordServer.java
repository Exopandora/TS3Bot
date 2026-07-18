package net.kardexo.bot.discord.domain.server;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.GuildChannel;
import net.kardexo.bot.discord.domain.channel.AbstractDiscordChannel;
import net.kardexo.bot.discord.domain.channel.DiscordServerChannel;
import net.kardexo.bot.discord.domain.client.DiscordClient;
import net.kardexo.bot.domain.channel.IChannel;
import net.kardexo.bot.domain.channel.IServerChannel;
import net.kardexo.bot.domain.client.IClient;
import net.kardexo.bot.domain.server.IServer;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class DiscordServer implements IServer {
	private final Guild guild;
	
	public DiscordServer(Guild guild) {
		this.guild = guild;
	}
	
	@Override
	public String getId() {
		return this.guild.getId().asString();
	}
	
	@Override
	public String getName() {
		return this.guild.getName();
	}
	
	@Override
	public Optional<IChannel> findChannelByName(String name) {
		GuildChannel guildChannel = this.guild.getChannels()
			.filter(channel -> channel.getName().equals(name))
			.blockFirst();
		return Optional.ofNullable(guildChannel)
			.map(AbstractDiscordChannel::of)
			.flatMap(Function.identity());
	}
	
	@Override
	public Optional<IChannel> findChannelById(String id) {
		return this.guild.getChannelById(Snowflake.of(id))
			.map(AbstractDiscordChannel::of)
			.blockOptional()
			.flatMap(Function.identity());
	}
	
	@Override
	public List<IClient> getClients() {
		return this.guild.getMembers().<IClient>map(DiscordClient::new).collectList().block();
	}
	
	@Override
	public List<IChannel> getChannels() {
		return this.guild.getChannels().map(AbstractDiscordChannel::of)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collectList()
			.block();
	}
	
	@Override
	public IServerChannel getServerChannel() {
		return new DiscordServerChannel(this.guild.getPublicUpdatesChannel().block());
	}
	
	public Guild getGuild() {
		return this.guild;
	}
}
