package net.kardexo.bot.domain.channel;

import net.kardexo.bot.domain.client.IClient;
import net.kardexo.bot.domain.server.IServer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IChannel {
	String getName();
	
	String getId();
	
	List<IClient> getClients();
	
	@Nullable IServer getServer();
	
	boolean isJoinable();
}
