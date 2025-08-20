package net.kardexo.bot.adapters.discord;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ConnectEvent;
import discord4j.core.event.domain.lifecycle.DisconnectEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import net.kardexo.bot.adapters.discord.channel.AbstractDiscordChannelAdapter;
import net.kardexo.bot.adapters.discord.channel.DiscordConsoleChannelAdapter;
import net.kardexo.bot.domain.AbstractBot;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.api.IChannel;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.domain.api.IConsoleChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class DiscordBotAdapter extends AbstractBot<DiscordConfigAdapter>
{
	private static final Logger logger = LoggerFactory.getLogger(DiscordBotAdapter.class);
	
	private GatewayDiscordClient gatewayDiscordClient;
	private DiscordBotClientAdapter botClient;
	
	public DiscordBotAdapter(String configFile) throws IOException
	{
		super(configFile, DiscordConfigAdapter::of, new Random());
	}
	
	@Override
	protected void connect()
	{
		DiscordConfigAdapter config = this.getConfig();
		DiscordClient discordClient = DiscordClient.create(config.getToken());
		this.gatewayDiscordClient = discordClient.login().block();
		
		if(this.gatewayDiscordClient == null)
		{
			throw new RuntimeException("Could not login");
		}
		
		this.botClient = new DiscordBotClientAdapter(this.gatewayDiscordClient);
		this.gatewayDiscordClient.on(MessageCreateEvent.class).subscribe(event ->
		{
			if(event.getMessage().getAuthor().isEmpty())
			{
				return;
			}
			
			IClient client = new DiscordClientAdapter(event.getMessage().getAuthor().get());
			String message = event.getMessage().getContent();
			MessageChannel messageChannel = event.getMessage().getChannel().block();
			
			if(messageChannel == null)
			{
				return;
			}
			
			Optional<IChannel> channel = AbstractDiscordChannelAdapter.of(messageChannel);
			
			if(channel.isEmpty())
			{
				return;
			}
			
			DiscordBotAdapter.this.onMessage(channel.get(), client, message);
		});
		this.gatewayDiscordClient.on(ConnectEvent.class).subscribe(event -> this.onConnect());
		this.gatewayDiscordClient.on(DisconnectEvent.class).subscribe(event -> this.onDisconnect());
		this.onConnect();
		this.gatewayDiscordClient.onDisconnect().block();
	}
	
	@Override
	protected void onConnect()
	{
		logger.info("Connected");
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
		this.gatewayDiscordClient.logout();
		logger.info("Logged out");
		logger.info("Shutdown");
	}
	
	@Override
	protected List<String> getAllClientUidsForLoginBonus()
	{
		return Collections.emptyList();
	}
	
	@Override
	protected IBotClient getBotClient()
	{
		return this.botClient;
	}
	
	@Override
	protected IConsoleChannel getConsoleChannel()
	{
		return new DiscordConsoleChannelAdapter();
	}
}
