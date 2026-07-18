package net.kardexo.bot.domain.config;

import java.io.File;
import java.io.IOException;

public interface IConfigFactory<T extends Config> {
	T create(File configFile) throws IOException;
}
