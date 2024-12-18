package net.kardexo.bot.domain.api;

import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public interface IBotClient extends IClient
{
	void sendPrivateMessage(IPrivateChannel channel, String message);
	
	void sendServerMessage(IServerChannel channel, String message);
	
	void sendChannelMessage(IMessageChannel channel, String message);
	
	void sendConsoleMessage(IConsoleChannel channel, String message);
	
	default void sendMessage(IChannel channel, String message)
	{
		switch(channel)
		{
			case IPrivateChannel pc -> this.sendPrivateMessage(pc, message);
			case IConsoleChannel cc -> this.sendConsoleMessage(cc, message);
			case IServerChannel sc -> this.sendServerMessage(sc, message);
			case IMessageChannel mc -> this.sendChannelMessage(mc, message);
			default -> {}
		}
	}
	
	void ban(IServer server, @Nullable String reason, Duration duration, IClient client);
	
	void kick(IServer server, @Nullable String reason, IClient... client);
	
	void move(IClient client, IChannel channel);
	
	void disconnect();
	
	boolean isSilent();
	
	void setSilent(boolean silent);
	
	boolean canMove();
}
