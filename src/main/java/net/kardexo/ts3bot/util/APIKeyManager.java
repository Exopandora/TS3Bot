package net.kardexo.ts3bot.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import net.kardexo.ts3bot.config.Config.APIKey;
import net.kardexo.ts3bot.config.Config.APIKey.Limit;

public class APIKeyManager
{
	private static final long BUFFER_TIME = 250;
	
	private final Map<String, APIKey> apiKeys;
	private final Map<APIKey, LinkedList<Long>> requests = new HashMap<APIKey, LinkedList<Long>>();
	
	public APIKeyManager(Map<String, APIKey> apiKeys)
	{
		this.apiKeys = apiKeys;
	}
	
	public String requestKey(String key)
	{
		return this.requestToken(key, "api_key");
	}
	
	public String requestToken(String key, String token)
	{
		APIKey apiKey = this.apiKeys.get(key);
		
		if(apiKey == null)
		{
			return null;
		}
		
		String result = apiKey.getTokens().path(token).asText();
		
		if(apiKey.getLimits() != null)
		{
			try
			{
				return this.schedule(apiKey, result).get();
			}
			catch(InterruptedException | ExecutionException e)
			{
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	private synchronized CompletableFuture<String> schedule(APIKey apiKey, String result)
	{
		LinkedList<Long> requests = this.requests.computeIfAbsent(apiKey, api -> new LinkedList<Long>());
		long time = System.currentTimeMillis();
		this.removeExpired(time, apiKey.getLimits(), requests);
		
		if(!requests.isEmpty())
		{
			long waitingTime = this.computeWaitingTime(time, apiKey.getLimits(), requests);
			requests.add(time + waitingTime);
			return CompletableFuture.supplyAsync(result::toString, CompletableFuture.delayedExecutor(waitingTime, TimeUnit.MILLISECONDS));
		}
		
		requests.add(time);
		return CompletableFuture.supplyAsync(result::toString);
	}
	
	private void removeExpired(long time, List<Limit> limits, LinkedList<Long> requests)
	{
		long bound = time - Collections.max(limits, (a, b) -> Long.compare(a.getDuration(), b.getDuration())).getDuration();
		
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
