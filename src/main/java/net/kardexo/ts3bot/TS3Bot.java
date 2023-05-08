package net.kardexo.ts3bot;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.event.ClientJoinEvent;
import com.github.theholywaffle.teamspeak3.api.event.TS3EventAdapter;
import com.github.theholywaffle.teamspeak3.api.event.TS3EventType;
import com.github.theholywaffle.teamspeak3.api.event.TextMessageEvent;
import com.github.theholywaffle.teamspeak3.api.reconnect.ConnectionHandler;
import com.github.theholywaffle.teamspeak3.api.reconnect.ReconnectStrategy;
import com.github.theholywaffle.teamspeak3.api.wrapper.Channel;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.mojang.brigadier.CommandDispatcher;

import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.impl.BalanceCommand;
import net.kardexo.ts3bot.commands.impl.BanCommand;
import net.kardexo.ts3bot.commands.impl.BingoCommand;
import net.kardexo.ts3bot.commands.impl.BotCommand;
import net.kardexo.ts3bot.commands.impl.CalculateCommand;
import net.kardexo.ts3bot.commands.impl.ExitCommand;
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
import net.kardexo.ts3bot.config.Config;
import net.kardexo.ts3bot.message.CommandMessageProcressor;
import net.kardexo.ts3bot.message.IMessageProcessor;
import net.kardexo.ts3bot.message.URLMessageProcessor;
import net.kardexo.ts3bot.util.APIKeyManager;
import net.kardexo.ts3bot.util.BonusManager;
import net.kardexo.ts3bot.util.ChatHistory;
import net.kardexo.ts3bot.util.CoinManager;
import net.kardexo.ts3bot.util.UserConfigManager;
import net.kardexo.ts3bot.util.UserConfigManager.UserConfig;
import net.kardexo.ts3bot.util.Util;

public class TS3Bot extends TS3EventAdapter implements ConnectionHandler
{
	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.246";
	public static final String API_KEY_WATCH_2_GETHER = "watch_2_gether";
	public static final String API_KEY_TWITCH = "twitch";
	public static final String API_KEY_YOUTUBE = "youtube";
	public static final String API_KEY_TWITTER = "twitter";
	public static final String API_KEY_LEAGUE_OF_LEGENDS = "league_of_legends";
	public static final String API_KEY_IMAGGA = "imagga";
	public static final Random RANDOM = new Random();
	public static final Logger LOGGER = LogManager.getLogger(TS3Bot.class);
	
	private static TS3Bot instance;
	
	private int id;
	private final Config config;
	private final ChatHistory history;
	private final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<CommandSource>();
	private final List<IMessageProcessor> messageProcessors = List.of(new CommandMessageProcressor(), new URLMessageProcessor());
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final CoinManager coinManager = new CoinManager(Util.createFile("coins.json"), this.objectMapper);
	private final BonusManager loginBonusManager = new BonusManager(Util.createFile("claims.json"), this.objectMapper, this::loginBonus);
	private final UserConfigManager userConfigManager = new UserConfigManager(Util.createFile("userconfig.json"), this.objectMapper);
	private final APIKeyManager apiKeyManager;
	private Timer timer;
	private TS3Api api;
	private TS3Query query;
	private boolean silent;
	
	public TS3Bot(File config) throws IOException
	{
		TS3Bot.instance = this;
		this.config = this.objectMapper.readValue(config, Config.class);
		this.history = new ChatHistory(this.config.getChatHistorySize());
		this.apiKeyManager = new APIKeyManager(this.config.getApiKeys());
	}
	
	public void start() throws InterruptedException
	{
		Runtime.getRuntime().addShutdownHook(new Thread(this::exit));
		
		TS3Config config = new TS3Config();
		config.setHost(this.config.getHostAddress());
		config.setReconnectStrategy(ReconnectStrategy.constantBackoff());
		config.setConnectionHandler(this);
		
		this.registerCommands();
		this.query = new TS3Query(config);
		this.query.connect();
		
		try(Scanner scanner = new Scanner(System.in))
		{
			while(scanner.hasNextLine())
			{
				Map<String, String> map = new HashMap<String, String>();
				map.put("msg", scanner.nextLine().replaceFirst("^!*", "!"));
				map.put("invokerid", String.valueOf(-1));
				this.onTextMessage(new TextMessageEvent(map));
			}
		}
	}
	
