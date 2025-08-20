package net.kardexo.bot.domain;

import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.api.IChannel;
import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.domain.api.IConfigFactory;
import net.kardexo.bot.domain.api.IConsoleChannel;
import net.kardexo.bot.domain.config.Config;
import net.kardexo.bot.services.APIKeyService;
import net.kardexo.bot.services.BonusService;
import net.kardexo.bot.services.ConfigService;
import net.kardexo.bot.services.EconomyService;
import net.kardexo.bot.services.MessageService;
import net.kardexo.bot.services.PermissionService;
import net.kardexo.bot.services.UserConfigService;
import net.kardexo.bot.services.api.IAPIKeyService;
import net.kardexo.bot.services.api.IBonusService;
import net.kardexo.bot.services.api.IConfigService;
import net.kardexo.bot.services.api.IEconomyService;
import net.kardexo.bot.services.api.IMessageService;
import net.kardexo.bot.services.api.IPermissionService;
import net.kardexo.bot.services.api.IUserConfigService;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static net.kardexo.bot.domain.Util.OBJECT_MAPPER;

public abstract class AbstractBot<T extends Config>
{
	private final IConfigService<T> configService;
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
	
	public AbstractBot(String configFile, IConfigFactory<T> configFactory, Random random) throws IOException
	{
		this.configService = new ConfigService<T>(configFile, configFactory);
		this.random = random;
		this.chatHistory = new ChatHistory(this.getConfig().getChatHistorySize());
		this.economyService = new EconomyService(Util.createFile("coins.json"), this.getConfig().getCurrency(), OBJECT_MAPPER);
		this.apiKeyService = new APIKeyService(this.getConfig().getApiKeys());
		this.permissionService = new PermissionService(this.getConfig().getPermissions());
		Runtime.getRuntime().addShutdownHook(new Thread(this::exit));
	}
	
	public final void start()
	{
		this.connect();
	}
	
	protected abstract void connect();
	
	protected void onConnect()
	{
		this.messageService = new MessageService(this.getBotClient(), this.configService, this.apiKeyService, this.permissionService, this.economyService, this.userConfigService, this.random);
		this.loginBonusService.claim(this.getAllClientUidsForLoginBonus());
		this.timer = new Timer();
		this.timer.schedule(this.loginBonusService.createTimerTask(this::getAllClientUidsForLoginBonus), Util.tomorrow(), TimeUnit.DAYS.toMillis(1));
		this.consoleListener = new Thread(this::listen);
		this.consoleListener.setDaemon(true);
		this.consoleListener.start();
	}
	
	protected void onClientJoin(IClient client)
	{
		this.loginBonusService.claim(client.getId());
	}
	
	protected void onMessage(IChannel channel, IClient client, String message)
	{
		this.messageService.onMessage(channel, client, message, this.chatHistory);
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
				String message = scanner.nextLine().replaceFirst("^(?:" + Pattern.quote(this.getConfig().getCommandPrefix()) + ")?", this.getConfig().getCommandPrefix());
				this.onMessage(this.getConsoleChannel(), this.getBotClient(), message);
			}
		}
	}
	
	private void awardLoginBonus(String user)
	{
		this.economyService.add(user, this.getConfig().getLoginBonus());
	}
	
	protected abstract List<String> getAllClientUidsForLoginBonus();
	
	protected abstract IBotClient getBotClient();
	
	protected abstract IConsoleChannel getConsoleChannel();
	
	public T getConfig()
	{
		return this.configService.getConfig();
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
