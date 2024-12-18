package net.kardexo.bot.domain.api;

import java.util.List;
import java.util.Optional;

public interface IServer
{
	String getId();
	
	String getName();
	
	Optional<IChannel> findChannelByName(String name);
	
	Optional<IChannel> findChannelById(String id);
	
	List<IClient> getClients();
	
	List<IChannel> getChannels();
	
	IServerChannel getServerChannel();
}
