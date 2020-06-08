package net.kardexo.ts3bot;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ChatHistory
{
	private final Map<String, Long> history = new HashMap<String, Long>();
	private final int size;
	
	public ChatHistory(int size)
	{
		this.size = size;
	}
	
	public boolean appendAndCheckIfNew(String line, long millis)
	{
		Long now = System.currentTimeMillis();
		Long timestamp = this.history.put(line, now);
		
		if(timestamp == null)
		{
			if(this.history.size() > this.size)
			{
				this.history.remove(this.getMin().getKey());
			}
		}
		else
		{
			return now - timestamp > millis;
		}
		
		return true;
	}
	
	private Entry<String, Long> getMin()
	{
		Entry<String, Long> min = null;
		
		for(Entry<String, Long> entry : this.history.entrySet())
		{
			if(min == null || entry.getValue() < min.getValue())
			{
				min = entry;
			}
		}
		
		return min;
	}
}
