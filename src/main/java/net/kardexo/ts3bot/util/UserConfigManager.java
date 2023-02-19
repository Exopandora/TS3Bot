package net.kardexo.ts3bot.util;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

public class UserConfigManager
{
	private final ObjectMapper objectMapper;
	private final Map<String, UserConfig> users;
	private final File file;
	
	public UserConfigManager(File file, ObjectMapper objectMapper) throws IOException
	{
		this.file = file;
		this.objectMapper = objectMapper;
		this.users = Util.readJsonFile(this.file, this.objectMapper, new TypeReference<Map<String, UserConfig>>() {}, Maps::newHashMap);
	}
	
	public UserConfig getUserConfig(String user)
	{
		return this.users.getOrDefault(user, new UserConfig());
	}
	
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
				e.printStackTrace();
			}
		}
	}
	
	public static class UserConfig
	{
		@JsonProperty("leauge_of_legends_alias")
		private String leaugeOfLegendsAlias = null;
		
		public String getLeaugeOfLegendsAlias()
		{
			return leaugeOfLegendsAlias;
		}
		
		public void setLeaugeOfLegendsAlias(String leaugeOfLegendsAlias)
		{
			this.leaugeOfLegendsAlias = leaugeOfLegendsAlias;
		}
	}
}
