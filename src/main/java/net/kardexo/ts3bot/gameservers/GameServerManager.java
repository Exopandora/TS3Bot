package net.kardexo.ts3bot.gameservers;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GameServerManager extends Thread implements Closeable
{
	private static final String OS = String.valueOf(System.getProperty("os.name")).toLowerCase();
	private static final Logger LOGGER = LogManager.getLogger(GameServerManager.class);
	
	private final ServerSocket server;
	private final Map<String, GameServerBridgeServer> bridges = new HashMap<String, GameServerBridgeServer>();
	private final Map<String, File> servers;
	
	public GameServerManager(Map<String, File> servers) throws IOException
	{
		this.server = new ServerSocket(4915);
		this.servers = servers;
	}
	
	@Override
	public void run()
	{
		while(!this.isInterrupted())
		{
			Optional<GameServerBridgeServer> optional = this.accept();
			
			if(optional.isPresent())
			{
				GameServerBridgeServer bridge = optional.get();
				this.bridges.put(bridge.getServerId(), bridge);
			}
		}
		
		try
		{
			this.server.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private Optional<GameServerBridgeServer> accept()
	{
		try
		{
			Socket socket = this.server.accept();
			BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			GameServer server = new ObjectMapper().readValue(input.readLine(), GameServer.class);
			
			if(!this.isServerRunning(server.getId()))
			{
				GameServerManager.LOGGER.info("Gameserver " + server.getId() + " connected");
				GameServerBridgeServer bridge = new GameServerBridgeServer(server, socket, input, () ->
				{
					GameServerManager.LOGGER.info("Gameserver " + server.getId() + " disconnected");
					this.bridges.remove(server.getId());
				});
				bridge.start();
				return Optional.of(bridge);
			}
			else
			{
				socket.getOutputStream().write("\n".getBytes());
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		return Optional.empty();
	}
	
	public void sendCommand(String id, String command)
	{
		GameServerBridgeServer bridge = this.bridges.get(id);
		
		if(bridge != null)
		{
			bridge.sendCommand(command);
		}
	}
	
	public int startServer(String id) throws IOException, InterruptedException
	{
		File file = this.servers.get(id);
		StringBuilder command = new StringBuilder();
		
		if(OS.contains("win"))
		{
			command.append("cmd /c \"" + file.getAbsolutePath() + "\"");
		}
		else
		{
			command.append(file.getAbsolutePath().replaceAll("[^\\] ", "\\ ") + " > nul 2>&1");
		}
		
		return new ProcessBuilder(command.toString()).directory(file.getParentFile()).inheritIO().start().waitFor();
	}
	
	public void stopServer(String id)
	{
		GameServerBridgeServer bridge = this.bridges.remove(id);
		
		if(bridge != null)
		{
			bridge.stopServer();
		}
	}
	
	public boolean isServerRunning(String id)
	{
		return this.bridges.containsKey(id);
	}
	
	public List<String> getRunningServerIds()
	{
		return this.bridges.keySet().stream().sorted().collect(Collectors.toUnmodifiableList());
	}
	
	public List<String> getAvailableServerIds()
	{
		return this.servers.keySet().stream().filter(key -> !this.bridges.containsKey(key)).sorted().collect(Collectors.toUnmodifiableList());
	}
	
	@Override
	public void close() throws IOException
	{
		this.server.close();
	}
}
