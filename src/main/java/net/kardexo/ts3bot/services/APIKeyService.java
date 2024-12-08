package net.kardexo.ts3bot.services;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.kardexo.ts3bot.config.Config.APIKey;
import net.kardexo.ts3bot.config.Config.APIKey.Limit;

public class APIKeyService
{
	public static final String API_KEY_WATCH_2_GETHER = "watch_2_gether";
	public static final String API_KEY_TWITCH = "twitch";
	public static final String API_KEY_YOUTUBE = "youtube";
	public static final String API_KEY_TWITTER = "twitter";
	public static final String API_KEY_LEAGUE_OF_LEGENDS = "league_of_legends";
	public static final String API_KEY_IMAGGA = "imagga";
	private static final long BUFFER_TIME = 250;
	
	private final Map<String, APIKey> apiKeys;
	private final Map<APIKey, LinkedList<Long>> requests = new HashMap<APIKey, LinkedList<Long>>();
	
	public APIKeyService(Map<String, APIKey> apiKeys)
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
			long waitingTime = this.schedule(apiKey);
			
			if(waitingTime > 0)
			{
				try
				{
					Thread.sleep(waitingTime);
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		return result;
	}
	
	private synchronized long schedule(APIKey apiKey)
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
