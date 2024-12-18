package net.kardexo.bot.adapters.commands.arguments;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.kardexo.bot.adapters.commands.CommandSource;
import net.kardexo.bot.domain.api.IChannel;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.domain.api.IServer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DiscordChannelArgumentType implements ArgumentType<IChannel>
{
	private static final DynamicCommandExceptionType CHANNEL_NOT_FOUND = new DynamicCommandExceptionType(id -> new LiteralMessage("Could not find channel <#" + id + ">"));
	
	@Override
	public IChannel parse(StringReader reader) throws CommandSyntaxException
	{
		reader.expect('<');
		reader.expect('@');
		long id = reader.readLong();
		reader.expect('>');
		return new DiscordChannelParseResult(id);
	}
	
	public static DiscordChannelArgumentType channel()
	{
		return new DiscordChannelArgumentType();
	}
	
	record DiscordChannelParseResult(long id) implements IChannel
	{
		@Override
		public String getName()
		{
			throw new RuntimeException("Channel must be resolved before accessing name");
		}
		
		@Override
		public String getId()
		{
			return String.valueOf(this.id);
		}
		
		@Override
		public List<IClient> getClients()
		{
			throw new RuntimeException("Channel must be resolved before accessing clients");
		}
		
		@Override
		public @Nullable IServer getServer()
		{
			return null;
		}
		
		public IChannel resolve(CommandSource context) throws CommandSyntaxException
		{
			IServer server = context.getChannel().getServer();
			
			if(server == null)
			{
				throw CHANNEL_NOT_FOUND.create(this.id);
			}
			
			String id = String.valueOf(this.id);
			
			return server.getChannels().stream()
				.filter(channel -> channel.getId().equals(id))
				.findFirst()
				.orElseThrow(() -> CHANNEL_NOT_FOUND.create(this.id));
		}
	}
}
