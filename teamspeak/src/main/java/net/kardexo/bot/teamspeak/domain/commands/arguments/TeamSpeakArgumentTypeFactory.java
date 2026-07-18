package net.kardexo.bot.teamspeak.domain.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.kardexo.bot.domain.channel.IChannel;
import net.kardexo.bot.domain.client.IBotClient;
import net.kardexo.bot.domain.client.IClient;
import net.kardexo.bot.domain.commands.CommandSource;
import net.kardexo.bot.domain.commands.arguments.ArgumentTypeFactory;

public class TeamSpeakArgumentTypeFactory implements ArgumentTypeFactory {
	@Override
	public ArgumentType<IChannel> createChannelArgumentType(IBotClient bot) {
		return TeamSpeakChannelArgumentType.channel(bot);
	}
	
	@Override
	public IChannel getChannelArgumentType(CommandContext<CommandSource> context, String name) {
		return context.getArgument(name, IChannel.class);
	}
	
	@Override
	public ArgumentType<IClient> createClientArgumentType(IBotClient bot) {
		return TeamSpeakClientArgumentType.client(bot);
	}
	
	@Override
	public IClient getClientArgumentType(CommandContext<CommandSource> context, String name) {
		return context.getArgument(name, IClient.class);
	}
}
