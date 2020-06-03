package net.kardexo.ts3bot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ChatHistory
{
	private final List<String> history = new ArrayList<String>();
	private final int size;
	
	public ChatHistory(int size)
	{
		this.size = size;
	}
	
	public void append(String line)
	{
		synchronized(this.history)
		{
			this.history.add(0, line);
			
			if(this.history.size() > this.size)
			{
				this.history.remove(this.size);
			}
		}
	}
	
	public String search(Predicate<String> predicate)
	{
		return this.search(predicate, 0, this.history.size());
	}
	
	public String search(Predicate<String> predicate, int start, int end)
	{
		synchronized(this.history)
		{
			if(end < 0 || end > this.history.size())
			{
				end = this.history.size();
			}
			
			for(int x = start; x < end; x++)
			{
				String message = this.history.get(x);
				
				if(predicate.test(message))
				{
					return message;
				}
			}
			
			return null;
		}
	}
	
	public boolean contains(Predicate<String> predicate, boolean skipLast, int limit)
	{
		return this.search(predicate, skipLast ? 1 : 0, Math.max(1, limit + (skipLast ? 1 : 0))) == null;
	}
}
