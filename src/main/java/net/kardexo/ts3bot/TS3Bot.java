package net.kardexo.ts3bot;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.manevolent.ts3j.api.Channel;
import com.github.manevolent.ts3j.api.Client;
import com.github.manevolent.ts3j.api.TextMessageTargetMode;
import com.github.manevolent.ts3j.command.CommandException;
import com.github.manevolent.ts3j.event.ClientJoinEvent;
import com.github.manevolent.ts3j.event.TS3Listener;
import com.github.manevolent.ts3j.event.TextMessageEvent;
import com.github.manevolent.ts3j.identity.LocalIdentity;
import com.github.manevolent.ts3j.protocol.socket.client.LocalTeamspeakClientSocket;
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
import net.kardexo.ts3bot.util.StringUtils;
import net.kardexo.ts3bot.util.TS3Utils;

public class TS3Bot implements TS3Listener
{
	public static final Random RANDOM = new Random();
	
	private static final Logger LOGGER = LogManager.getLogger(TS3Bot.class);
	private static TS3Bot instance;
	
	private int id;
	private final Config config;
	private final ChatHistory history;
	private final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<CommandSource>();
	private final List<IMessageProcessor> messageProcessors = new ArrayList<IMessageProcessor>();
	private final GameServerManager gameserverManager;
	private LocalTeamspeakClientSocket client;
	private boolean silent;
	
	public TS3Bot(Config config) throws IOException
	{
		TS3Bot.instance = this;
		this.config = config;
		this.history = new ChatHistory(this.config.getChatHistorySize());
		this.gameserverManager = new GameServerManager(this.config.getGameservers());
	}
	
	public void start() throws InterruptedException, IOException, TimeoutException, CommandException
	{
		Runtime.getRuntime().addShutdownHook(new Thread(this::exit));
		
		this.registerCommands();
		this.registerMessageProcessors();
		
		this.gameserverManager.start();
		
		this.client = new LocalTeamspeakClientSocket();
		this.client.setIdentity(LocalIdentity.read(new File(this.config.getIdentity())));
		this.client.addListener(this);
		this.client.setNickname(this.config.getNickname());
		
		while(!this.connect())
		{
			Thread.sleep(10000);
		}
		
		TS3Bot.LOGGER.info("Connected to " + this.config.getHostAddress());
		
		this.id = this.client.getClientId();
		this.client.subscribeAll();
		
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
			InetSocketAddress address = new InetSocketAddress(InetAddress.getByName(this.config.getHostAddress()), this.config.getHostPort());
			this.client.connect(address, StringUtils.emptyToNull(this.config.getServerPassword()), 10000L);
			return true;
		}
		catch(Exception e)
		{
			TS3Bot.LOGGER.error("Connection failed");
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
		
		Optional<Client> client = TS3Utils.findClientById(event.getInvokerId());
		
		if(client.isEmpty())
		{
			return;
		}
		
		StringReader reader = new StringReader(message);
		
		if(reader.canRead() && reader.peek() == '!')
		{
			reader.skip();
			
			CommandSource source = new CommandSource(client.get(), event.getTargetMode());
			
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
						TS3Utils.sendMessage(event.getTargetMode(), client.get(), e.getMessage());
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
						
						TS3Utils.sendMessage(event.getTargetMode(), client.get(), "Usage: !" + command + builder.toString());
					}
				}
				else
				{
					TS3Utils.sendMessage(event.getTargetMode(), client.get(), e.getMessage());
				}
			}
		}
		else if(event.getTargetMode().equals(TextMessageTargetMode.CHANNEL))
		{
			if(this.history.appendAndCheckIfNew(reader.getString(), 10000))
			{
				for(IMessageProcessor processor : this.messageProcessors)
				{
					if(processor.onMessage(reader.getString(), client.get(), event.getTargetMode()))
					{
						break;
					}
				}
			}
		}
		
		TS3Utils.sendMessage(event.getTargetMode(), client.get(), event.getMessage());
	}
	
	@Override
	public void onClientJoin(ClientJoinEvent event)
	{
		Optional<Channel> channel = TS3Utils.findChannelByName(this.config.getChannelName());
		
		if(channel.isPresent())
		{
			TS3Utils.executeSilently(() -> TS3Utils.moveClient(this.getId(), channel.get().getId(), this.config.getChannelPassword()));
		}
		else
		{
			TS3Bot.LOGGER.error("Could not find channel " + this.config.getChannelName());
		}
	}
	
	public void exit()
	{
		if(this.client != null)
		{
			try
			{
				this.client.disconnect();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
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
	
	public LocalTeamspeakClientSocket getClient()
	{
		return this.client;
	}
	
	public int getId()
	{
		return this.id;
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
