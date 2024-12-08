package net.kardexo.ts3bot.message;

import com.github.theholywaffle.teamspeak3.api.TextMessageTargetMode;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.impl.BalanceCommand;
import net.kardexo.ts3bot.commands.impl.BanCommand;
import net.kardexo.ts3bot.commands.impl.BingoCommand;
import net.kardexo.ts3bot.commands.impl.BotCommand;
import net.kardexo.ts3bot.commands.impl.CalculateCommand;
import net.kardexo.ts3bot.commands.impl.ExitCommand;
import net.kardexo.ts3bot.commands.impl.GambleCommand;
import net.kardexo.ts3bot.commands.impl.HelpCommand;
import net.kardexo.ts3bot.commands.impl.KickAllCommand;
import net.kardexo.ts3bot.commands.impl.KickCommand;
import net.kardexo.ts3bot.commands.impl.LeagueOfLegendsCommand;
import net.kardexo.ts3bot.commands.impl.MoveCommand;
import net.kardexo.ts3bot.commands.impl.PlayCommand;
import net.kardexo.ts3bot.commands.impl.RandomCommand;
import net.kardexo.ts3bot.commands.impl.RulesCommand;
import net.kardexo.ts3bot.commands.impl.SayCommand;
import net.kardexo.ts3bot.commands.impl.SilentCommand;
import net.kardexo.ts3bot.commands.impl.TeamsCommand;
import net.kardexo.ts3bot.commands.impl.TextCommand;
import net.kardexo.ts3bot.commands.impl.TimerCommand;
import net.kardexo.ts3bot.commands.impl.TransferCommand;
import net.kardexo.ts3bot.commands.impl.TwitchCommand;
import net.kardexo.ts3bot.commands.impl.Watch2GetherCommand;
import net.kardexo.ts3bot.commands.impl.YouTubeCommand;

import java.util.List;
import java.util.stream.Collectors;

public class CommandMessageProcessor implements IMessageProcessor
{
	private final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<CommandSource>();
	
	public CommandMessageProcessor(TS3Bot bot)
	{
		ExitCommand.register(this.dispatcher);
		BotCommand.register(this.dispatcher);
		HelpCommand.register(this.dispatcher);
		TwitchCommand.register(this.dispatcher);
		TeamsCommand.register(this.dispatcher);
		Watch2GetherCommand.register(this.dispatcher);
		RandomCommand.register(this.dispatcher);
		MoveCommand.register(this.dispatcher);
		SilentCommand.register(this.dispatcher);
		LeagueOfLegendsCommand.register(this.dispatcher);
		TextCommand.register(this.dispatcher);
		KickCommand.register(this.dispatcher, bot);
		KickAllCommand.register(this.dispatcher);
		YouTubeCommand.register(this.dispatcher);
		RulesCommand.register(this.dispatcher);
		SayCommand.register(this.dispatcher);
		TimerCommand.register(this.dispatcher);
		BingoCommand.register(this.dispatcher);
		CalculateCommand.register(this.dispatcher);
		BalanceCommand.register(this.dispatcher, bot);
		TransferCommand.register(this.dispatcher, bot);
		PlayCommand.register(this.dispatcher);
		BanCommand.register(this.dispatcher, bot);
		GambleCommand.register(this.dispatcher);
	}
	
	@Override
	public void process(TS3Bot bot, String message, int invokerId, TextMessageTargetMode targetMode)
	{
		CommandSource source = new CommandSource(bot, bot.getApi().getClientInfo(invokerId == -1 ? bot.getId() : invokerId), targetMode);
		
		if(bot.isSilent() && (!source.hasPermission("admin") || invokerId != -1))
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
					HelpCommand.appendAllUsage(builder, this.dispatcher, nodes, source, true);
					source.sendFeedback("Usage: !" + command + builder.toString());
				}
			}
			else
			{
				source.sendFeedback(e.getRawMessage().getString());
			}
		}
	}
	
	@Override
	public boolean isApplicable(TS3Bot bot, String message, int invokerId, TextMessageTargetMode targetMode)
	{
		return message.startsWith("!") && !message.matches("!+");
	}
}
