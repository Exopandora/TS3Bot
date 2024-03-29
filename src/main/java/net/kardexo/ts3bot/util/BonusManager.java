package net.kardexo.ts3bot.util;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;

public class BonusManager
{
	private final ObjectMapper objectMapper;
	private final Set<String> claimed;
	private final Consumer<String> reward;
	private final File file;
	
	public BonusManager(File file, ObjectMapper objectMapper, Consumer<String> reward) throws IOException
	{
		this.file = file;
		this.objectMapper = objectMapper;
		this.claimed = Util.readJsonFile(this.file, this.objectMapper, new TypeReference<Set<String>>() {}, Sets::newHashSet);
		this.reward = reward;
	}
	
	public void reset()
	{
		synchronized(this.claimed)
		{
			this.claimed.clear();
			this.save();
		}
	}
	
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
	
	public boolean hasClaimed(String user)
	{
		synchronized(this.claimed)
		{
			return this.claimed.contains(user);
		}
	}
	
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
				e.printStackTrace();
			}
		}
	}
	
	public TimerTask createTimerTask(Supplier<Collection<String>> users)
	{
		return new TimerTask()
		{
			@Override
			public void run()
			{
				BonusManager.this.reset();
				BonusManager.this.claim(users.get());
			}
		};
	}
}
