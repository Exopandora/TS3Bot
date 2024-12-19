package net.kardexo.bot.adapters.discord.channel;

import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Category;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.entity.channel.NewsChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.object.entity.channel.StoreChannel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.discordjson.json.ChannelData;
import discord4j.discordjson.possible.Possible;
import net.kardexo.bot.adapters.discord.DiscordClientAdapter;
import net.kardexo.bot.adapters.discord.DiscordServerAdapter;
import net.kardexo.bot.domain.api.IChannel;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.domain.api.IServer;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractDiscordChannelAdapter implements IChannel
{
	protected final Channel channel;
	
	protected AbstractDiscordChannelAdapter(Channel channel)
	{
		this.channel = channel;
	}
	
	@Override
	public String getName()
	{
		return switch(this.channel.getType())
		{
			case UNKNOWN -> "Unknown";
			case GUILD_TEXT -> ((TextChannel) this.channel).getName();
			case GUILD_STORE -> ((StoreChannel) this.channel).getName();
			case GUILD_CATEGORY -> ((Category) this.channel).getName();
			case DM, GROUP_DM -> this.channel.getRestChannel().getData().blockOptional()
				.map(ChannelData::name)
				.map(Possible::toOptional)
				.flatMap(optional -> optional)
				.orElseGet(() -> ((PrivateChannel) this.channel).getRecipients().stream().map(User::getUsername).collect(Collectors.joining(", ")));
			case GUILD_VOICE, GUILD_STAGE_VOICE -> ((VoiceChannel) this.channel).getName();
			case GUILD_NEWS -> ((NewsChannel) this.channel).getName();
		};
	}
	
	@Override
	public String getId()
	{
		return this.channel.getId().asString();
	}
	
	@Override
	public List<IClient> getClients()
	{
		return switch(this.channel.getType())
		{
			case UNKNOWN, GUILD_TEXT, GUILD_STORE, GUILD_CATEGORY -> Collections.emptyList();
			case DM, GROUP_DM -> ((PrivateChannel) this.channel).getRecipients().stream()
				.<IClient>map(DiscordClientAdapter::new)
				.toList();
			case GUILD_VOICE, GUILD_STAGE_VOICE -> ((VoiceChannel) this.channel).getMembers()
				.<IClient>map(DiscordClientAdapter::new)
				.collectList()
				.block();
			case GUILD_NEWS -> ((NewsChannel) this.channel).getMembers()
				.<IClient>map(DiscordClientAdapter::new)
				.collectList()
				.block();
		};
	}
	
	@Override
	public @Nullable IServer getServer()
	{
		return switch(this.channel.getType())
		{
			case UNKNOWN, DM, GROUP_DM -> null;
			default -> new DiscordServerAdapter(((GuildChannel) this.channel).getGuild().block());
		};
	}
	
	@Override
	public boolean isJoinable()
	{
		return false;
	}
	
	@Override
	public boolean equals(Object object)
	{
		if(!(object instanceof AbstractDiscordChannelAdapter other))
		{
			return false;
		}
		
		return this.getId().equals(other.getId());
	}
	
	@Override
	public int hashCode()
	{
		return this.channel.hashCode();
	}
	
	public static Optional<IChannel> of(Channel channel)
	{
		return switch(channel)
		{
			case PrivateChannel pc -> Optional.of(new DiscordPrivateChannelAdapter(pc));
			case GuildMessageChannel gc -> Optional.of(new DiscordMessageChannelAdapter(gc));
			default -> Optional.empty();
		};
	}
}
