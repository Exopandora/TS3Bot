package net.kardexo.bot.adapters.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.kardexo.bot.domain.CommandSource;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.api.IChannel;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.services.commands.arguments.ArgumentTypeFactory;

public class TS3ArgumentTypeFactory implements ArgumentTypeFactory
{
	@Override
	public ArgumentType<IChannel> createChannelArgumentType(IBotClient bot)
	{
		return TS3ChannelArgumentType.channel(bot);
	}
	
	@Override
	public IChannel getChannelArgumentType(CommandContext<CommandSource> context, String name)
	{
		return context.getArgument(name, IChannel.class);
	}
	
	@Override
	public ArgumentType<IClient> createClientArgumentType(IBotClient bot)
	{
		return TS3ClientArgumentType.client(bot);
	}
	
	@Override
	public IClient getClientArgumentType(CommandContext<CommandSource> context, String name)
	{
		return context.getArgument(name, IClient.class);
	}
}
