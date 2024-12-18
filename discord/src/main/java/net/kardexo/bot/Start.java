package net.kardexo.bot;

import net.kardexo.bot.adapters.discord.DiscordBotAdapter;
import net.kardexo.bot.adapters.discord.DiscordConfigAdapter;

public class Start
{
	public static void main(String[] args) throws Exception
	{
		System.setProperty("slf4j.internal.verbosity", "WARN");
		new DiscordBotAdapter(DiscordConfigAdapter.of("config.json")).start();
	}
}
