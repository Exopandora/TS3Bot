package net.kardexo.bot.domain.services;

import net.kardexo.bot.api.IConfigService;
import net.kardexo.bot.domain.config.Config;
import net.kardexo.bot.domain.config.IConfigFactory;

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
