package net.kardexo.bot.adapters.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kardexo.bot.adapters.commands.CommandSource;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.api.IChannel;
import net.kardexo.bot.domain.api.IClient;

import java.util.ServiceLoader;

public interface ArgumentTypeFactory
{
	ArgumentTypeFactory INSTANCE = ServiceLoader.load(ArgumentTypeFactory.class).findFirst().orElseThrow();
	
	ArgumentType<IChannel> createChannelArgumentType(IBotClient bot);
	
	IChannel getChannelArgumentType(CommandContext<CommandSource> context, String name) throws CommandSyntaxException;
	
	ArgumentType<IClient> createClientArgumentType(IBotClient bot);
	
	IClient getClientArgumentType(CommandContext<CommandSource> context, String name);
}
