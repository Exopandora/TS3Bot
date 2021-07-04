package net.kardexo.ts3bot.commands.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;

import ch.obermuhlner.math.big.BigDecimalMath;
import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;
import net.kardexo.ts3bot.commands.impl.CommandCalculate.Expression.ParseException;

public class CommandCalculate
{
	private static final Map<String, BigDecimal> HISTORY = new HashMap<String, BigDecimal>();
	private static final DynamicCommandExceptionType PARSING_EXCEPTION = new DynamicCommandExceptionType(exception -> new LiteralMessage(String.valueOf(exception)));
	private static final SimpleCommandExceptionType NO_ANS_STORED = new SimpleCommandExceptionType(new LiteralMessage("No previous value stored for ans"));
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.##########", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
	private static final MathContext MATH_CONTEXT = new MathContext(100);
	private static final MathContext MATH_CONTEXT_CEIL = new MathContext(MATH_CONTEXT.getPrecision(), RoundingMode.CEILING);
	private static final MathContext MATH_CONTEXT_FLOOR = new MathContext(MATH_CONTEXT.getPrecision(), RoundingMode.FLOOR);
	private static final BigDecimal E = BigDecimalMath.e(MATH_CONTEXT);
	private static final BigDecimal PI = BigDecimalMath.pi(MATH_CONTEXT);
	private static final BigDecimal TO_RADIANS = PI.divide(new BigDecimal(180), MATH_CONTEXT);
	private static final BigDecimal TO_DEGREES = new BigDecimal(180).divide(PI, MATH_CONTEXT);
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		LiteralCommandNode<CommandSource> watch2gether = dispatcher.register(Commands.literal("calculate")
				.then(Commands.argument("expression", StringArgumentType.greedyString())
						.executes(context -> calculate(context, StringArgumentType.getString(context, "expression")))));
		dispatcher.register(Commands.literal("calc")
				.redirect(watch2gether));
	}
	
	private static int calculate(CommandContext<CommandSource> context, String expression) throws CommandSyntaxException
	{
		try
		{
			String uuid = context.getSource().getClientInfo().getUniqueIdentifier();
			
			if(expression.contains("ans") && !HISTORY.containsKey(uuid))
			{
				throw NO_ANS_STORED.create();
			}
			
			BigDecimal x = new Expression(expression).eval(HISTORY.get(uuid));
			context.getSource().sendFeedback(expression + " = " + DECIMAL_FORMAT.format(x));
			HISTORY.put(uuid, x);
			
			return x.intValue();
		}
		catch(ParseException e)
		{
			throw PARSING_EXCEPTION.create(e.getMessage());
		}
	}
	
	public static class Expression
	{
		private final String string;
		
		public Expression(String string)
		{
			this.string = string;
		}
		
		public BigDecimal eval() throws ParseException
		{
			return this.eval(null);
		}
		
		public BigDecimal eval(BigDecimal ans) throws ParseException
		{
			Parser parser = new Parser(this.string, ans);
			BigDecimal x = parser.parseExpression();
			
			if(parser.getPosition() < this.string.length())
			{
				throw new ParseException("Unexpected: " + (char) parser.getChar(), parser.getPosition());
			}
			
			return x;
		}
		
		@Override
		public String toString()
		{
			return this.string;
		}
		
		private static class Parser
		{
			private final String string;
			private final BigDecimal ans;
			private int position;
			private int character;
			
			public Parser(String string, BigDecimal ans)
			{
				this.string = string;
				this.ans = ans;
				this.position = 0;
				this.character = this.string.isEmpty() ? -1 : this.string.charAt(0);
			}
			
			private boolean consume(int c)
			{
				while(this.getChar() == ' ')
				{
					this.next();
				}
				
				if(this.getChar() == c)
				{
					this.next();
					return true;
				}
				
				return false;
			}
			
			private void consumeExpected(char c) throws ParseException
			{
				if(!this.consume(c))
				{
					throw new ParseException("Expected: " + c, this.getPosition());
				}
			}
			
			public BigDecimal parseExpression() throws ParseException
			{
				BigDecimal x = this.parseTerm();
				
				while(true)
				{
					if(this.consume('+'))
					{
						x = x.add(this.parseTerm());
					}
					else if(this.consume('-'))
					{
						x = x.subtract(this.parseTerm());
					}
					else
					{
						return x;
					}
				}
			}
			
			private BigDecimal parseTerm() throws ParseException
			{
				BigDecimal x = this.parseFactor();
				
				while(true)
				{
					if(this.consume('*'))
					{
						x = x.multiply(this.parseFactor(), MATH_CONTEXT);
					}
					else if(this.consume('/'))
					{
						BigDecimal y = this.parseFactor();
						
						if(y.equals(BigDecimal.ZERO))
						{
							throw new ParseException("Division by zero", this.position);
						}
						
						x = x.divide(y, MATH_CONTEXT);
					}
					else if(this.getChar() >= 'a' && this.getChar() <= 'z')
					{
						int start = this.getPosition();
						
						while(this.getChar() >= 'a' && this.getChar() <= 'z')
						{
							this.next();
						}
						
						String function = this.string.substring(start, this.getPosition());
						
						if(function.equals("mod"))
						{
							x = x.remainder(this.parseExpression(), MATH_CONTEXT);
						}
						else
						{
							throw new ParseException("Unknown operator: " + function, start);
						}
					}
					else
					{
						return x;
					}
				}
			}
			
			private BigDecimal parseArgument() throws ParseException
			{
				this.consumeExpected('(');
				BigDecimal x = this.parseExpression();
				this.consumeExpected(')');
				return x;
			}
			
			private BigDecimal parseArguments(BiFunction<BigDecimal, BigDecimal, BigDecimal> function, boolean multipleArgs) throws ParseException
			{
				this.consumeExpected('(');
				BigDecimal x = this.parseExpression();
				this.consumeExpected(',');
				x = function.apply(x, this.parseExpression());
				
				if(multipleArgs)
				{
					while(this.consume(','))
					{
						x = function.apply(x, this.parseExpression());
					}
				}
				
				this.consumeExpected(')');
				return x;
			}
			
			private BigDecimal parseFactor() throws ParseException
			{
				if(this.consume('+'))
				{
					return this.parseFactor();
				}
				
				if(this.consume('-'))
				{
					return this.parseFactor().negate(MATH_CONTEXT);
				}
				
				BigDecimal x;
				
				if(this.consume('('))
				{
					x = this.parseExpression();
					this.consumeExpected(')');
				}
				else if((this.character >= '0' && this.character <= '9') || this.character == '.')
				{
					int start = this.getPosition();
					
					while((this.character >= '0' && this.character <= '9') || this.character == '.')
					{
						this.next();
					}
					
					try
					{
						x = BigDecimalMath.toBigDecimal(this.string.substring(start, this.getPosition()), MATH_CONTEXT);
					}
					catch(NumberFormatException e)
					{
						throw new ParseException(e.getMessage(), start);
					}
				}
				else if(this.getChar() >= 'a' && this.getChar() <= 'z')
				{
					int start = this.getPosition();
					
					while(this.getChar() >= 'a' && this.getChar() <= 'z')
					{
						this.next();
					}
					
					String function = this.string.substring(start, this.getPosition());
					
					switch(function)
					{
						case "e":
							x = E;
							break;
						case "pi":
							x = PI;
							break;
						case "ans":
							if(this.ans == null)
							{
								throw new ParseException("No value for ans", start);
							}
							x = this.ans;
							break;
						case "sqrt":
							x = this.parseArgument();
							
							try
							{
								x = BigDecimalMath.sqrt(x, MATH_CONTEXT);
							}
							catch(ArithmeticException e)
							{
								throw new ParseException("Undefined input for function sqrt: " + DECIMAL_FORMAT.format(x), start);
							}
							
							break;
						case "ceil":
							x = this.parseArgument().round(MATH_CONTEXT_CEIL);
							break;
						case "floor":
							x = this.parseArgument().round(MATH_CONTEXT_FLOOR);
							break;
						case "rad":
							x = this.parseArgument().multiply(TO_RADIANS, MATH_CONTEXT);
							break;
						case "deg":
							x = this.parseArgument().multiply(TO_DEGREES, MATH_CONTEXT);
							break;
						case "round":
							x = this.parseArgument().round(MATH_CONTEXT);
							break;
						case "log":
							x = this.parseArgument();
							
							try
							{
								x = BigDecimalMath.log10(x, MATH_CONTEXT);
							}
							catch(ArithmeticException e)
							{
								throw new ParseException("Undefined input for function log: " + DECIMAL_FORMAT.format(x), start);
							}
							
							break;
						case "ln":
							x = this.parseArgument();
							
							try
							{
								x = BigDecimalMath.log(x, MATH_CONTEXT);
							}
							catch(ArithmeticException e)
							{
								throw new ParseException("Undefined input for function ln: " + DECIMAL_FORMAT.format(x), start);
							}
							
							break;
						case "sin":
							x = BigDecimalMath.sin(this.parseArgument(), MATH_CONTEXT);
							break;
						case "cos":
							x = BigDecimalMath.cos(this.parseArgument(), MATH_CONTEXT);
							break;
						case "tan":
							x = BigDecimalMath.tan(this.parseArgument(), MATH_CONTEXT);
							break;
						case "sinh":
							x = BigDecimalMath.sinh(this.parseArgument(), MATH_CONTEXT);
							break;
						case "cosh":
							x = BigDecimalMath.cosh(this.parseArgument(), MATH_CONTEXT);
							break;
						case "tanh":
							x = BigDecimalMath.tanh(this.parseArgument(), MATH_CONTEXT);
							break;
						case "asin":
							x = this.parseArgument();
							
							try
							{
								x = BigDecimalMath.asin(x, MATH_CONTEXT);
							}
							catch(ArithmeticException e)
							{
								throw new ParseException("Undefined input for function asin: " + DECIMAL_FORMAT.format(x), start);
							}
							
							break;
						case "acos":
							x = this.parseArgument();
							
							try
							{
								x = BigDecimalMath.acos(x, MATH_CONTEXT);
							}
							catch(ArithmeticException e)
							{
								throw new ParseException("Undefined input for function acos: " + DECIMAL_FORMAT.format(x), start);
							}
							
							break;
						case "atan":
							x = BigDecimalMath.atan(this.parseArgument(), MATH_CONTEXT);
							break;
						case "asinh":
							x = BigDecimalMath.asinh(this.parseArgument(), MATH_CONTEXT);
							break;
						case "acosh":
							x = BigDecimalMath.acosh(this.parseArgument(), MATH_CONTEXT);
							break;
						case "atanh":
							x = BigDecimalMath.atanh(this.parseArgument(), MATH_CONTEXT);
							break;
						case "min":
							x = this.parseArguments((a, b) -> a.compareTo(b) < 0 ? a : b, true);
							break;
						case "max":
							x = this.parseArguments((a, b) -> a.compareTo(b) < 0 ? b : a, true);
							break;
						default:
							throw new ParseException("Unknown function: " + function, start);
					}
				}
				else
				{
					throw new ParseException("Unexpected: " + (char) this.getChar(), this.getPosition());
				}
				
				if(this.consume('^'))
				{
					x = BigDecimalMath.pow(x, this.parseFactor(), MATH_CONTEXT);
				}
				else if(this.consume('!'))
				{
					x = BigDecimalMath.factorial(x, MATH_CONTEXT);
				}
				
				return x;
			}
			
			public int getPosition()
			{
				return this.position;
			}
			
			public int getChar()
			{
				return this.character;
			}
			
			public void next()
			{
				this.character = (++this.position < this.string.length()) ? this.string.charAt(this.position) : -1;
			}
		}
		
		public static class ParseException extends Exception
		{
			private static final long serialVersionUID = -7352867054559488848L;
			
			public ParseException(String message, int position)
			{
				super(message + " at position " + position);
			}
		}
	}
}
