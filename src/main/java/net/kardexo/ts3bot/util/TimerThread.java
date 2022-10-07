package net.kardexo.ts3bot.util;

import java.time.Instant;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;

public class TimerThread extends Thread
{
	private final Queue<Timer> queue = new PriorityQueue<Timer>((a, b) -> a.getEnd().compareTo(b.getEnd()));
	
	public TimerThread()
	{
		this.setDaemon(true);
	}
	
	@Override
	public void run()
	{
		while(true)
		{
			try
			{
				while(!this.queue.isEmpty())
				{
					Timer timer = this.queue.peek();
					Instant now = Instant.now();
					
					if(!timer.isCancelled())
					{
						long wait = timer.getEnd().toEpochMilli() - now.toEpochMilli();
						Thread.sleep(wait);
						timer.execute();
					}
					
					synchronized(this.queue)
					{
						this.queue.remove(timer);	
					}
				}
				
				synchronized(this)
				{
					this.wait();
				}
			}
			catch(InterruptedException e)
			{
				//Timers changed
			}
		}
	}
	
	public boolean hasTimer(String id)
	{
		return this.getTimer(id).isPresent();
	}
	
	public Optional<Timer> getTimer(String id)
	{
		synchronized(this.queue)
		{
			for(Timer timer : this.queue)
			{
				if(timer.getId().equals(id))
				{
					return Optional.of(timer);
				}
			}
		}
		
		return Optional.empty();
	}
	
	public Optional<Timer> resetTimer(String id)
	{
		Optional<Timer> optional = this.getTimer(id);
		
		if(optional.isPresent())
		{
			Timer timer = optional.get();
			timer.cancel();
			
			synchronized(this.queue)
			{
				this.queue.remove(timer);	
			}
			
			synchronized(this)
			{
				this.interrupt();
			}
		}
		
		return optional;
	}
	
	public Optional<Timer> setTimer(String id, Instant end, Runnable runnable)
	{
		Optional<Timer> optional = this.getTimer(id);
		
		if(optional.isPresent())
		{
			Timer timer = optional.get();
			timer.cancel();
			
			synchronized(this.queue)
			{
				this.queue.remove(timer);	
			}
		}
		
		synchronized(this.queue)
		{
			this.queue.add(new Timer(id, end, runnable));
		}
		
		synchronized(this)
		{
			this.interrupt();
		}
		
		return optional;
	}
	
	public static class Timer
	{
		private final String id;
		private final Instant end;
		private final Runnable runnable;
		private boolean cancelled;
		
		public Timer(String id, Instant end, Runnable runnable)
		{
			this.id = id;
			this.end = end;
			this.runnable = runnable;
		}
		
		public void execute()
		{
			this.runnable.run();
		}
		
		public String getId()
		{
			return this.id;
		}
		
		public Instant getEnd()
		{
			return this.end;
		}
		
		public boolean isCancelled()
		{
			return this.cancelled;
		}
		
		public void cancel()
		{
			this.cancelled = true;
		}
	}
}
