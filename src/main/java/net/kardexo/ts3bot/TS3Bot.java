package net.kardexo.ts3bot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.TextMessageTargetMode;
import com.github.theholywaffle.teamspeak3.api.event.TS3EventAdapter;
import com.github.theholywaffle.teamspeak3.api.event.TS3EventType;
import com.github.theholywaffle.teamspeak3.api.event.TextMessageEvent;
import com.github.theholywaffle.teamspeak3.api.wrapper.ClientInfo;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;

import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.impl.CommandAmouranth;
import net.kardexo.ts3bot.commands.impl.CommandBobRoss;
import net.kardexo.ts3bot.commands.impl.CommandBot;
import net.kardexo.ts3bot.commands.impl.CommandCoinflip;
import net.kardexo.ts3bot.commands.impl.CommandExit;
import net.kardexo.ts3bot.commands.impl.CommandGameServers;
import net.kardexo.ts3bot.commands.impl.CommandHeldDerSteine;
import net.kardexo.ts3bot.commands.impl.CommandHelp;
import net.kardexo.ts3bot.commands.impl.CommandKick;
import net.kardexo.ts3bot.commands.impl.CommandKickAll;
import net.kardexo.ts3bot.commands.impl.CommandLeagueOfLegends;
import net.kardexo.ts3bot.commands.impl.CommandMove;
import net.kardexo.ts3bot.commands.impl.CommandRandom;
import net.kardexo.ts3bot.commands.impl.CommandSilent;
import net.kardexo.ts3bot.commands.impl.CommandTeams;
import net.kardexo.ts3bot.commands.impl.CommandWatch2Gether;
import net.kardexo.ts3bot.config.Config;
import net.kardexo.ts3bot.gameservers.GameServerManager;
import net.kardexo.ts3bot.processors.message.IMessageProcessor;
import net.kardexo.ts3bot.processors.message.impl.URLProcessor;
import net.kardexo.ts3bot.util.ChatHistory;

public class TS3Bot extends TS3EventAdapter
{
	public static final Random RANDOM = new Random();
	
	private static final Logger LOGGER = LogManager.getLogger(TS3Bot.class);
	private static TS3Bot instance;
	
	private int id;
	private final Config config;
	private final ChatHistory history;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<CommandSource>();
	private final List<IMessageProcessor> messageProcessors = new ArrayList<IMessageProcessor>();
	private final GameServerManager gameserverManager;
	private TS3Query query;
	private TS3Api api;
	private boolean silent;
	
	public TS3Bot(File config) throws IOException
	{
		TS3Bot.instance = this;
		this.config = this.objectMapper.readValue(config, Config.class);
		this.history = new ChatHistory(this.config.getChatHistorySize());
		this.gameserverManager = new GameServerManager(this.config.getGameservers());
	}
	
	public void start() throws InterruptedException
	{
		Runtime.getRuntime().addShutdownHook(new Thread(this::exit));
		TS3Config config = new TS3Config().setHost(this.config.getHostAddress());
		
		this.query = new TS3Query(config);
		this.gameserverManager.start();
		
		while(!this.connect())
		{
			Thread.sleep(10000);
		}
		
		TS3Bot.LOGGER.info("Connected to " + this.config.getHostAddress());
		
		this.registerCommands();
		this.registerMessageProcessors();
		this.api = this.query.getApi();
		
		while(!this.login())
		{
			Thread.sleep(10000);
		}
		
		TS3Bot.LOGGER.info("Logged in as " + this.config.getLoginName());
		
		this.api.selectVirtualServerById(this.config.getVirtualServerId(), this.config.getLoginName());
		this.id = this.api.whoAmI().getId();
		this.api.moveClient(this.id, this.api.getChannelByNameExact(this.config.getChannelName(), true).getId());
		this.api.registerEvent(TS3EventType.TEXT_CHANNEL);
		this.api.registerEvent(TS3EventType.TEXT_PRIVATE);
		this.api.registerEvent(TS3EventType.TEXT_SERVER);
		this.api.addTS3Listeners(this);
		
		Scanner scanner = new Scanner(System.in);
		
		while(scanner.hasNextLine())
		{
			String line = scanner.nextLine();
			
			if(line.equals("exit"))
			{
				System.exit(0);
			}
		}
		
		scanner.close();
	}
	
	private boolean connect()
	{
		try
		{
			this.query.connect();
			return true;
		}
		catch(Exception e)
		{
			TS3Bot.LOGGER.error("Connection failed");
			return false;
		}
	}
	
