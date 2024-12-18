package net.kardexo.bot.domain.api;

public interface IClient
{
	String getId();
	
	String getName();
	
	IPrivateChannel getPrivateChannel();
}
