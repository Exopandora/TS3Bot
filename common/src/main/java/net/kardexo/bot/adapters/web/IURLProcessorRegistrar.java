package net.kardexo.bot.adapters.web;

import net.kardexo.bot.services.api.IAPIKeyService;

import java.util.List;
import java.util.ServiceLoader;

public interface IURLProcessorRegistrar
{
	ServiceLoader<IURLProcessorRegistrar> INSTANCE = ServiceLoader.load(IURLProcessorRegistrar.class);
	
	void register(List<IURLProcessor> registrar, IAPIKeyService apiKeyService);
}
