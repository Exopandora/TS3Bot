package net.kardexo.ts3bot;

import java.io.File;

public class Start
{
	public static void main(String[] args) throws Exception
	{
		System.setProperty("log4j.shutdownHookEnabled", Boolean.toString(false));
		TS3Bot bot = new TS3Bot(new File("config.json"));
		bot.start();
	}
}
