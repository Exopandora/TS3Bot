package net.kardexo.bot.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.kardexo.bot.services.api.IBonusService;
import net.kardexo.bot.domain.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BonusService implements IBonusService
{
	private static final Logger logger = LoggerFactory.getLogger(BonusService.class);
	
	private final ObjectMapper objectMapper;
	private final Set<String> claimed;
	private final Consumer<String> reward;
	private final File file;
	
	public BonusService(File file, ObjectMapper objectMapper, Consumer<String> reward) throws IOException
	{
		this.file = file;
		this.objectMapper = objectMapper;
		this.claimed = Util.readJsonFile(this.file, this.objectMapper, new TypeReference<Set<String>>() {}, HashSet::new);
		this.reward = reward;
	}
	
	@Override
	public void reset()
	{
		synchronized(this.claimed)
		{
			this.claimed.clear();
			this.save();
		}
	}
	
	@Override
	public boolean claim(String user)
	{
		synchronized(this.claimed)
		{
			if(this.hasClaimed(user))
			{
				return false;
			}
			
			this.claimed.add(user);
			this.reward.accept(user);
			this.save();
			
			return true;
		}
	}
	
	@Override
	public boolean hasClaimed(String user)
	{
		synchronized(this.claimed)
		{
			return this.claimed.contains(user);
		}
	}
	
	@Override
	public void claim(Collection<String> users)
	{
		synchronized(this.claimed)
		{
			for(String user : users)
			{
				if(!this.hasClaimed(user))
				{
					this.claimed.add(user);
					this.reward.accept(user);
				}
			}
			
			this.save();
		}
	}
	
	private void save()
	{
		synchronized(this.claimed)
		{
			try
			{
				this.objectMapper.writeValue(this.file, this.claimed);
			}
			catch(Exception e)
			{
				logger.error("Error saving bonus claims", e);
			}
		}
	}
	
	@Override
	public TimerTask createTimerTask(Supplier<Collection<String>> users)
	{
		return new TimerTask()
		{
			@Override
			public void run()
			{
				BonusService.this.reset();
				BonusService.this.claim(users.get());
			}
		};
	}
}
