package net.kardexo.bot.adapters.commands.arguments;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.User;
import net.kardexo.bot.adapters.discord.DiscordBotClientAdapter;
import net.kardexo.bot.adapters.discord.DiscordClientAdapter;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.api.IClient;

public class DiscordClientArgumentType implements ArgumentType<IClient>
{
	private static final DynamicCommandExceptionType USER_NOT_FOUND = new DynamicCommandExceptionType(id -> new LiteralMessage("Could not find user <@" + id + ">"));
	
	private final IBotClient bot;
	
	private DiscordClientArgumentType(IBotClient bot)
	{
		this.bot = bot;
	}
	
	@Override
	public IClient parse(StringReader reader) throws CommandSyntaxException
	{
		reader.expect('<');
		reader.expect('@');
		long id = reader.readLong();
		reader.expect('>');
		User user = ((DiscordBotClientAdapter) this.bot).getGatewayDiscordClient().getUserById(Snowflake.of(id))
			.blockOptional()
			.orElseThrow(() -> USER_NOT_FOUND.create(id));
		return new DiscordClientAdapter(user);
	}
	
	public static DiscordClientArgumentType client(IBotClient bot)
	{
		return new DiscordClientArgumentType(bot);
	}
}
