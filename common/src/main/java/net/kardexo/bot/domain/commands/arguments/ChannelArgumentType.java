package net.kardexo.bot.domain.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kardexo.bot.domain.channel.IChannel;
import net.kardexo.bot.domain.client.IBotClient;
import net.kardexo.bot.domain.commands.CommandSource;

public interface ChannelArgumentType extends ArgumentType<IChannel>
{
	static ArgumentType<IChannel> channel(IBotClient bot)
	{
		return ArgumentTypeFactory.INSTANCE.createChannelArgumentType(bot);
	}
	
	static IChannel getChannel(final CommandContext<CommandSource> context, final String name) throws CommandSyntaxException
	{
		return ArgumentTypeFactory.INSTANCE.getChannelArgumentType(context, name);
	}
}
