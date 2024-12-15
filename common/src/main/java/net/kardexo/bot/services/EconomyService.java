package net.kardexo.bot.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.kardexo.bot.domain.Util;
import net.kardexo.bot.services.api.IEconomyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EconomyService implements IEconomyService
{
	private static final Logger logger = LoggerFactory.getLogger(EconomyService.class);
	
	private final ObjectMapper objectMapper;
	private final Map<String, Long> wallet;
	private final File file;
	private final String currency;
	
	public EconomyService(File file, String currency, ObjectMapper objectMapper) throws IOException
	{
		this.file = file;
		this.currency = currency;
		this.objectMapper = objectMapper;
		this.wallet = Util.readJsonFile(this.file, this.objectMapper, new TypeReference<Map<String, Long>>() {}, HashMap::new);
	}
	
	@Override
	public void add(String user, long coins)
	{
		synchronized(this.wallet)
		{
			this.wallet.compute(user, (key, value) -> value == null ? coins : value + coins);
			this.save();
		}
	}
	
	@Override
	public void subtract(String user, long coins)
	{
		synchronized(this.wallet)
		{
			this.wallet.compute(user, (key, value) -> value == null ? coins : value - coins);
			this.save();
		}
	}
	
	@Override
	public long get(String user)
	{
		synchronized(this.wallet)
		{
			return this.wallet.getOrDefault(user, 0L);
		}
	}
	
	@Override
	public boolean hasCoins(String user, long coins)
	{
		synchronized(this.wallet)
		{
			return this.get(user) >= coins;
		}
	}
	
	@Override
	public void set(String user, long coins)
	{
		synchronized(this.wallet)
		{
			this.wallet.compute(user, (key, value) -> Math.max(0, coins));
			this.save();
		}
	}
	
	@Override
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
	
	@Override
	public String getCurrency()
	{
		return this.currency;
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
				logger.error("Error saving wallet", e);
			}
		}
	}
}
