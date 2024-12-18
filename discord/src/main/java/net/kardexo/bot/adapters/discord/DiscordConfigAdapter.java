package net.kardexo.bot.adapters.discord;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.kardexo.bot.domain.config.Config;

import java.io.File;
import java.io.IOException;

import static net.kardexo.bot.domain.Util.OBJECT_MAPPER;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscordConfigAdapter extends Config
{
	@JsonProperty("token")
	private String token;
	
	public DiscordConfigAdapter()
	{
		super();
	}
	
	public String getToken()
	{
		return this.token;
	}
	
	public static DiscordConfigAdapter of(String file) throws IOException
	{
		return OBJECT_MAPPER.readValue(new File(file), DiscordConfigAdapter.class);
	}
}
