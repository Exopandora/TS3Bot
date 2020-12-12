package net.kardexo.ts3bot.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.github.manevolent.ts3j.api.Channel;
import com.github.manevolent.ts3j.api.Client;
import com.github.manevolent.ts3j.api.TextMessageTargetMode;
import com.github.manevolent.ts3j.command.response.CommandResponse;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.kardexo.ts3bot.TS3Bot;

public class TS3Utils
{
	public static final SimpleCommandExceptionType ERROR_EXECUTING_COMMAND = new SimpleCommandExceptionType(new LiteralMessage("Error executing command"));
	
	public static Optional<Channel> findChannelByName(String name)
	{
		try
		{
			CommandResponse<Iterable<Channel>> response = TS3Bot.getInstance().getClient().getChannels();
			
			for(Channel channel : response.get())
			{
				if(channel.getName().equals(name))
				{
					return Optional.of(channel);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return Optional.empty();
	}
	
	public static void sendMessage(TextMessageTargetMode targetMode, Client client, String message)
	{
		System.out.println(targetMode + " " + client.getId() + " " + client.getNickname() + " \"" + message + "\"");
		TS3Utils.executeSilently(() ->
		{
			switch(targetMode)
			{
				case CHANNEL:
					TS3Bot.getInstance().getClient().sendChannelMessage(client.getChannelId(), message);
					break;
				case CLIENT:
					TS3Bot.getInstance().getClient().sendPrivateMessage(client.getId(), message);
					break;
				case SERVER:
					TS3Bot.getInstance().getClient().sendServerMessage(message);
					break;
			}
		});
	}
	
	public static Optional<Client> findClientById(int clientId)
	{
		try
		{
			return Optional.of(TS3Bot.getInstance().getClient().getClientInfo(clientId));
		}
		catch(Exception e)
		{
			return Optional.empty();
		}
	}
	
	public static List<Client> getClients() throws CommandSyntaxException
	{
		List<Client> clients = new ArrayList<Client>();
		
		TS3Utils.execute(() ->
		{
			for(Client client : TS3Bot.getInstance().getClient().listClients())
			{
				if(client.getId() != TS3Bot.getInstance().getId())
				{
					clients.add(client);
				}
			}
		});
		
		return clients;
	}
	
	public static int kick(Client client) throws CommandSyntaxException
	{
		return TS3Utils.kick(client, "");
	}
	
	public static int kick(Client client, String reason) throws CommandSyntaxException
	{
		int clientId = client.getId();
		TS3Utils.execute(() -> TS3Bot.getInstance().getClient().kick(Arrays.asList(clientId), reason));
		return clientId;
	}
	
	public static int kick(Predicate<Client> predicate) throws CommandSyntaxException
	{
		return TS3Utils.kick(predicate, "");
	}
	
	public static int kick(Predicate<Client> predicate, String reason) throws CommandSyntaxException
	{
		List<Integer> clients = new ArrayList<Integer>();
		
		for(Client client : TS3Utils.getClients())
		{
			if(predicate.test(client))
			{
				clients.add(client.getId());
			}
		}
		
		TS3Utils.execute(() -> TS3Bot.getInstance().getClient().kick(clients, reason));
		return clients.size();
	}
	
	public static void moveClient(int clientId, int channelId) throws CommandSyntaxException
	{
		TS3Utils.moveClient(clientId, channelId, null);
	}
	
	public static void moveClient(int clientId, int channelId, String password) throws CommandSyntaxException
	{
		TS3Utils.execute(() -> TS3Bot.getInstance().getClient().clientMove(clientId, channelId, StringUtils.emptyToNull(password)));
	}
	
	public static void executeSilently(ExceptionRunnable runnable)
	{
		try
		{
			runnable.run();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void execute(ExceptionRunnable runnable) throws CommandSyntaxException
	{
		try
		{
			runnable.run();
		}
		catch(Exception e)
		{
			ERROR_EXECUTING_COMMAND.create();
		}
	}
	
	@FunctionalInterface
	public static interface ExceptionRunnable
	{
		void run() throws Exception;
	}
}
