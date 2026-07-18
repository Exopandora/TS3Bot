package net.kardexo.bot.domain.client;

import net.kardexo.bot.domain.channel.IPrivateChannel;

public interface IClient {
	String getId();
	
	String getName();
	
	IPrivateChannel getPrivateChannel();
}
