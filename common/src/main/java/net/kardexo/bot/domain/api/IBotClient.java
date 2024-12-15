package net.kardexo.bot.domain.api;

import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public interface IBotClient extends IClient
{
	void sendPrivateMessage(IClient client, String message);
	
	void sendChannelMessage(String message);
	
	void sendServerMessage(String message);
	
	void sendConsoleMessage(String message);
	
	default void sendMessage(MessageTarget target, IClient client, String message)
	{
		switch(target)
		{
			case CLIENT -> this.sendPrivateMessage(client, message);
			case CHANNEL -> this.sendChannelMessage(message);
			case SERVER -> this.sendServerMessage(message);
			case CONSOLE -> this.sendConsoleMessage(message);
		}
	}
	
	Optional<IChannel> findChannelByName(String name);
	
	Optional<IChannel> findChannelById(String id);
	
	Optional<IClient> findClientByName(String name);
	
	Optional<IClient> findClientById(String id);
	
	List<IClient> getClients();
	
	List<IChannel> getChannels();
	
	void ban(@Nullable String reason, Duration duration, IClient client);
	
	void kick(@Nullable String reason, IClient... client);
	
	void move(IClient client, IChannel channel);
	
	void disconnect();
	
	boolean isSilent();
	
	void setSilent(boolean silent);
}
