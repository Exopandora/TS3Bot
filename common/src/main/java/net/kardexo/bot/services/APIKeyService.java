package net.kardexo.bot.services;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.kardexo.bot.domain.config.APIKeyConfig;
import net.kardexo.bot.domain.config.APIKeyConfig.Limit;
import net.kardexo.bot.services.api.IAPIKeyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class APIKeyService implements IAPIKeyService
{
	private static final long BUFFER_TIME = 250;
	private static final Logger logger = LoggerFactory.getLogger(APIKeyService.class);
	
	private final Map<String, APIKeyConfig> apiKeys;
	private final Map<APIKeyConfig, LinkedList<Long>> requests = new HashMap<APIKeyConfig, LinkedList<Long>>();
	
	public APIKeyService(Map<String, APIKeyConfig> apiKeys)
	{
		this.apiKeys = apiKeys;
	}
	
	@Override
	public String requestKey(String key)
	{
		return this.requestToken(key, "api_key");
	}
	
	@Override
	public String requestToken(String key, String token)
	{
		APIKeyConfig apiKey = this.apiKeys.get(key);
		
		if(apiKey == null)
		{
			return null;
		}
		
		String result = apiKey.getTokens().path(token).asText();
		
		if(apiKey.getLimits() != null)
		{
			long waitingTime = this.schedule(apiKey);
			
			if(waitingTime > 0)
			{
				try
				{
					Thread.sleep(waitingTime);
				}
				catch(InterruptedException e)
				{
					logger.error("Interrupted while waiting for token {}", token);
				}
			}
		}
		
		return result;
	}
	
	private synchronized long schedule(APIKeyConfig apiKey)
	{
		LinkedList<Long> requests = this.requests.computeIfAbsent(apiKey, api -> new LinkedList<Long>());
		long time = System.currentTimeMillis();
		this.removeExpired(time, apiKey.getLimits(), requests);
		
		if(!requests.isEmpty())
		{
			long waitingTime = this.computeWaitingTime(time, apiKey.getLimits(), requests);
			requests.add(time + waitingTime);
			return waitingTime;
		}
		
		requests.add(time);
		return 0;
	}
	
	private void removeExpired(long time, List<Limit> limits, LinkedList<Long> requests)
	{
		long bound = time - Collections.max(limits, Comparator.comparingLong(Limit::getDuration)).getDuration();
		
		while(!requests.isEmpty() && bound > requests.peek())
		{
			requests.pop();
		}
	}
	
	private long computeWaitingTime(long time, List<Limit> limits, LinkedList<Long> requests)
	{
		long result = 0;
		
		for(Limit limit : limits)
		{
			if(limit.getLimit() > 0 && limit.getLimit() <= requests.size())
			{
				long bound = requests.get(Math.max(requests.size() - limit.getLimit(), 0));
				
				if(limit.getDuration() > time - bound)
				{
					long waitingTime = limit.getDuration() - time + bound + BUFFER_TIME;
					
					if(waitingTime > result)
					{
						result = waitingTime;
					}
				}
			}
		}
		
		return result;
	}
}
