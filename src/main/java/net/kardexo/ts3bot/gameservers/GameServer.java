package net.kardexo.ts3bot.gameservers;

import java.io.File;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GameServer
{
	@JsonProperty("id")
	private String id;
	@JsonProperty("start_command")
	private String startCommand;
	@JsonProperty("stop_command")
	private String stopCommand;
	
	public GameServer()
	{
		super();
	}
	
	public GameServer(String id, File path, String startCommand, String stopCommand)
	{
		this.id = id;
		this.startCommand = startCommand;
		this.stopCommand = stopCommand;
	}
	
	public String getId()
	{
		return this.id;
	}
	
	public String getStartCommand()
	{
		return this.startCommand;
	}
	
	public String getStopCommand()
	{
		return this.stopCommand;
	}
}
