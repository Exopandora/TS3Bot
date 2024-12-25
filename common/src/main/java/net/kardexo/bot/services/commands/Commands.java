package net.kardexo.bot.services.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.kardexo.bot.domain.CommandSource;
import net.kardexo.bot.services.api.ICommandRegistrar;
import net.kardexo.bot.services.commands.impl.BalanceCommand;
import net.kardexo.bot.services.commands.impl.BanCommand;
import net.kardexo.bot.services.commands.impl.BingoCommand;
import net.kardexo.bot.services.commands.impl.BotCommand;
import net.kardexo.bot.services.commands.impl.CalculateCommand;
import net.kardexo.bot.services.commands.impl.ExitCommand;
import net.kardexo.bot.services.commands.impl.GambleCommand;
import net.kardexo.bot.services.commands.impl.HelpCommand;
import net.kardexo.bot.services.commands.impl.KickAllCommand;
import net.kardexo.bot.services.commands.impl.KickCommand;
import net.kardexo.bot.services.commands.impl.LeagueOfLegendsCommand;
import net.kardexo.bot.services.commands.impl.MoveCommand;
import net.kardexo.bot.services.commands.impl.PlayCommand;
import net.kardexo.bot.services.commands.impl.RandomCommand;
import net.kardexo.bot.services.commands.impl.RulesCommand;
import net.kardexo.bot.services.commands.impl.SayCommand;
import net.kardexo.bot.services.commands.impl.SilentCommand;
import net.kardexo.bot.services.commands.impl.TeamsCommand;
import net.kardexo.bot.services.commands.impl.TextCommand;
import net.kardexo.bot.services.commands.impl.TimerCommand;
import net.kardexo.bot.services.commands.impl.TransferCommand;
import net.kardexo.bot.services.commands.impl.TwitchCommand;
import net.kardexo.bot.services.commands.impl.Watch2GetherCommand;
import net.kardexo.bot.services.commands.impl.YouTubeCommand;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.config.Config;
import net.kardexo.bot.services.api.IAPIKeyService;
import net.kardexo.bot.services.api.IEconomyService;
import net.kardexo.bot.services.api.IPermissionService;
import net.kardexo.bot.services.api.IURLMessageProcessor;
import net.kardexo.bot.services.api.IUserConfigService;

public class Commands implements ICommandRegistrar
{
	@Override
	public void register
	(
		CommandDispatcher<CommandSource> dispatcher,
		IBotClient bot,
		Config config,
		IAPIKeyService apiKeyService,
		IPermissionService permissionService,
		IEconomyService economyService,
		IUserConfigService userConfigService,
		IURLMessageProcessor urlMessageProcessor
	)
	{
		ExitCommand.register(dispatcher, permissionService);
		BotCommand.register(dispatcher);
		HelpCommand.register(dispatcher, config);
		TwitchCommand.register(dispatcher, config, apiKeyService);
		TeamsCommand.register(dispatcher);
		Watch2GetherCommand.register(dispatcher, config, apiKeyService, urlMessageProcessor);
		RandomCommand.register(dispatcher);
		MoveCommand.register(dispatcher, bot, permissionService);
		SilentCommand.register(dispatcher, permissionService);
		LeagueOfLegendsCommand.register(dispatcher, config, apiKeyService, userConfigService);
		TextCommand.register(dispatcher, config);
		KickCommand.register(dispatcher, bot, permissionService);
		KickAllCommand.register(dispatcher, permissionService);
		YouTubeCommand.register(dispatcher, config, apiKeyService);
		RulesCommand.register(dispatcher, config);
		SayCommand.register(dispatcher);
		TimerCommand.register(dispatcher);
		BingoCommand.register(dispatcher, config);
		CalculateCommand.register(dispatcher);
		BalanceCommand.register(dispatcher, bot, permissionService, economyService);
		TransferCommand.register(dispatcher, bot, economyService);
		PlayCommand.register(dispatcher);
		BanCommand.register(dispatcher, bot, permissionService);
		GambleCommand.register(dispatcher, economyService);
	}
	
	public static LiteralArgumentBuilder<CommandSource> literal(String name)
	{
		return LiteralArgumentBuilder.literal(name);
	}
	
	public static <T> RequiredArgumentBuilder<CommandSource, T> argument(String name, ArgumentType<T> type)
	{
		return RequiredArgumentBuilder.argument(name, type);
	}
}
