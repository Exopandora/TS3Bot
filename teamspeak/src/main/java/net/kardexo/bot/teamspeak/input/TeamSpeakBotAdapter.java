package net.kardexo.bot.teamspeak.input;

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
import net.kardexo.bot.domain.channel.IChannel;
import net.kardexo.bot.domain.channel.IConsoleChannel;
import net.kardexo.bot.domain.client.IBotClient;
import net.kardexo.bot.domain.client.IClient;
import net.kardexo.bot.input.AbstractBot;
import net.kardexo.bot.teamspeak.domain.channel.TeamSpeakConsoleChannelAdapter;
import net.kardexo.bot.teamspeak.domain.channel.TeamSpeakMessageChannelAdapter;
import net.kardexo.bot.teamspeak.domain.channel.TeamSpeakPrivateChannelAdapter;
import net.kardexo.bot.teamspeak.domain.channel.TeamSpeakServerChannelAdapter;
import net.kardexo.bot.teamspeak.domain.client.TeamSpeakBotClientAdapter;
import net.kardexo.bot.teamspeak.domain.client.TeamSpeakClientAdapter;
import net.kardexo.bot.teamspeak.domain.config.TeamSpeakConfigAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class TeamSpeakBotAdapter extends AbstractBot<TeamSpeakConfigAdapter>
{
	private static final Logger logger = LoggerFactory.getLogger(TeamSpeakBotAdapter.class);
	
	private TS3Api api;
	private TS3Query query;
	private TeamSpeakBotClientAdapter botClient;
	
	public TeamSpeakBotAdapter(String configFile) throws IOException
	{
		super(configFile, TeamSpeakConfigAdapter::of, new Random());
	}
	
	@Override
	protected void connect()
	{
		TeamSpeakConfigAdapter config = this.getConfig();
		TS3Config ts3config = new TS3Config();
		TS3Listener ts3Listener = new TS3EventAdapter()
		{
			@Override
			public void onTextMessage(TextMessageEvent event)
			{
				IClient client = new TeamSpeakClientAdapter(TeamSpeakBotAdapter.this.api, event.getInvokerId());
				String message = event.getMessage();
				IChannel channel = switch(event.getTargetMode())
				{
					case CLIENT -> new TeamSpeakPrivateChannelAdapter(TeamSpeakBotAdapter.this.api, event.getInvokerId());
					case CHANNEL -> new TeamSpeakMessageChannelAdapter(TeamSpeakBotAdapter.this.api, TeamSpeakBotAdapter.this.botClient.getChannelId());
					case SERVER -> new TeamSpeakServerChannelAdapter(TeamSpeakBotAdapter.this.api);
				};
				TeamSpeakBotAdapter.this.onMessage(channel, client, message);
			}
			
			@Override
			public void onClientJoin(ClientJoinEvent event)
			{
				TeamSpeakBotAdapter.this.onClientJoin(new TeamSpeakClientAdapter(TeamSpeakBotAdapter.this.api, event.getClientId()));
			}
		};
		ConnectionHandler connectionHandler = new ConnectionHandler()
		{
			@Override
			public void onConnect(TS3Api api)
			{
				if(TeamSpeakBotAdapter.this.api != null)
				{
					TeamSpeakBotAdapter.this.onConnect();
				}
			}
			
			@Override
			public void onDisconnect(TS3Query ts3Query)
			{
				TeamSpeakBotAdapter.this.onDisconnect();
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
		TeamSpeakConfigAdapter config = this.getConfig();
		logger.info("Connected to {}", config.getHostAddress());
		this.api.selectVirtualServerById(config.getVirtualServerId(), config.getLoginName());
		int id = this.api.whoAmI().getId();
		this.botClient = new TeamSpeakBotClientAdapter(this.api, id);
		Channel channel = this.api.getChannelByNameExact(config.getChannelName(), true);
		
		if(channel != null)
		{
			this.botClient.move(this.botClient, new TeamSpeakMessageChannelAdapter(this.api, channel.getId()));
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
		return new TeamSpeakConsoleChannelAdapter(this.api, this.botClient.getClientId());
	}
}
