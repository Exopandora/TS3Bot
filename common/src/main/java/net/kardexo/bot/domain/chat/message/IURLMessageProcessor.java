package net.kardexo.bot.domain.chat.message;

import net.kardexo.bot.domain.chat.ChatHistory;
import org.jetbrains.annotations.NotNull;

public interface IURLMessageProcessor extends IMessageProcessor
{
	String processMessage(String message, @NotNull ChatHistory chatHistory);
	
	String processMessage(String message);
}
