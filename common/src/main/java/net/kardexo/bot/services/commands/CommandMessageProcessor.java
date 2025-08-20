package net.kardexo.bot.services.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.kardexo.bot.domain.CommandSource;
import net.kardexo.bot.domain.api.IConfigFactory;
import net.kardexo.bot.services.api.IConfigService;
import net.kardexo.bot.services.api.commands.ICommandRegistrar;
import net.kardexo.bot.services.commands.impl.HelpCommand;
import net.kardexo.bot.domain.ChatHistory;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.api.IChannel;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.domain.api.IConsoleChannel;
import net.kardexo.bot.domain.config.Config;
import net.kardexo.bot.services.api.IAPIKeyService;
import net.kardexo.bot.services.api.IEconomyService;
import net.kardexo.bot.services.api.IMessageProcessor;
import net.kardexo.bot.services.api.IPermissionService;
import net.kardexo.bot.services.api.IURLMessageProcessor;
import net.kardexo.bot.services.api.IUserConfigService;

import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommandMessageProcessor implements IMessageProcessor
{
	private final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<CommandSource>();
	private final IPermissionService permissionService;
	private final Config config;
	private final Random random;
	
	public CommandMessageProcessor
	(
		IBotClient bot,
		IConfigService<? extends Config> configService,
		IAPIKeyService apiKeyService,
		IPermissionService permissionService,
		IEconomyService economyService,
		IUserConfigService userConfigService,
		IURLMessageProcessor urlMessageProcessor,
		Random random
	)
	{
		this.permissionService = permissionService;
		this.config = configService.getConfig();
		this.random = random;
		ICommandRegistrar.INSTANCE.forEach(registrar ->
		{
			registrar.register(this.dispatcher, bot, configService, apiKeyService, permissionService, economyService, userConfigService, urlMessageProcessor);
		});
	}
	
	@Override
	public void process(IBotClient bot, IChannel channel, IClient client, String message, ChatHistory chatHistory)
	{
		CommandSource source = new CommandSource(bot, channel, client, chatHistory, this.random);
		
		if(bot.isSilent() && (!this.permissionService.hasPermission(client, "admin") || !(channel instanceof IConsoleChannel)))
		{
			return;
		}
		
		ParseResults<CommandSource> parse = this.dispatcher.parse(message.substring(this.config.getCommandPrefix().length()), source);
		
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
					source.sendFeedback("Usage: " + this.config.getCommandPrefix() + command + builder);
				}
			}
			else
			{
				source.sendFeedback(e.getRawMessage().getString());
			}
		}
	}
	
	@Override
	public boolean isApplicable(IBotClient bot, IChannel channel, IClient client, String message, ChatHistory chatHistory)
	{
		return message.startsWith(this.config.getCommandPrefix()) &&
			(this.config.getCommandPrefix().length() > 1 || !message.matches("^(?:" + Pattern.quote(this.config.getCommandPrefix()) + ")+$"));
	}
}
