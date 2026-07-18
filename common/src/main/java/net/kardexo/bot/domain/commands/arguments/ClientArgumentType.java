package net.kardexo.bot.domain.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.kardexo.bot.domain.client.IBotClient;
import net.kardexo.bot.domain.client.IClient;
import net.kardexo.bot.domain.commands.CommandSource;

public interface ClientArgumentType extends ArgumentType<IClient>
{
	static ArgumentType<IClient> client(IBotClient bot)
	{
		return ArgumentTypeFactory.INSTANCE.createClientArgumentType(bot);
	}
	
	static IClient getClient(final CommandContext<CommandSource> context, final String name)
	{
		return ArgumentTypeFactory.INSTANCE.getClientArgumentType(context, name);
	}
}
