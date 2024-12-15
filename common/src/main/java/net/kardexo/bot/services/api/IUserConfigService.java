package net.kardexo.bot.services.api;

import net.kardexo.bot.domain.config.UserConfig;

public interface IUserConfigService
{
	UserConfig getUserConfig(String user);
	
	void saveUserConfig(String user, UserConfig config);
}
