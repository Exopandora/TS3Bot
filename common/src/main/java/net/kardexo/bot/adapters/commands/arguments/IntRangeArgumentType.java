package net.kardexo.bot.adapters.commands.arguments;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

public class IntRangeArgumentType implements ArgumentType<IntRangeArgumentType.IntRange>
{
	private static final SimpleCommandExceptionType INVALID_RANGE = new SimpleCommandExceptionType(new LiteralMessage("Upper bound must be higher than lower bound"));
	private static final Dynamic2CommandExceptionType INTERVAL_TOO_LOW = new Dynamic2CommandExceptionType((found, min) -> new LiteralMessage("Lower bound must not be less than " + min + ", found " + found));
	private static final Dynamic2CommandExceptionType INTERVAL_TOO_HIGH = new Dynamic2CommandExceptionType((found, max) -> new LiteralMessage("Upper bound must be more than " + max + ", found " + found));
	
	private final int minimum;
	private final int maximum;
	
	public IntRangeArgumentType(int minimum, int maximum)
	{
		this.minimum = minimum;
		this.maximum = maximum;
	}
	
	@Override
	public IntRange parse(StringReader reader) throws CommandSyntaxException
	{
		int start = reader.getCursor();
		int lowerBound = this.readNumber(reader);
		reader.expect('-');
		int upperBound = this.readNumber(reader);
		
		if(lowerBound >= upperBound)
		{
			reader.setCursor(start);
			throw INVALID_RANGE.createWithContext(reader);
		}
		
		if(lowerBound < this.minimum)
		{
			reader.setCursor(start);
			throw INTERVAL_TOO_LOW.createWithContext(reader, lowerBound, this.minimum);
		}
		
		if(lowerBound > this.maximum || upperBound > this.maximum)
		{
			reader.setCursor(start);
			throw INTERVAL_TOO_HIGH.createWithContext(reader, upperBound, this.maximum);
		}
		
		return new IntRange(lowerBound, upperBound);
	}
	
	private int readNumber(StringReader reader) throws CommandSyntaxException
	{
		int start = reader.getCursor();
		
		if(reader.canRead() && reader.peek() == '-')
		{
			reader.skip();
		}
		
		while(reader.canRead() && isValidCharacter(reader))
		{
			reader.skip();
		}
		
		String number = reader.getString().substring(start, reader.getCursor());
		
		try
		{
			return Integer.parseInt(number);
		}
		catch(NumberFormatException numberformatexception)
		{
			throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidInt().createWithContext(reader, number);
		}
	}
	
	private static boolean isValidCharacter(StringReader reader)
	{
		char c = reader.peek();
		
		if(c < '0' || c > '9')
		{
			if(c != '.')
			{
				return false;
			}
			
			return !reader.canRead(2) || reader.peek(1) != '.';
		}
		
		return true;
	}
	
	public static IntRangeArgumentType range(int lowerBound, int upperBound)
	{
		return new IntRangeArgumentType(lowerBound, upperBound);
	}
	
	public static IntRangeArgumentType range()
	{
		return new IntRangeArgumentType(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}
	
	public static IntRange getRange(final CommandContext<?> context, final String name)
	{
		return context.getArgument(name, IntRange.class);
	}
	
	public static record IntRange(int lowerBound, int upperBound) {}
}
