package net.kardexo.bot.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.kardexo.bot.services.api.IUserConfigService;
import net.kardexo.bot.domain.config.UserConfig;
import net.kardexo.bot.domain.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UserConfigService implements IUserConfigService
{
	private static final Logger logger = LoggerFactory.getLogger(UserConfigService.class);
	
	private final ObjectMapper objectMapper;
	private final Map<String, UserConfig> users;
	private final File file;
	
	public UserConfigService(File file, ObjectMapper objectMapper) throws IOException
	{
		this.file = file;
		this.objectMapper = objectMapper;
		this.users = Util.readJsonFile(this.file, this.objectMapper, new TypeReference<Map<String, UserConfig>>() {}, HashMap::new);
	}
	
	@Override
	public UserConfig getUserConfig(String user)
	{
		return this.users.getOrDefault(user, new UserConfig());
	}
	
	@Override
	public void saveUserConfig(String user, UserConfig config)
	{
		synchronized(this.users)
		{
			this.users.put(user, config);
			
			try
			{
				this.objectMapper.writeValue(this.file, this.users);
			}
			catch(Exception e)
			{
				logger.error("Error saving user config", e);
			}
		}
	}
}
