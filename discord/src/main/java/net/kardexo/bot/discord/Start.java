package net.kardexo.bot.discord;

import net.kardexo.bot.discord.input.DiscordBot;

public class Start {
	public static void main(String[] args) throws Exception {
		System.setProperty("slf4j.internal.verbosity", "WARN");
		new DiscordBot("config.json").start();
	}
}
