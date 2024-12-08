package net.kardexo.ts3bot;

import com.fasterxml.jackson.databind.JsonNode;
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
import com.github.theholywaffle.teamspeak3.api.wrapper.ClientInfo;
import com.github.theholywaffle.teamspeak3.api.wrapper.Wrapper;
import net.kardexo.ts3bot.config.Config;
import net.kardexo.ts3bot.message.CommandMessageProcessor;
import net.kardexo.ts3bot.message.IMessageProcessor;
import net.kardexo.ts3bot.message.URLMessageProcessor;
import net.kardexo.ts3bot.services.APIKeyService;
import net.kardexo.ts3bot.services.BonusService;
import net.kardexo.ts3bot.util.ChatHistory;
import net.kardexo.ts3bot.services.EconomyService;
import net.kardexo.ts3bot.util.PermissionProvider;
import net.kardexo.ts3bot.services.UserConfigService;
import net.kardexo.ts3bot.services.UserConfigService.UserConfig;
import net.kardexo.ts3bot.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class TS3Bot extends TS3EventAdapter implements ConnectionHandler, PermissionProvider
{
	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.246";
	public static final Random RANDOM = new Random();
	public static final Logger LOGGER = LogManager.getLogger(TS3Bot.class);
	
	private static TS3Bot instance;
	
	private int id;
	private final Config config;
	private final ChatHistory history;
	private final List<IMessageProcessor> messageProcessors = List.of(new CommandMessageProcessor(this), new URLMessageProcessor());
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final EconomyService economyService = new EconomyService(Util.createFile("coins.json"), this.objectMapper);
	private final BonusService loginBonusService = new BonusService(Util.createFile("claims.json"), this.objectMapper, this::loginBonus);
	private final UserConfigService userConfigService = new UserConfigService(Util.createFile("userconfig.json"), this.objectMapper);
	private final APIKeyService apiKeyService;
	private Timer timer;
	private TS3Api api;
	private TS3Query query;
	private boolean silent;
	
	public TS3Bot(File config) throws IOException
	{
		TS3Bot.instance = this;
		this.config = this.objectMapper.readValue(config, Config.class);
		this.history = new ChatHistory(this.config.getChatHistorySize());
		this.apiKeyService = new APIKeyService(this.config.getApiKeys());
	}
	
	public void start()
	{
		Runtime.getRuntime().addShutdownHook(new Thread(this::exit));
		
		TS3Config config = new TS3Config();
		config.setHost(this.config.getHostAddress());
		config.setReconnectStrategy(ReconnectStrategy.constantBackoff());
		config.setConnectionHandler(this);
		
		this.query = new TS3Query(config);
		this.query.connect();
		
		try(Scanner scanner = new Scanner(System.in))
		{
			while(scanner.hasNextLine())
			{
				Map<String, String> map = new HashMap<String, String>();
				map.put("msg", scanner.nextLine().replaceFirst("^!*", "!"));
				map.put("invokerid", String.valueOf(-1));
				this.onTextMessage(new TextMessageEvent(new Wrapper(map)));
			}
		}
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
	public void onConnect(TS3Api api)
	{
		TS3Bot.LOGGER.info("Connected to " + this.config.getHostAddress());
		
		this.api = api;
		
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
			
			this.loginBonusService.claim(this.getAllClientUids());
			this.timer = new Timer();
			this.timer.schedule(this.loginBonusService.createTimerTask(this::getAllClientUids), Util.tomorrow(), TimeUnit.DAYS.toMillis(1));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void onClientJoin(ClientJoinEvent event)
	{
		this.loginBonusService.claim(event.getUniqueClientIdentifier());
	}
	
	@Override
	public void onDisconnect(TS3Query ts3Query)
	{
		this.timer.cancel();
	}
	
	@Override
	public boolean hasPermission(ClientInfo clientInfo, String permission)
	{
		if(clientInfo.getId() == this.id)
		{
			return true;
		}
		
		JsonNode group = this.getConfig().getPermissions().get(permission);
		
		if(group != null)
		{
			String uid = clientInfo.getUniqueIdentifier();
			
			for(JsonNode jsonNode : group)
			{
				if(jsonNode.asText().equals(uid))
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	public void loginBonus(String user)
	{
		this.economyService.add(user, this.config.getLoginBonus());
	}
	
	public UserConfig getUserConfig(String user)
	{
		return this.userConfigService.getUserConfig(user);
	}
	
	public void saveUserConfig(String user, UserConfig config)
	{
		this.userConfigService.saveUserConfig(user, config);
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
	
	public APIKeyService getApiKeyManager()
	{
		return this.apiKeyService;
	}
	
	public void setSilent(boolean silent)
	{
		this.silent = silent;
	}
	
	public boolean isSilent()
	{
		return this.silent;
	}
	
	public ChatHistory getChatHistory()
	{
		return this.history;
	}
	
	public EconomyService getEconomyService()
	{
		return this.economyService;
	}
	
	public static TS3Bot getInstance()
	{
		return TS3Bot.instance;
	}
}
