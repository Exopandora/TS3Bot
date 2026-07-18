package net.kardexo.bot.teamspeak;

import net.kardexo.bot.teamspeak.input.TeamSpeakBot;

public class Start {
	public static void main(String[] args) throws Exception {
		System.setProperty("slf4j.internal.verbosity", "WARN");
		new TeamSpeakBot("config.json").start();
	}
}
