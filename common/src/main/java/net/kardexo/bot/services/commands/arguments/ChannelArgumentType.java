package net.kardexo.bot.services.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kardexo.bot.domain.CommandSource;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.api.IChannel;

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
