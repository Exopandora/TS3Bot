package net.kardexo.bot.domain.chat.message;

public interface IURLProcessor {
	String process(String url);
	
	boolean isApplicable(String message);
}
