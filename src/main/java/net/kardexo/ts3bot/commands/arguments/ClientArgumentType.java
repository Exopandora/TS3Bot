package net.kardexo.ts3bot.commands.arguments;

import java.util.AbstractMap.SimpleEntry;
import java.util.Comparator;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.kardexo.ts3bot.TS3Bot;

public class ClientArgumentType implements ArgumentType<Client>
{
	private static final DynamicCommandExceptionType USER_NOT_FOUND = new DynamicCommandExceptionType(username -> new LiteralMessage("Could not find user " + username));
	
	private final TS3Bot ts3bot;
	
	private ClientArgumentType(TS3Bot ts3bot)
	{
		this.ts3bot = ts3bot;
	}
	
	@Override
	public <S> Client parse(StringReader reader) throws CommandSyntaxException
	{
		String username = normalize(reader.getRemaining());
		SimpleEntry<Client, String> result = this.ts3bot.getApi().getClients().stream()
			.map(client -> new SimpleEntry<Client, String>(client, normalize(client.getNickname())))
			.filter(pair -> username.startsWith(pair.getValue()))
			.sorted(Comparator.comparingInt(pair -> -pair.getValue().length()))
			.findFirst()
			.orElseThrow(() -> USER_NOT_FOUND.createWithContext(reader, username));
		reader.setCursor(reader.getCursor() + result.getValue().length());
		return result.getKey();
	}
	
	private static String normalize(String string)
	{
		return string.replaceAll("\\s+", " ").trim().toLowerCase();
	}
	
	public static ClientArgumentType client(TS3Bot ts3bot)
	{
		return new ClientArgumentType(ts3bot);
	}
	
	public static Client getClient(final CommandContext<?> context, final String name)
	{
		return context.getArgument(name, Client.class);
	}
}
