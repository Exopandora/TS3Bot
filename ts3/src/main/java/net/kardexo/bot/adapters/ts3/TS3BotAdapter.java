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
import net.kardexo.bot.domain.AbstractBot;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.domain.api.MessageTarget;
import net.kardexo.bot.domain.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import static net.kardexo.bot.domain.Util.OBJECT_MAPPER;

public class TS3BotAdapter extends AbstractBot
{
	private static final Logger logger = LoggerFactory.getLogger(TS3BotAdapter.class);
	
	private TS3Api api;
	private TS3Query query;
	private TS3BotClientAdapter botClient;
	
	public TS3BotAdapter(File config) throws IOException
	{
		super(OBJECT_MAPPER.readValue(config, Config.class), new Random());
	}
	
	@Override
	protected void connect()
	{
		Config config = this.getConfig();
		TS3Config ts3config = new TS3Config();
		TS3Listener ts3Listener = new TS3EventAdapter()
		{
			@Override
			public void onTextMessage(TextMessageEvent event)
			{
				IClient client = new TS3ClientAdapter(TS3BotAdapter.this.api, event.getInvokerId());
				String message = event.getMessage();
				MessageTarget target = switch(event.getTargetMode())
				{
					case CLIENT -> MessageTarget.CLIENT;
					case CHANNEL -> MessageTarget.CHANNEL;
					case SERVER -> MessageTarget.SERVER;
				};
				TS3BotAdapter.this.onMessage(client, message, target);
			}
			
			@Override
			public void onClientJoin(ClientJoinEvent event)
			{
				TS3BotAdapter.this.onClientJoin(new TS3ClientAdapter(TS3BotAdapter.this.api, event.getInvokerId()));
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
		ts3config.setReconnectStrategy(ReconnectStrategy.constantBackoff());
		ts3config.setConnectionHandler(connectionHandler);
		ts3config.setLoginCredentials(config.getLoginName(), config.getLoginPassword());
		
		this.query = new TS3Query(ts3config);
		this.query.connect();
		this.api = this.query.getApi();
		this.botClient = new TS3BotClientAdapter(this.api, this.api.whoAmI().getId());
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
		Config config = this.getConfig();
		logger.info("Connected to {}", config.getHostAddress());
		this.api.selectVirtualServerById(config.getVirtualServerId(), config.getLoginName());
		int id = this.api.whoAmI().getId();
		Channel channel = this.api.getChannelByNameExact(config.getChannelName(), true);
		
		if(channel != null)
		{
			if(this.api.getClientInfo(id).getChannelId() != channel.getId())
			{
				this.api.moveClient(id, channel.getId());
			}
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
		if(this.api != null)
		{
			this.api.logout();
		}
		
		logger.info("Logged out");
		
		if(this.query != null)
		{
			this.query.exit();
		}
		
		logger.info("TS3 query shutdown");
	}
	
	@Override
	protected IBotClient getBotClient()
	{
		return this.botClient;
	}
}
