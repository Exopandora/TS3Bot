package net.kardexo.ts3bot;

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.kardexo.ts3bot.config.Config;

public class Start
{
	public static void main(String[] args) throws Exception
	{
		System.setProperty("log4j.shutdownHookEnabled", Boolean.toString(false));
		Config config = new ObjectMapper().readValue(new File("config.json"), Config.class);
		TS3Bot bot = new TS3Bot(config);
		bot.start();
	}
}
