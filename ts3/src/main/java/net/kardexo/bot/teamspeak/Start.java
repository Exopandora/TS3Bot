package net.kardexo.bot.teamspeak;

import net.kardexo.bot.teamspeak.input.TS3BotAdapter;

public class Start
{
	public static void main(String[] args) throws Exception
	{
		System.setProperty("slf4j.internal.verbosity", "WARN");
		new TS3BotAdapter("config.json").start();
	}
}
