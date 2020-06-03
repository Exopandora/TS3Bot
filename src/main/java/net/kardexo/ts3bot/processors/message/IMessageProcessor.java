package net.kardexo.ts3bot.processors.message;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.api.TextMessageTargetMode;
import com.github.theholywaffle.teamspeak3.api.wrapper.ClientInfo;

public interface IMessageProcessor
{
	boolean onMessage(String message, TS3Api api, ClientInfo clientInfo, TextMessageTargetMode target);
}
