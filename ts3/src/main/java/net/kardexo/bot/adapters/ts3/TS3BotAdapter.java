package net.kardexo.bot.adapters.ts3;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.event.ClientJoinEvent;
import com.github.theholywaffle.teamspeak3.api.event.TS3EventAdapter;
import com.github.theholywaffle.teamspeak3.api.event.TS3EventType;
import com.github.theholywaffle.teamspeak3.api.event.TS3Listener;
import com.github.theholywaffle.teamspeak3.api.event.TextMessageEvent;
import com.github.theholywaffle.teamspeak3.api.reconnect.ConnectionHandler;
import com.github.theholywaffle.teamspeak3.api.reconnect.ReconnectStrategy;
import com.github.theholywaffle.teamspeak3.api.wrapper.Channel;
import net.kardexo.bot.adapters.ts3.channel.TS3ConsoleChannelAdapter;
import net.kardexo.bot.adapters.ts3.channel.TS3MessageChannelAdapter;
import net.kardexo.bot.adapters.ts3.channel.TS3PrivateChannelAdapter;
import net.kardexo.bot.adapters.ts3.channel.TS3ServerChannelAdapter;
import net.kardexo.bot.domain.AbstractBot;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.api.IChannel;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.domain.api.IConsoleChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class TS3BotAdapter extends AbstractBot<TS3ConfigAdapter>
{
	private static final Logger logger = LoggerFactory.getLogger(TS3BotAdapter.class);
	
	private TS3Api api;
	private TS3Query query;
	private TS3BotClientAdapter botClient;
	
	public TS3BotAdapter(String configFile) throws IOException
	{
		super(configFile, TS3ConfigAdapter::of, new Random());
	}
	
	@Override
	protected void connect()
	{
		TS3ConfigAdapter config = this.getConfig();
		TS3Config ts3config = new TS3Config();
		TS3Listener ts3Listener = new TS3EventAdapter()
		{
			@Override
			public void onTextMessage(TextMessageEvent event)
			{
				IClient client = new TS3ClientAdapter(TS3BotAdapter.this.api, event.getInvokerId());
				String message = event.getMessage();
				IChannel channel = switch(event.getTargetMode())
				{
					case CLIENT -> new TS3PrivateChannelAdapter(TS3BotAdapter.this.api, event.getInvokerId());
					case CHANNEL -> new TS3MessageChannelAdapter(TS3BotAdapter.this.api, TS3BotAdapter.this.botClient.getChannelId());
					case SERVER -> new TS3ServerChannelAdapter(TS3BotAdapter.this.api);
				};
				TS3BotAdapter.this.onMessage(channel, client, message);
			}
			
			@Override
			public void onClientJoin(ClientJoinEvent event)
			{
				TS3BotAdapter.this.onClientJoin(new TS3ClientAdapter(TS3BotAdapter.this.api, event.getClientId()));
			}
		};
		ConnectionHandler connectionHandler = new ConnectionHandler()
		{
			@Override
			public void onConnect(TS3Api api)
			{
				if(TS3BotAdapter.this.api != null)
				{
					TS3BotAdapter.this.onConnect();
				}
			}
			
			@Override
			public void onDisconnect(TS3Query ts3Query)
			{
				TS3BotAdapter.this.onDisconnect();
			}
		};
		ts3config.setHost(config.getHostAddress());
		ts3config.setProtocol(config.getProtocol());
		ts3config.setReconnectStrategy(ReconnectStrategy.constantBackoff());
		ts3config.setConnectionHandler(connectionHandler);
		ts3config.setLoginCredentials(config.getLoginName(), config.getLoginPassword());
		
		this.query = new TS3Query(ts3config);
		this.query.connect();
		this.api = this.query.getApi();
		this.api.selectVirtualServerById(config.getVirtualServerId(), config.getLoginName());
		this.api.registerEvent(TS3EventType.TEXT_CHANNEL);
		this.api.registerEvent(TS3EventType.TEXT_PRIVATE);
		this.api.registerEvent(TS3EventType.TEXT_SERVER);
		this.api.registerEvent(TS3EventType.CHANNEL);
		this.api.addTS3Listeners(ts3Listener);
		this.onConnect();
	}
	
	@Override
	protected void onConnect()
	{
		TS3ConfigAdapter config = this.getConfig();
		logger.info("Connected to {}", config.getHostAddress());
		this.api.selectVirtualServerById(config.getVirtualServerId(), config.getLoginName());
		int id = this.api.whoAmI().getId();
		this.botClient = new TS3BotClientAdapter(this.api, id);
		Channel channel = this.api.getChannelByNameExact(config.getChannelName(), true);
		
		if(channel != null)
		{
			this.botClient.move(this.botClient, new TS3MessageChannelAdapter(this.api, channel.getId()));
		}
		else
		{
			logger.error("Channel {} does not exist", config.getChannelName());
		}
		
		super.onConnect();
	}
	
	@Override
	protected void onDisconnect()
	{
		logger.info("Disconnected");
		super.onDisconnect();
	}
	
	public void exit()
	{
		logger.info("Logging out...");
		
		if(this.api != null)
		{
			this.api.logout();
		}
		
		logger.info("Logged out");
		logger.info("Shutting down TS3 query...");
		
		if(this.query != null)
		{
			this.query.exit();
		}
		
		logger.info("TS3 query shutdown");
	}
	
	@Override
	protected List<String> getAllClientUidsForLoginBonus()
	{
		return this.botClient.getServer().getClients().stream().map(IClient::getId).toList();
	}
	
	@Override
	protected IBotClient getBotClient()
	{
		return this.botClient;
	}
	
	@Override
	protected IConsoleChannel getConsoleChannel()
	{
		return new TS3ConsoleChannelAdapter(this.api, this.botClient.clientId);
	}
}
