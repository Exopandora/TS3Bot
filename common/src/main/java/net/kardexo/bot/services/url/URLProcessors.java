package net.kardexo.bot.services.url;

import net.kardexo.bot.services.api.IURLProcessor;
import net.kardexo.bot.services.api.IURLProcessorRegistrar;
import net.kardexo.bot.adapters.url.SteamURLProcessor;
import net.kardexo.bot.adapters.url.TwitchURLProcessor;
import net.kardexo.bot.adapters.url.TwitterURLProcessor;
import net.kardexo.bot.adapters.url.Watch2GetherURLProcessor;
import net.kardexo.bot.adapters.url.YouTubeURLProcessor;
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
