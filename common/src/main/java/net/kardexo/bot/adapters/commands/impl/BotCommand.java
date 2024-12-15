package net.kardexo.bot.adapters.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.kardexo.bot.adapters.commands.CommandSource;
import net.kardexo.bot.adapters.commands.Commands;

public class BotCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("bot")
			.executes(BotCommand::bot));
	}
	
	private static int bot(CommandContext<CommandSource> context)
	{
		context.getSource().sendPrivateMessage("KardExo Bot");
		return 0;
	}
}
