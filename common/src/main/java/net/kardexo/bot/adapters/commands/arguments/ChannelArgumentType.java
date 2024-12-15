package net.kardexo.bot.adapters.commands.arguments;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.api.IChannel;

import java.util.AbstractMap.SimpleEntry;
import java.util.Comparator;

public class ChannelArgumentType implements ArgumentType<IChannel>
{
	private static final DynamicCommandExceptionType CHANNEL_NOT_FOUND = new DynamicCommandExceptionType(channel -> new LiteralMessage("Could not find channel " + channel));
	
	private final IBotClient bot;
	
	private ChannelArgumentType(IBotClient bot)
	{
		this.bot = bot;
	}
	
	@Override
	public IChannel parse(StringReader reader) throws CommandSyntaxException
	{
		String username = normalize(reader.getRemaining());
		SimpleEntry<IChannel, String> result = this.bot.getChannels().stream()
			.map(channel -> new SimpleEntry<IChannel, String>(channel, normalize(channel.getName())))
			.filter(pair -> username.startsWith(pair.getValue()))
			.max(Comparator.comparingInt(pair -> pair.getValue().length()))
			.orElseThrow(() -> CHANNEL_NOT_FOUND.createWithContext(reader, username));
		reader.setCursor(reader.getCursor() + result.getValue().length());
		return result.getKey();
	}
	
	private static String normalize(String string)
	{
		return string.replaceAll("\\s+", " ").trim().toLowerCase();
	}
	
	public static ChannelArgumentType channel(IBotClient bot)
	{
		return new ChannelArgumentType(bot);
	}
	
	public static IChannel getChannel(final CommandContext<?> context, final String name)
	{
		return context.getArgument(name, IChannel.class);
	}
}
