package net.kardexo.bot.services.api;

import java.util.List;
import java.util.ServiceLoader;

public interface IURLProcessorRegistrar
{
	ServiceLoader<IURLProcessorRegistrar> INSTANCE = ServiceLoader.load(IURLProcessorRegistrar.class);
	
	void register(List<IURLProcessor> registrar, IAPIKeyService apiKeyService);
}
