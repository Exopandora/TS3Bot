package net.kardexo.bot.services.api;

import net.kardexo.bot.domain.ChatHistory;
import org.jetbrains.annotations.NotNull;

public interface IURLMessageProcessor extends IMessageProcessor
{
	String processMessage(String message, @NotNull ChatHistory chatHistory);
	
	String processMessage(String message);
}
