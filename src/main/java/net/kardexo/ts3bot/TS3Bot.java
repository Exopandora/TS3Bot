package net.kardexo.ts3bot;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.TextMessageTargetMode;
import com.github.theholywaffle.teamspeak3.api.event.TS3EventAdapter;
import com.github.theholywaffle.teamspeak3.api.event.TS3EventType;
import com.github.theholywaffle.teamspeak3.api.event.TextMessageEvent;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.impl.CommandAmouranth;
import net.kardexo.ts3bot.commands.impl.CommandBobRoss;
import net.kardexo.ts3bot.commands.impl.CommandBot;
import net.kardexo.ts3bot.commands.impl.CommandExit;
import net.kardexo.ts3bot.commands.impl.CommandHelp;
import net.kardexo.ts3bot.commands.impl.CommandMove;
import net.kardexo.ts3bot.commands.impl.CommandRandom;
import net.kardexo.ts3bot.commands.impl.CommandSteam;
import net.kardexo.ts3bot.commands.impl.CommandTeams;
import net.kardexo.ts3bot.commands.impl.CommandTwitch;
import net.kardexo.ts3bot.commands.impl.CommandWatch2Gether;
import net.kardexo.ts3bot.config.Config;

public class TS3Bot extends TS3EventAdapter
{
	private static final Logger LOGGER = LogManager.getLogger();
	private static TS3Bot instance;
	
	private int id;
	private final Config config;
	private final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<CommandSource>();
	private final List<String> history = new ArrayList<String>();
	private TS3Query query;
	private TS3Api api;
	
	public TS3Bot(Config config)
	{
		TS3Bot.instance = this;
		this.config = config;
	}
	
	public void start() throws InterruptedException
	{
		Runtime.getRuntime().addShutdownHook(new Thread(this::exit));
		TS3Config config = new TS3Config().setHost(this.config.getHostAddress());
		
		this.query = new TS3Query(config);
		
		while(!this.connect())
		{
			Thread.sleep(10000);
		}
		
		TS3Bot.LOGGER.info("Connected to " + this.config.getHostAddress());
		
		this.registerCommands();
		this.api = this.query.getApi();
		
		while(!this.login())
		{
			Thread.sleep(10000);
		}
		
		TS3Bot.LOGGER.info("Logged in as " + this.config.getLoginName());
		
		this.api.selectVirtualServerById(this.config.getVirtualServerId(), this.config.getLoginName());
		this.id = this.api.whoAmI().getId();
		this.api.moveClient(this.id, this.api.getChannelByNameExact(this.config.getChannelName(), true).getId());
		this.api.registerEvent(TS3EventType.TEXT_CHANNEL);
		this.api.registerEvent(TS3EventType.TEXT_PRIVATE);
		this.api.registerEvent(TS3EventType.TEXT_SERVER);
		this.api.addTS3Listeners(this);
		
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
			this.query.connect();
			return true;
		}
		catch(Exception e)
		{
			TS3Bot.LOGGER.error("Connection failed");
			return false;
		}
	}
	
	private boolean login()
	{
		try
		{
			this.api.login(this.config.getLoginName(), this.config.getLoginPassword());
			return true;
		}
		catch(Exception e)
		{
			TS3Bot.LOGGER.error("Login failed");
			return false;
		}
	}
	
	private void registerCommands()
	{
		CommandExit.register(this.dispatcher);
		CommandSteam.register(this.dispatcher);
		CommandBot.register(this.dispatcher);
		CommandHelp.register(this.dispatcher);
		CommandAmouranth.register(this.dispatcher);
		CommandBobRoss.register(this.dispatcher);
		CommandTwitch.register(this.dispatcher);
		CommandTeams.register(this.dispatcher);
		CommandWatch2Gether.register(this.dispatcher);
		CommandRandom.register(this.dispatcher);
		CommandMove.register(this.dispatcher);
	}
	
	@Override
	public void onTextMessage(TextMessageEvent event)
	{
		if(event.getInvokerId() == this.id)
		{
			return;
		}
		
		StringReader reader = new StringReader(event.getMessage());
		
		if(reader.canRead() && reader.peek() == '!')
		{
			reader.skip();
			
			try
			{
				this.dispatcher.execute(reader, new CommandSource(this.query, this.api, this.api.getClientInfo(event.getInvokerId()), event.getTargetMode(), this.history, this.id));
			}
			catch(CommandSyntaxException e)
			{
				this.api.sendTextMessage(event.getTargetMode(), event.getInvokerId(), e.getMessage());
			}
		}
		else if(event.getTargetMode().equals(TextMessageTargetMode.CHANNEL))
		{
			this.history.add(0, event.getMessage());
			
			if(this.history.size() > this.config.getChatHistorySize())
			{
				this.history.remove(this.config.getChatHistorySize());
			}
		}
	}
	
	public void exit()
	{
		if(this.api != null)
		{
			this.api.logout();
			TS3Bot.LOGGER.info("Logged out");
		}
		
		if(this.query != null)
		{
			this.query.exit();
			TS3Bot.LOGGER.info("Disconnected");
		}
	}
	
	public Config getConfig()
	{
		return this.config;
	}
	
	public TS3Api getApi()
	{
		return this.api;
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public List<String> getHistory()
	{
		return this.history;
	}
	
	public TS3Query getQuery()
	{
		return this.query;
	}
	
	public static TS3Bot getInstance()
	{
		return TS3Bot.instance;
	}
}
