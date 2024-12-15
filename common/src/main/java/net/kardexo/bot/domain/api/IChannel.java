package net.kardexo.bot.domain.api;

import java.util.List;

public interface IChannel
{
	String getName();
	
	String getId();
	
	List<IClient> getClients();
}
