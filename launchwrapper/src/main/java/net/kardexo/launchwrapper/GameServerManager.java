package net.kardexo.launchwrapper;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GameServerManager extends Thread implements Closeable
{
	private final Process process;
	private final GameServer server;
	private final PrintWriter output;
	
	private GameServerBridgeClient bridge;
	
	public GameServerManager(Process process, GameServer server)
	{
		this.process = process;
		this.server = server;
		this.output = new PrintWriter(process.getOutputStream());
	}
	
	@Override
	public void run()
	{
		while(!this.isInterrupted())
		{
			Optional<GameServerBridgeClient> optional = this.accept();
			
			if(optional.isPresent())
			{
				this.bridge = optional.get();
				this.bridge.run();
			}
		}
	}
	
	private Optional<GameServerBridgeClient> accept()
	{
		try
		{
			Socket socket = new Socket(InetAddress.getLocalHost(), 4915);
			PrintWriter output = new PrintWriter(socket.getOutputStream());
			output.println(new ObjectMapper().writeValueAsString(this.server));
			output.flush();
			return Optional.of(new GameServerBridgeClient(this.process, socket, this.output));
		}
		catch(IOException e)
		{
			return Optional.<GameServerBridgeClient>empty();
		}
	}
	
	public void sendCommand(String command)
	{
		this.output.println(command);
		this.output.flush();
	}
	
	@Override
	public void close() throws IOException
	{
		this.bridge.close();
	}
}
