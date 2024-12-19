package net.kardexo.bot.domain.api;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IChannel
{
	String getName();
	
	String getId();
	
	List<IClient> getClients();
	
	@Nullable IServer getServer();
	
	boolean isJoinable();
}
