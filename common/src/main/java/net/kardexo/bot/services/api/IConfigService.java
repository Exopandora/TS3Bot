package net.kardexo.bot.services.api;

import net.kardexo.bot.domain.config.Config;

import java.io.IOException;

public interface IConfigService<T extends Config>
{
	T getConfig();
	
	void reload() throws IOException;
}
