package net.kardexo.bot.discord.domain.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.kardexo.bot.domain.config.Config;

import java.io.File;
import java.io.IOException;

import static net.kardexo.bot.domain.Util.OBJECT_MAPPER;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscordConfig extends Config {
	@JsonProperty("token")
	private String token;
	
	public DiscordConfig() {
		super();
	}
	
	public String getToken() {
		return this.token;
	}
	
	@Override
	public void reload(File configFile) throws IOException {
		OBJECT_MAPPER.readerForUpdating(this).readValue(configFile);
	}
	
	public static DiscordConfig of(File configFile) throws IOException {
		return OBJECT_MAPPER.readValue(configFile, DiscordConfig.class);
	}
}
