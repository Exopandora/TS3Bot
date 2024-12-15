package net.kardexo.bot.services.api;

import java.util.Collection;
import java.util.TimerTask;
import java.util.function.Supplier;

public interface IBonusService
{
	void reset();
	
	boolean claim(String user);
	
	boolean hasClaimed(String user);
	
	void claim(Collection<String> users);
	
	TimerTask createTimerTask(Supplier<Collection<String>> users);
}
