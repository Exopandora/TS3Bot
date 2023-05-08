package net.kardexo.ts3bot.commands.arguments;

import java.time.Duration;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

public class DurationArgumentType implements ArgumentType<Duration>
{
	private static final Map<String, TimeUnit> IDENTIFIER_TO_TIMEUNIT = Arrays.stream(TimeUnit.values()).mapMulti((TimeUnit unit, Consumer<SimpleEntry<String, TimeUnit>> consumer) ->
	{
		for(String identifier : unit.getIdentifiers())
		{
			consumer.accept(new SimpleEntry<String, TimeUnit>(identifier.toLowerCase(), unit));
		}
	}).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	private static final DynamicCommandExceptionType INVALID_TIME_UNIT = new DynamicCommandExceptionType((found) -> new LiteralMessage("Invalid time unit, found " + found));
	private static final Dynamic2CommandExceptionType INVALID_TIME_UNIT_ORDER = new Dynamic2CommandExceptionType((expected, found) -> new LiteralMessage("Invalid time unit order, expected '" + expected + "' but found '" + found + "'"));
	private static final SimpleCommandExceptionType TOTAL_DURATION_TOO_LOW = new SimpleCommandExceptionType(new LiteralMessage("Total duration must be higher than 0"));
	private static final DynamicCommandExceptionType DURATION_TOO_LOW = new DynamicCommandExceptionType((found) -> new LiteralMessage("Duration must be at least 0, found " + found));
	
	@Override
	public <S> Duration parse(StringReader reader) throws CommandSyntaxException
	{
		final int start = reader.getCursor();
		Duration duration = Duration.ZERO;
		boolean empty = true;
		
		for(TimeUnit unit : TimeUnit.values())
		{
			StringReader copy = new StringReader(reader);
			copy.skipWhitespace();
			Optional<Duration> section = this.tryReadTimeUnit(copy, unit);
			
			if(section.isPresent())
			{
				duration = duration.plus(section.get());
				reader.setCursor(copy.getCursor());
				empty = false;
			}
		}
		
		if(empty || duration.isZero())
		{
			reader.setCursor(start);
			throw TOTAL_DURATION_TOO_LOW.createWithContext(reader);
		}
		
		return duration;
	}
	
	private Optional<Duration> tryReadTimeUnit(StringReader reader, TimeUnit expected) throws CommandSyntaxException
	{
		int start = reader.getCursor();
		int duration;
		
		try
		{
			duration = reader.readInt();
		}
		catch(CommandSyntaxException e)
		{
			return Optional.empty();
		}
		
		if(duration < 0)
		{
			reader.setCursor(start);
			throw DURATION_TOO_LOW.createWithContext(reader, duration);
		}
		
		reader.skipWhitespace();
		start = reader.getCursor();
		String identifier = reader.readUnquotedString();
		TimeUnit unit = IDENTIFIER_TO_TIMEUNIT.get(identifier);
		
		if(unit == null)
		{
			reader.setCursor(start);
			throw INVALID_TIME_UNIT.createWithContext(reader, identifier);
		}
		else if(expected.isAfter(unit))
		{
			reader.setCursor(start);
			throw INVALID_TIME_UNIT_ORDER.createWithContext(reader, expected, unit);
		}
		else if(!expected.equals(unit))
		{
			return Optional.empty();
		}
		
		return Optional.of(unit.getMapper().apply((long) duration));
	}
	
	public static DurationArgumentType duration()
	{
		return new DurationArgumentType();
	}
	
	public static Duration getDuration(final CommandContext<?> context, final String name)
	{
		return context.getArgument(name, Duration.class);
	}
	
	private static enum TimeUnit
	{
		DAYS("days", Duration::ofDays, "d", "ds", "day", "days"),
		HOURS("hours", Duration::ofHours, "h", "hrs", "hour", "hours"),
		MINUTES("minutes", Duration::ofMinutes, "m", "min", "mins", "minute", "minutes"),
		SECONDS("seconds", Duration::ofSeconds, "s", "sec", "secs", "second", "seconds");
		
		private final String name;
		private final Function<Long, Duration> mapper;
		private final String[] identifiers;
		
		private TimeUnit(String name, Function<Long, Duration> mapper, String... identifiers)
		{
			this.name = name;
			this.mapper = mapper;
			this.identifiers = identifiers;
		}
		
		public String[] getIdentifiers()
		{
			return this.identifiers;
		}
		
		public Function<Long, Duration> getMapper()
		{
			return this.mapper;
		}
		
		public boolean isAfter(TimeUnit other)
		{
			return other.ordinal() < this.ordinal();
		}
		
		@Override
		public String toString()
		{
			return this.name;
		}
	}
}
