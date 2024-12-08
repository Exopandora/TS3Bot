package net.kardexo.ts3bot.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CoinManager
{
	private final ObjectMapper objectMapper;
	private final Map<String, Long> wallet;
	private final File file;
	
	public CoinManager(File file, ObjectMapper objectMapper) throws JsonParseException, JsonMappingException, IOException
	{
		this.file = file;
		this.objectMapper = objectMapper;
		this.wallet = Util.readJsonFile(this.file, this.objectMapper, new TypeReference<Map<String, Long>>() {}, HashMap::new);
	}
	
	public void add(String user, long coins)
	{
		synchronized(this.wallet)
		{
			this.wallet.compute(user, (key, value) -> value == null ? coins : value + coins);
			this.save();
		}
	}
	
	public void subtract(String user, long coins)
	{
		synchronized(this.wallet)
		{
			this.wallet.compute(user, (key, value) -> value == null ? coins : value - coins);
			this.save();
		}
	}
	
	public long get(String user)
	{
		synchronized(this.wallet)
		{
			return this.wallet.getOrDefault(user, 0L);
		}
	}
	
	public boolean hasCoins(String user, long coins)
	{
		synchronized(this.wallet)
		{
			return this.get(user) >= coins;
		}
	}
	
	public void set(String user, long coins)
	{
		synchronized(this.wallet)
		{
			this.wallet.compute(user, (key, value) -> Math.max(0, coins));
			this.save();
		}
	}
	
	public boolean transfer(String sender, String beneficiary, long coins)
	{
		synchronized(this.wallet)
		{
			if(!this.hasCoins(sender, coins))
			{
				return false;
			}
			
			this.subtract(sender, coins);
			this.add(beneficiary, coins);
			
			return true;
		}
	}
	
	private void save()
	{
		synchronized(this.wallet)
		{
			try
			{
				this.objectMapper.writeValue(this.file, this.wallet);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
