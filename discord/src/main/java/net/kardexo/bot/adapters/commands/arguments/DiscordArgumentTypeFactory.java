package net.kardexo.bot.adapters.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kardexo.bot.adapters.commands.CommandSource;
import net.kardexo.bot.adapters.commands.arguments.DiscordChannelArgumentType.DiscordChannelParseResult;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.api.IChannel;
import net.kardexo.bot.domain.api.IClient;

public class DiscordArgumentTypeFactory implements ArgumentTypeFactory
{
	@Override
	public ArgumentType<IChannel> createChannelArgumentType(IBotClient bot)
	{
		return DiscordChannelArgumentType.channel();
	}
	
	@Override
	public IChannel getChannelArgumentType(CommandContext<CommandSource> context, String name) throws CommandSyntaxException
	{
		return context.getArgument(name, DiscordChannelParseResult.class).resolve(context.getSource());
	}
	
	@Override
	public ArgumentType<IClient> createClientArgumentType(IBotClient bot)
	{
		return DiscordClientArgumentType.client(bot);
	}
	
	@Override
	public IClient getClientArgumentType(CommandContext<CommandSource> context, String name)
	{
		return context.getArgument(name, IClient.class);
	}
}