	private boolean login()
	{
		try
		{
			this.api.login(this.config.getLoginName(), this.config.getLoginPassword());
			return true;
		}
		catch(Exception e)
		{
			TS3Bot.LOGGER.error("Login failed");
			return false;
		}
	}
	
	private void registerCommands()
	{
		CommandExit.register(this.dispatcher);
		CommandBot.register(this.dispatcher);
		CommandHelp.register(this.dispatcher);
		CommandAmouranth.register(this.dispatcher);
		CommandBobRoss.register(this.dispatcher);
		CommandTeams.register(this.dispatcher);
		CommandWatch2Gether.register(this.dispatcher);
		CommandRandom.register(this.dispatcher);
		CommandMove.register(this.dispatcher);
		CommandSilent.register(this.dispatcher);
		CommandLeagueOfLegends.register(this.dispatcher);
		CommandGameServers.register(this.dispatcher);
		CommandCoinflip.register(this.dispatcher);
		CommandKick.register(this.dispatcher);
		CommandKickAll.register(this.dispatcher);
		CommandHeldDerSteine.register(this.dispatcher);
	}
	
	private void registerMessageProcessors()
	{
		this.messageProcessors.add(new URLProcessor());
	}
	
	@Override
	public void onTextMessage(TextMessageEvent event)
	{
		if(event.getInvokerId() == this.id)
		{
			return;
		}
		
		String message = event.getMessage().strip();
		
		if(message.isEmpty())
		{
			return;
		}
		
		StringReader reader = new StringReader(message);
		
		if(reader.canRead() && reader.peek() == '!')
		{
			reader.skip();
			
			CommandSource source = new CommandSource(this.api, this.api.getClientInfo(event.getInvokerId()), event.getTargetMode());
			
			if(this.silent && !source.hasPermission("admin"))
			{
				return;
			}
			
			ParseResults<CommandSource> parse = this.dispatcher.parse(reader, source);
			
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
					List<ParsedCommandNode<CommandSource>> nodes = parse.getContext().getLastChild().getNodes();
					
					if(nodes.isEmpty())
					{
						this.api.sendTextMessage(event.getTargetMode(), event.getInvokerId(), e.getMessage());
					}
					else
					{
						CommandNode<CommandSource> lastNode = nodes.get(nodes.size() - 1).getNode();
						String command = nodes.stream().map(node -> node.getNode().getName()).collect(Collectors.joining(" "));
						String[] usage = this.dispatcher.getAllUsage(lastNode, source, true);
						StringBuilder builder = new StringBuilder();
						
						if(usage.length > 0 && !usage[0].isEmpty())
						{
							builder.append(" " + Arrays.toString(usage));
						}
						
						this.api.sendTextMessage(event.getTargetMode(), event.getInvokerId(), "Usage: !" + command + builder.toString());
					}
				}
				else
				{
					this.api.sendTextMessage(event.getTargetMode(), event.getInvokerId(), e.getMessage());
				}
			}
		}
		else if(event.getTargetMode().equals(TextMessageTargetMode.CHANNEL))
		{
			ClientInfo info = this.api.getClientInfo(event.getInvokerId());
			
			if(this.history.appendAndCheckIfNew(reader.getString(), 10000))
			{
				for(IMessageProcessor processor : this.messageProcessors)
				{
					if(processor.onMessage(reader.getString(), this.api, info, event.getTargetMode()))
					{
						break;
					}
				}
			}
		}
	}
	
	public void exit()
	{
		if(this.api != null)
		{
			this.api.logout();
			TS3Bot.LOGGER.info("Logged out");
		}
		
		if(this.query != null)
		{
			this.query.exit();
			TS3Bot.LOGGER.info("Disconnected");
		}
		
		if(this.gameserverManager != null)
		{
			try
			{
				this.gameserverManager.close();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public Config getConfig()
	{
		return this.config;
	}
	
	public ObjectMapper getObjectMapper()
	{
		return this.objectMapper;
	}
	
	public TS3Api getApi()
	{
		return this.api;
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public TS3Query getQuery()
	{
		return this.query;
	}
	
	public GameServerManager getGameserverManager()
	{
		return this.gameserverManager;
	}
	
	public void setSilent(boolean silent)
	{
		this.silent = silent;
	}
	
	public boolean isSilent()
	{
		return this.silent;
	}
	
	public static TS3Bot getInstance()
	{
		return TS3Bot.instance;
	}
}
