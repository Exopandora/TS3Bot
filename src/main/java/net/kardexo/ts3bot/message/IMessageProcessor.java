package net.kardexo.ts3bot.message;

import com.github.theholywaffle.teamspeak3.api.TextMessageTargetMode;

import net.kardexo.ts3bot.TS3Bot;

public interface IMessageProcessor
{
	void process(TS3Bot bot, String message, int invokerId, TextMessageTargetMode targetMode);
	
	boolean isApplicable(TS3Bot bot, String message, int invokerId, TextMessageTargetMode targetMode);
}
