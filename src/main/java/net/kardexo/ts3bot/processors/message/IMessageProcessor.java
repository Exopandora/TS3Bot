package net.kardexo.ts3bot.processors.message;

import com.github.manevolent.ts3j.api.Client;
import com.github.manevolent.ts3j.api.TextMessageTargetMode;

public interface IMessageProcessor
{
	boolean onMessage(String message, Client invoker, TextMessageTargetMode target);
}
