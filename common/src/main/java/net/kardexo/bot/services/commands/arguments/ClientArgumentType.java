package net.kardexo.bot.services.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.kardexo.bot.domain.CommandSource;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.api.IClient;

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
