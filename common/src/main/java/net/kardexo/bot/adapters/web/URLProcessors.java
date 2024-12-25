package net.kardexo.bot.adapters.web;

import net.kardexo.bot.adapters.web.impl.SteamURLProcessor;
import net.kardexo.bot.adapters.web.impl.TwitchURLProcessor;
import net.kardexo.bot.adapters.web.impl.TwitterURLProcessor;
import net.kardexo.bot.adapters.web.impl.Watch2GetherURLProcessor;
import net.kardexo.bot.adapters.web.impl.YouTubeURLProcessor;
import net.kardexo.bot.services.api.IAPIKeyService;

import java.util.List;

public class URLProcessors implements IURLProcessorRegistrar
{
	@Override
	public void register(List<IURLProcessor> registrar, IAPIKeyService apiKeyService)
	{
		registrar.add(new SteamURLProcessor(apiKeyService));
		registrar.add(new TwitchURLProcessor(apiKeyService));
		registrar.add(new YouTubeURLProcessor(apiKeyService));
		registrar.add(new TwitterURLProcessor(apiKeyService));
		registrar.add(new Watch2GetherURLProcessor());
	}
}
