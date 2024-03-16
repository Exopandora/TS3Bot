package net.kardexo.ts3bot.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.ts3bot.api.LeagueOfLegends.Region;
import net.kardexo.ts3bot.api.LeagueOfLegends.RiotId;

public class RiotIdArgumentType implements ArgumentType<RiotId>
{
	private final StringArgumentType delegate;
	private final Region defaultRegion;
	
	private RiotIdArgumentType(Region defaultRegion)
	{
		this.delegate = StringArgumentType.greedyString();
		this.defaultRegion = defaultRegion;
	}
	
	@Override
	public <S> RiotId parse(StringReader reader) throws CommandSyntaxException
	{
		return RiotId.parse(this.delegate.parse(reader), this.defaultRegion);
	}
	
	public static RiotIdArgumentType greedy(Region defaultRegion)
	{
		return new RiotIdArgumentType(defaultRegion);
	}
	
	public static RiotId getRiotId(final CommandContext<?> context, final String name)
	{
		return context.getArgument(name, RiotId.class);
	}
}
