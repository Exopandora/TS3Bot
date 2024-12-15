package net.kardexo.bot.domain;

import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.domain.api.MessageTarget;
import net.kardexo.bot.domain.config.Config;
import net.kardexo.bot.services.APIKeyService;
import net.kardexo.bot.services.BonusService;
import net.kardexo.bot.services.EconomyService;
import net.kardexo.bot.services.MessageService;
import net.kardexo.bot.services.PermissionService;
import net.kardexo.bot.services.UserConfigService;
import net.kardexo.bot.services.api.IAPIKeyService;
import net.kardexo.bot.services.api.IBonusService;
import net.kardexo.bot.services.api.IEconomyService;
import net.kardexo.bot.services.api.IMessageService;
import net.kardexo.bot.services.api.IPermissionService;
import net.kardexo.bot.services.api.IUserConfigService;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import static net.kardexo.bot.domain.Util.OBJECT_MAPPER;

public abstract class AbstractBot
{
	private final Config config;
	private final Random random;
	private final ChatHistory chatHistory;
	private final IEconomyService economyService;
	private final IBonusService loginBonusService = new BonusService(Util.createFile("claims.json"), OBJECT_MAPPER, this::awardLoginBonus);
	private final IUserConfigService userConfigService = new UserConfigService(Util.createFile("userconfig.json"), OBJECT_MAPPER);
	private final IPermissionService permissionService;
	private final IAPIKeyService apiKeyService;
	private Thread consoleListener;
	private IMessageService messageService;
	private Timer timer;
	
	public AbstractBot(Config config, Random random) throws IOException
	{
		this.config = config;
		this.random = random;
		this.chatHistory = new ChatHistory(this.config.getChatHistorySize());
		this.economyService = new EconomyService(Util.createFile("coins.json"), this.config.getCurrency(), OBJECT_MAPPER);
		this.apiKeyService = new APIKeyService(this.config.getApiKeys());
		this.permissionService = new PermissionService(this.config.getPermissions());
		Runtime.getRuntime().addShutdownHook(new Thread(this::exit));
	}
	
	public final void start()
	{
		this.connect();
	}
	
	protected abstract void connect();
	
	protected void onConnect()
	{
		this.messageService = new MessageService(this.getBotClient(), this.config, this.apiKeyService, this.permissionService, this.economyService, this.userConfigService, this.random);
		this.loginBonusService.claim(this.getAllClientUids());
		this.timer = new Timer();
		this.timer.schedule(this.loginBonusService.createTimerTask(this::getAllClientUids), Util.tomorrow(), TimeUnit.DAYS.toMillis(1));
		this.consoleListener = new Thread(this::listen);
		this.consoleListener.setDaemon(true);
		this.consoleListener.start();
	}
	
	protected void onClientJoin(IClient client)
	{
		this.loginBonusService.claim(client.getUniqueId());
	}
	
	protected void onMessage(IClient client, String message, MessageTarget target)
	{
		this.messageService.onMessage(client, message, target, this.chatHistory);
	}
	
	protected void onDisconnect()
	{
		this.timer.cancel();
		this.consoleListener.interrupt();
	}
	
	public abstract void exit();
	
	private void listen()
	{
		try(Scanner scanner = new Scanner(System.in))
		{
			while(scanner.hasNextLine())
			{
				String message = scanner.nextLine().replaceFirst("^!*", "!");
				this.onMessage(this.getBotClient(), message, MessageTarget.CONSOLE);
			}
		}
	}
	
	private void awardLoginBonus(String user)
	{
		this.economyService.add(user, this.config.getLoginBonus());
	}
	
	private List<String> getAllClientUids()
	{
		return this.getBotClient().getClients().stream().map(IClient::getUniqueId).toList();
	}
	
	protected abstract IBotClient getBotClient();
	
	public Config getConfig()
	{
		return this.config;
	}
	
	public ChatHistory getChatHistory()
	{
		return this.chatHistory;
	}
	
	public IAPIKeyService getApiKeyService()
	{
		return this.apiKeyService;
	}
	
	public IEconomyService getEconomyService()
	{
		return this.economyService;
	}
}
