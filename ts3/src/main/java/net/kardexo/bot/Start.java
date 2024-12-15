package net.kardexo.bot;

import net.kardexo.bot.adapters.ts3.TS3BotAdapter;

import java.io.File;

public class Start
{
	public static void main(String[] args) throws Exception
	{
		System.setProperty("slf4j.internal.verbosity", "WARN");
		new TS3BotAdapter(new File("config.json")).start();
	}
}
