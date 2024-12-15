package net.kardexo.bot.domain.api;

public interface IClient
{
	String getId();
	
	String getUniqueId();
	
	String getName();
	
	IChannel getChannel();
}
