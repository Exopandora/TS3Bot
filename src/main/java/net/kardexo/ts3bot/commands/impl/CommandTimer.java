package net.kardexo.ts3bot.commands.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;
import net.kardexo.ts3bot.util.Util;

public class CommandTimer
{
	private static final SimpleCommandExceptionType INVALID_DURATION = new SimpleCommandExceptionType(new LiteralMessage("Duration must follow pattern #d[ay][s] #h[our][s] #m[in[utes]][s] #s[ec[ond][s]]"));
	private static final SimpleCommandExceptionType COULD_NOT_PARSE_DURATION = new SimpleCommandExceptionType(new LiteralMessage("Could not parse duration"));
	private static final SimpleCommandExceptionType NO_TIMERS_SET = new SimpleCommandExceptionType(new LiteralMessage("No timers set"));
	private static final DynamicCommandExceptionType INVALID_NUMBER = new DynamicCommandExceptionType(number -> new LiteralMessage("Invalid number " + number));
	private static final Map<String, Timer> TIMERS = new HashMap<String, Timer>();
	private static final Pattern PATTERN = Pattern.compile("^(?:(\\d+)\\s*d(?:ays?)?\\s*)?(?:(\\d+)\\s*h(?:(?:ou)?rs)?\\s*?)?(?:(\\d+)\\s*m(?:in(?:ute)?s?)?\\s*?)?(?:(\\d+)\\s*s(?:ec(?:ond)?s?)?\\s*?)?$");
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("timer")
				.executes(context -> timer(context))
				.then(Commands.literal("reset")
						.executes(context -> reset(context)))
				.then(Commands.argument("duration", StringArgumentType.greedyString())
						.executes(context -> timer(context, StringArgumentType.getString(context, "duration")))));
	}
	
	private static int timer(CommandContext<CommandSource> context) throws CommandSyntaxException
	{
		Timer timer = TIMERS.get(context.getSource().getClientInfo().getUniqueIdentifier());
		
		if(timer == null)
		{
			throw NO_TIMERS_SET.create();
		}
		
		context.getSource().sendFeedback("Your timer ends in " + Util.formatDuration(Duration.between(Instant.now(), timer.getEnd()).getSeconds()));
		return (int) timer.getEnd().toEpochMilli();
	}
	
	private static int timer(CommandContext<CommandSource> context, String pattern) throws CommandSyntaxException
	{
		Matcher matcher = PATTERN.matcher(pattern);
		
		if(matcher.matches())
		{
			long duration = 0;
			
			if(matcher.group(1) != null)
			{
				duration += TimeUnit.DAYS.toSeconds(CommandTimer.parseNumber(matcher.group(1)));
			}
			
			if(matcher.group(2) != null)
			{
				duration += TimeUnit.HOURS.toSeconds(CommandTimer.parseNumber(matcher.group(2)));
			}
			
			if(matcher.group(3) != null)
			{
				duration += TimeUnit.MINUTES.toSeconds(CommandTimer.parseNumber(matcher.group(3)));
			}
			
			if(matcher.group(4) != null)
			{
				duration += TimeUnit.SECONDS.toSeconds(CommandTimer.parseNumber(matcher.group(4)));
			}
			
			if(duration > 0)
			{
				String uid = context.getSource().getClientInfo().getUniqueIdentifier();
				CommandTimer.removeTimer(uid);
				Timer timer = new Timer(Instant.now().plusSeconds(duration), CompletableFuture.runAsync(() ->
				{
					context.getSource().sendPrivateMessage("Your timer has ended!");
					CommandTimer.removeTimer(uid);
				}, CompletableFuture.delayedExecutor(duration, TimeUnit.SECONDS)));
				CommandTimer.addTimer(uid, timer);
			}
			else
			{
				throw COULD_NOT_PARSE_DURATION.create();
			}
			
			context.getSource().sendFeedback("Your timer has been set to " + Util.formatDuration(duration));
		}
		else
		{
			throw INVALID_DURATION.create();
		}
		
		return 0;
	}
	
	private static void addTimer(String id, Timer timer)
	{
		synchronized(TIMERS)
		{
			TIMERS.put(id, timer);
		}
	}
	
	private static Timer removeTimer(String id)
	{
		synchronized(TIMERS)
		{
			Timer timer = TIMERS.get(id);
			
			if(timer != null)
			{
				if(!timer.isDone())
				{
					timer.cancel();
				}
				
				return TIMERS.remove(id);
			}
		}
		
		return null;
	}
	
	private static int reset(CommandContext<CommandSource> context) throws CommandSyntaxException
	{
		Timer timer = CommandTimer.removeTimer(context.getSource().getClientInfo().getUniqueIdentifier());
		
		if(timer == null)
		{
			throw NO_TIMERS_SET.create();
		}
		
		context.getSource().sendFeedback("Your timer has been reset");
		return 0;
	}
	
	public static long parseNumber(String number) throws CommandSyntaxException
	{
		try
		{
			return Long.parseUnsignedLong(number);
		}
		catch(NumberFormatException e)
		{
			throw INVALID_NUMBER.create(number);
		}
	}
	
	private static class Timer
	{
		private final Instant end;
		private final CompletableFuture<Void> future;
		
		public Timer(Instant end, CompletableFuture<Void> future)
		{
			this.end = end;
			this.future = future;
		}
		
		public Instant getEnd()
		{
			return this.end;
		}
		
		public boolean isDone()
		{
			return this.future.isDone();
		}
		
		public boolean cancel()
		{
			return this.future.cancel(true);
		}
	}
}
