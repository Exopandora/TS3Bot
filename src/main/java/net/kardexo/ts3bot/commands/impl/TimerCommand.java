package net.kardexo.ts3bot.commands.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;
import net.kardexo.ts3bot.commands.arguments.DurationArgumentType;
import net.kardexo.ts3bot.util.TimerThread;
import net.kardexo.ts3bot.util.TimerThread.Timer;
import net.kardexo.ts3bot.util.Util;

public class TimerCommand
{
	private static final SimpleCommandExceptionType NO_TIMER_SET = new SimpleCommandExceptionType(new LiteralMessage("No timer set"));
	private static final TimerThread TIMER = new TimerThread();
	
	static
	{
		TIMER.start();
	}
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("timer")
			.executes(context -> timer(context))
			.then(Commands.literal("reset")
				.executes(context -> reset(context)))
			.then(Commands.argument("duration", DurationArgumentType.duration())
				.executes(context -> timer(context, DurationArgumentType.getDuration(context, "duration")))));
	}
	
	private static int timer(CommandContext<CommandSource> context) throws CommandSyntaxException
	{
		Optional<Timer> optional = TIMER.getTimer(context.getSource().getClientInfo().getUniqueIdentifier());
		
		if(optional.isEmpty())
		{
			throw NO_TIMER_SET.create();
		}
		
		Timer timer = optional.get();
		context.getSource().sendFeedback("Timer ends in " + Util.formatDuration(Duration.between(Instant.now(), timer.getEnd()).toSeconds()));
		return (int) Duration.between(Instant.now(), timer.getEnd()).toSeconds();
	}
	
	private static int timer(CommandContext<CommandSource> context, Duration duration) throws CommandSyntaxException
	{
		String uid = context.getSource().getClientInfo().getUniqueIdentifier();
		TIMER.setTimer(uid, Instant.now().plus(duration), () -> context.getSource().sendPrivateMessage("Timer has ended!"));
		context.getSource().sendFeedback("Timer has been set to " + Util.formatDuration(duration.toSeconds()));
		return (int) duration.toSeconds();
	}
	
	private static int reset(CommandContext<CommandSource> context) throws CommandSyntaxException
	{
		Optional<Timer> optional = TIMER.resetTimer(context.getSource().getClientInfo().getUniqueIdentifier());
		
		if(optional.isEmpty())
		{
			throw NO_TIMER_SET.create();
		}
		
		Timer timer = optional.get();
		context.getSource().sendFeedback("Timer has been reset");
		return (int) Duration.between(Instant.now(), timer.getEnd()).toSeconds();
	}
}