	private void registerCommands()
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
		KickCommand.register(this.dispatcher, this);
		KickAllCommand.register(this.dispatcher);
		YouTubeCommand.register(this.dispatcher);
		RulesCommand.register(this.dispatcher);
		SayCommand.register(this.dispatcher);
		TimerCommand.register(this.dispatcher);
		BingoCommand.register(this.dispatcher);
		CalculateCommand.register(this.dispatcher);
		BalanceCommand.register(this.dispatcher, this);
		TransferCommand.register(this.dispatcher, this);
		PlayCommand.register(this.dispatcher);
		BanCommand.register(this.dispatcher, this);
	}
	
	@Override
	public void onTextMessage(TextMessageEvent event)
	{
		if(this.api == null)
		{
			return;
		}
		
		if(event.getInvokerId() == this.id)
		{
			return;
		}
		
		String message = event.getMessage().strip();
		
		if(message.isEmpty())
		{
			return;
		}
		
		for(IMessageProcessor processor : this.messageProcessors)
		{
			if(processor.isApplicable(this, message, event.getInvokerId(), event.getTargetMode()))
			{
				processor.process(this, message, event.getInvokerId(), event.getTargetMode());
			}
		}
	}
	
	@Override
	public void onConnect(TS3Query ts3Query)
	{
		TS3Bot.LOGGER.info("Connected to " + this.config.getHostAddress());
		
		this.api = ts3Query.getApi();
		
		try
		{
			this.api.login(this.config.getLoginName(), this.config.getLoginPassword());
			this.api.selectVirtualServerById(this.config.getVirtualServerId(), this.config.getLoginName());
			
			this.id = this.api.whoAmI().getId();
			
			Channel channel = this.api.getChannelByNameExact(this.config.getChannelName(), true);
			
			if(channel != null)
			{
				if(this.api.getClientInfo(this.id).getChannelId() != channel.getId())
				{
					this.api.moveClient(this.id, channel.getId());
				}
			}
			else
			{
				TS3Bot.LOGGER.error("Channel " + this.config.getChannelName() + " does not exist");
			}
			
			this.api.registerEvent(TS3EventType.TEXT_CHANNEL);
			this.api.registerEvent(TS3EventType.TEXT_PRIVATE);
			this.api.registerEvent(TS3EventType.TEXT_SERVER);
			this.api.registerEvent(TS3EventType.CHANNEL);
			this.api.addTS3Listeners(this);
			
			TS3Bot.LOGGER.info("Logged in as " + this.config.getLoginName());
			
			this.loginBonusManager.claim(this.getAllClientUids());
			this.timer = new Timer();
			this.timer.schedule(this.loginBonusManager.createTimerTask(this::getAllClientUids), Util.tomorrow(), TimeUnit.DAYS.toMillis(1));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void onClientJoin(ClientJoinEvent event)
	{
		this.loginBonusManager.claim(event.getUniqueClientIdentifier());
	}
	
	@Override
	public void onDisconnect(TS3Query ts3Query)
	{
		this.timer.cancel();
	}
	
	public void loginBonus(String user)
	{
		this.coinManager.add(user, this.config.getLoginBonus());
	}
	
	public UserConfig getUserConfig(String user)
	{
		return this.userConfigManager.getUserConfig(user);
	}
	
	public void saveUserConfig(String user, UserConfig config)
	{
		this.userConfigManager.saveUserConfig(user, config);
	}
	
	public void exit()
	{
		if(this.api != null)
		{
			this.api.logout();
		}
		
		TS3Bot.LOGGER.info("Logged out");
		
		if(this.query != null)
		{
			this.query.exit();
		}
		
		TS3Bot.LOGGER.info("Disconnected");
	}
	
	public List<String> getAllClientUids()
	{
		return this.api.getClients().stream().map(Client::getUniqueIdentifier).toList();
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
	
	public APIKeyManager getApiKeyManager()
	{
		return this.apiKeyManager;
	}
	
	public void setSilent(boolean silent)
	{
		this.silent = silent;
	}
	
	public boolean isSilent()
	{
		return this.silent;
	}
	
	public CommandDispatcher<CommandSource> getCommandDispatcher()
	{
		return this.dispatcher;
	}
	
	public ChatHistory getChatHistory()
	{
		return this.history;
	}
	
	public CoinManager getCoinManager()
	{
		return this.coinManager;
	}
	
	public static TS3Bot getInstance()
	{
		return TS3Bot.instance;
	}
}
