package net.kardexo.bot.domain.services.url;

import net.kardexo.bot.api.IAPIKeyService;
import net.kardexo.bot.domain.chat.message.IURLProcessor;
import net.kardexo.bot.domain.chat.message.IURLProcessorRegistrar;
import net.kardexo.bot.output.services.url.SteamURLProcessor;
import net.kardexo.bot.output.services.url.TwitchURLProcessor;
import net.kardexo.bot.output.services.url.TwitterURLProcessor;
import net.kardexo.bot.output.services.url.Watch2GetherURLProcessor;
import net.kardexo.bot.output.services.url.YouTubeURLProcessor;

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
