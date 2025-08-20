package net.kardexo.bot.services;

import net.kardexo.bot.domain.api.IConfigFactory;
import net.kardexo.bot.domain.config.Config;
import net.kardexo.bot.services.api.IConfigService;

import java.io.File;
import java.io.IOException;

public class ConfigService<T extends Config> implements IConfigService<T>
{
	private final File configFile;
	private final T config;
	
	public ConfigService(String configFile, IConfigFactory<T> configFactory) throws IOException
	{
		this.configFile = new File(configFile);
		this.config = configFactory.create(this.configFile);
	}
	
	@Override
	public T getConfig()
	{
		return this.config;
	}
	
	@Override
	public void reload() throws IOException
	{
		this.config.reload(this.configFile);
	}
}
