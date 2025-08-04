package net.kardexo.bot.adapters.ts3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.theholywaffle.teamspeak3.TS3Query;
import net.kardexo.bot.domain.config.Config;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import static net.kardexo.bot.domain.Util.OBJECT_MAPPER;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TS3ConfigAdapter extends Config
{
	@JsonProperty("host_address")
	private String hostAddress;
	@JsonProperty("login_name")
	private String loginName;
	@JsonProperty("login_password")
	private String loginPassword;
	@JsonProperty("channel_name")
	private String channelName;
	@JsonProperty("protocol")
	private TS3Query.Protocol protocol;
	
	public TS3ConfigAdapter()
	{
		super();
	}
	
	public String getHostAddress()
	{
		return this.hostAddress;
	}
	
	public String getLoginName()
	{
		return this.loginName;
	}
	
	public String getLoginPassword()
	{
		return this.loginPassword;
	}
	
	public String getChannelName()
	{
		return this.channelName;
	}
	
	public @NotNull TS3Query.Protocol getProtocol()
	{
		if(this.protocol == null)
		{
			return TS3Query.Protocol.RAW;
		}
		
		return this.protocol;
	}
	
	public static TS3ConfigAdapter of(String file) throws IOException
	{
		return OBJECT_MAPPER.readValue(new File(file), TS3ConfigAdapter.class);
	}
}
