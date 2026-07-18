package net.kardexo.bot.domain.chat.message;

import net.kardexo.bot.api.IAPIKeyService;

import java.util.List;
import java.util.ServiceLoader;

public interface IURLProcessorRegistrar
{
	ServiceLoader<IURLProcessorRegistrar> INSTANCE = ServiceLoader.load(IURLProcessorRegistrar.class);
	
	void register(List<IURLProcessor> registrar, IAPIKeyService apiKeyService);
}
