package net.kardexo.bot.adapters.commands.arguments;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.kardexo.bot.adapters.ts3.TS3BotClientAdapter;
import net.kardexo.bot.domain.api.IBotClient;
import net.kardexo.bot.domain.api.IClient;

import java.util.AbstractMap.SimpleEntry;
import java.util.Comparator;

public class TS3ClientArgumentType implements ArgumentType<IClient>
{
	private static final DynamicCommandExceptionType USER_NOT_FOUND = new DynamicCommandExceptionType(username -> new LiteralMessage("Could not find user " + username));
	
	private final IBotClient bot;
	
	private TS3ClientArgumentType(IBotClient bot)
	{
		this.bot = bot;
	}
	
	@Override
	public IClient parse(StringReader reader) throws CommandSyntaxException
	{
		String username = normalize(reader.getRemaining());
		SimpleEntry<IClient, String> result = ((TS3BotClientAdapter) this.bot).getServer().getClients().stream()
			.map(client -> new SimpleEntry<IClient, String>(client, normalize(client.getName())))
			.filter(pair -> username.startsWith(pair.getValue()))
			.max(Comparator.comparingInt(pair -> pair.getValue().length()))
			.orElseThrow(() -> USER_NOT_FOUND.createWithContext(reader, username));
		reader.setCursor(reader.getCursor() + result.getValue().length());
		return result.getKey();
	}
	
	private static String normalize(String string)
	{
		return string.replaceAll("\\s+", " ").trim().toLowerCase();
	}
	
	public static TS3ClientArgumentType client(IBotClient bot)
	{
		return new TS3ClientArgumentType(bot);
	}
}
