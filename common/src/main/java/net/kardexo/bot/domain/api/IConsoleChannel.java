package net.kardexo.bot.domain.api;

import java.util.List;

public interface IConsoleChannel extends IChannel
{
	@Override
	default String getName()
	{
		return "Console";
	}
	
	@Override
	default String getId()
	{
		return "console";
	}
	
	List<IChannel> getBroadcastChannels();
}
