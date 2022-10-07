package net.kardexo.ts3bot.commands.arguments;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.kardexo.ts3bot.TS3Bot;

public class TS3UserArgumentType implements ArgumentType<Client>
{
	private static final DynamicCommandExceptionType USER_NOT_FOUND = new DynamicCommandExceptionType(username -> new LiteralMessage("Could not find user " + username));
	
	private final TS3Bot ts3bot;
	
	private TS3UserArgumentType(TS3Bot ts3bot)
	{
		this.ts3bot = ts3bot;
	}
	
	@Override
	public <S> Client parse(StringReader reader) throws CommandSyntaxException
	{
		String username = reader.getRemaining();
		reader.setCursor(reader.getTotalLength());
		
		for(Client client : this.ts3bot.getApi().getClients())
		{
			if(username.equalsIgnoreCase(client.getNickname()))
			{
				return client;
			}
		}
		
		throw USER_NOT_FOUND.createWithContext(reader, username);
	}
	
	public static TS3UserArgumentType client(TS3Bot ts3bot)
	{
		return new TS3UserArgumentType(ts3bot);
	}
	
	public static Client getClient(final CommandContext<?> context, final String name)
	{
		return context.getArgument(name, Client.class);
	}
}
