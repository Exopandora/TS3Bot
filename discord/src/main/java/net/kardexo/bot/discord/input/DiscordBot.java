package net.kardexo.bot.discord.input;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ConnectEvent;
import discord4j.core.event.domain.lifecycle.DisconnectEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import net.kardexo.bot.discord.domain.channel.AbstractDiscordChannel;
import net.kardexo.bot.discord.domain.channel.DiscordConsoleChannel;
import net.kardexo.bot.discord.domain.client.DiscordBotClient;
import net.kardexo.bot.discord.domain.client.DiscordClient;
import net.kardexo.bot.discord.domain.config.DiscordConfig;
import net.kardexo.bot.domain.channel.IChannel;
import net.kardexo.bot.domain.channel.IConsoleChannel;
import net.kardexo.bot.domain.client.IBotClient;
import net.kardexo.bot.domain.client.IClient;
import net.kardexo.bot.input.AbstractBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class DiscordBot extends AbstractBot<DiscordConfig> {
	private static final Logger logger = LoggerFactory.getLogger(DiscordBot.class);
	
	private GatewayDiscordClient gatewayDiscordClient;
	private DiscordBotClient botClient;
	
	public DiscordBot(String configFile) throws IOException {
		super(configFile, DiscordConfig::of, new Random());
	}
	
	@Override
	protected void connect() {
		DiscordConfig config = this.getConfig();
		discord4j.core.DiscordClient discordClient = discord4j.core.DiscordClient.create(config.getToken());
		this.gatewayDiscordClient = discordClient.login().block();
		if (this.gatewayDiscordClient == null) {
			throw new RuntimeException("Could not login");
		}
		this.botClient = new DiscordBotClient(this.gatewayDiscordClient);
		this.gatewayDiscordClient.on(MessageCreateEvent.class).subscribe(event -> {
			if (event.getMessage().getAuthor().isEmpty()) {
				return;
			}
			IClient client = new DiscordClient(event.getMessage().getAuthor().get());
			String message = event.getMessage().getContent();
			MessageChannel messageChannel = event.getMessage().getChannel().block();
			if (messageChannel == null) {
				return;
			}
			Optional<IChannel> channel = AbstractDiscordChannel.of(messageChannel);
			if (channel.isEmpty()) {
				return;
			}
			DiscordBot.this.onMessage(channel.get(), client, message);
		});
		this.gatewayDiscordClient.on(ConnectEvent.class).subscribe(event -> this.onConnect());
		this.gatewayDiscordClient.on(DisconnectEvent.class).subscribe(event -> this.onDisconnect());
		this.onConnect();
		this.gatewayDiscordClient.onDisconnect().block();
	}
	
	@Override
	protected void onConnect() {
		logger.info("Connected");
		super.onConnect();
	}
	
	@Override
	protected void onDisconnect() {
		logger.info("Disconnected");
		super.onDisconnect();
	}
	
	public void exit() {
		logger.info("Logging out...");
		this.gatewayDiscordClient.logout();
		logger.info("Logged out");
		logger.info("Shutdown");
	}
	
	@Override
	protected List<String> getAllClientUidsForLoginBonus() {
		return Collections.emptyList();
	}
	
	@Override
	protected IBotClient getBotClient() {
		return this.botClient;
	}
	
	@Override
	protected IConsoleChannel getConsoleChannel() {
		return new DiscordConsoleChannel();
	}
}
