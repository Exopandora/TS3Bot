package net.kardexo.bot.domain.api;

import net.kardexo.bot.domain.config.Config;

import java.io.File;
import java.io.IOException;

public interface IConfigFactory<T extends Config>
{
	T create(File configFile) throws IOException;
}
