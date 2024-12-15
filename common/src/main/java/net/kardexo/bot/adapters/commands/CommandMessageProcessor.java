package net.kardexo.bot.adapters.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.kardexo.bot.adapters.commands.impl.BalanceCommand;
import net.kardexo.bot.adapters.commands.impl.BanCommand;
import net.kardexo.bot.adapters.commands.impl.BingoCommand;
import net.kardexo.bot.adapters.commands.impl.BotCommand;
import net.kardexo.bot.adapters.commands.impl.CalculateCommand;
import net.kardexo.bot.adapters.commands.impl.ExitCommand;
import net.kardexo.bot.adapters.commands.impl.GambleCommand;
import net.kardexo.bot.adapters.commands.impl.HelpCommand;
import net.kardexo.bot.adapters.commands.impl.KickAllCommand;
import net.kardexo.bot.adapters.commands.impl.KickCommand;
import net.kardexo.bot.adapters.commands.impl.LeagueOfLegendsCommand;
import net.kardexo.bot.adapters.commands.impl.MoveCommand;
import net.kardexo.bot.adapters.commands.impl.PlayCommand;
import net.kardexo.bot.adapters.commands.impl.RandomCommand;
import net.kardexo.bot.adapters.commands.impl.RulesCommand;
import net.kardexo.bot.adapters.commands.impl.SayCommand;
import net.kardexo.bot.adapters.commands.impl.SilentCommand;
import net.kardexo.bot.adapters.commands.impl.TeamsCommand;
import net.kardexo.bot.adapters.commands.impl.TextCommand;
import net.kardexo.bot.adapters.commands.impl.TimerCommand;
import net.kardexo.bot.adapters.commands.impl.TransferCommand;
import net.kardexo.bot.adapters.commands.impl.TwitchCommand;
import net.kardexo.bot.adapters.commands.impl.Watch2GetherCommand;
import net.kardexo.bot.adapters.commands.impl.YouTubeCommand;
import net.kardexo.bot.adapters.web.URLMessageProcessor;
import net.kardexo.bot.domain.ChatHistory;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.domain.api.MessageTarget;
import net.kardexo.bot.domain.config.Config;
import net.kardexo.bot.services.api.IAPIKeyService;
import net.kardexo.bot.services.api.IEconomyService;
import net.kardexo.bot.services.api.IMessageProcessor;
import net.kardexo.bot.services.api.IPermissionService;
import net.kardexo.bot.services.api.IUserConfigService;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class CommandMessageProcessor implements IMessageProcessor
{
	private final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<CommandSource>();
	private final IPermissionService permissionService;
	private final Random random;
	
	public CommandMessageProcessor
	(
		IBotClient bot,
		Config config,
		IAPIKeyService apiKeyService,
		IPermissionService permissionService,
		IEconomyService economyService,
		IUserConfigService userConfigService,
		URLMessageProcessor urlMessageProcessor,
		Random random
	)
	{
		this.permissionService = permissionService;
		this.random = random;
		ExitCommand.register(this.dispatcher, permissionService);
		BotCommand.register(this.dispatcher);
		HelpCommand.register(this.dispatcher);
		TwitchCommand.register(this.dispatcher, config, apiKeyService);
		TeamsCommand.register(this.dispatcher);
		Watch2GetherCommand.register(this.dispatcher, config, apiKeyService, urlMessageProcessor);
		RandomCommand.register(this.dispatcher);
		MoveCommand.register(this.dispatcher, bot, permissionService);
		SilentCommand.register(this.dispatcher, permissionService);
		LeagueOfLegendsCommand.register(this.dispatcher, config, apiKeyService, userConfigService);
		TextCommand.register(this.dispatcher, config);
		KickCommand.register(this.dispatcher, bot, permissionService);
		KickAllCommand.register(this.dispatcher, permissionService);
		YouTubeCommand.register(this.dispatcher, config, apiKeyService);
		RulesCommand.register(this.dispatcher, config);
		SayCommand.register(this.dispatcher);
		TimerCommand.register(this.dispatcher);
		BingoCommand.register(this.dispatcher, config);
		CalculateCommand.register(this.dispatcher);
		BalanceCommand.register(this.dispatcher, bot, permissionService, economyService);
		TransferCommand.register(this.dispatcher, bot, economyService);
		PlayCommand.register(this.dispatcher);
		BanCommand.register(this.dispatcher, bot, permissionService);
		GambleCommand.register(this.dispatcher, economyService);
	}
	
	@Override
	public void process(IBotClient bot, String message, IClient client, MessageTarget target, ChatHistory chatHistory)
	{
		CommandSource source = new CommandSource(bot, client, target, chatHistory, this.random);
		
		if(bot.isSilent() && (!this.permissionService.hasPermission(client, "admin") || target != MessageTarget.CONSOLE))
		{
			return;
		}
		
		ParseResults<CommandSource> parse = this.dispatcher.parse(message.substring(1), source);
		
		try
		{
			if(parse.getReader().canRead())
			{
				if(parse.getExceptions().size() == 1)
				{
					throw parse.getExceptions().values().iterator().next();
				}
				else if(parse.getContext().getRange().isEmpty())
				{
					throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(parse.getReader());
				}
			}
			
			this.dispatcher.execute(parse);
		}
		catch(CommandSyntaxException e)
		{
			if(e.getCursor() != -1)
			{
				List<CommandNode<CommandSource>> nodes = parse.getContext().getLastChild().getNodes().stream()
					.map(ParsedCommandNode::getNode)
					.toList();
				
				if(nodes.isEmpty())
				{
					source.sendFeedback(e.getRawMessage().getString());
				}
				else
				{
					StringBuilder builder = new StringBuilder();
					String command = nodes.stream().map(CommandNode::getName).collect(Collectors.joining(" "));
					HelpCommand.appendAllUsage(builder, this.dispatcher, nodes, source);
					source.sendFeedback("Usage: !" + command + builder);
				}
			}
			else
			{
				source.sendFeedback(e.getRawMessage().getString());
			}
		}
	}
	
	@Override
	public boolean isApplicable(IBotClient bot, String message, IClient client, MessageTarget target, ChatHistory chatHistory)
	{
		return message.startsWith("!") && !message.matches("!+");
	}
}
