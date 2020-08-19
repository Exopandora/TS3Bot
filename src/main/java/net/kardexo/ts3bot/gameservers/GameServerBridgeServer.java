package net.kardexo.ts3bot.gameservers;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class GameServerBridgeServer extends Thread implements Closeable
{
	private final GameServer server;
	private final Socket socket;
	private final BufferedReader input;
	private final PrintWriter output;
	private final Runnable shutdownHook;
	
	public GameServerBridgeServer(GameServer server, Socket socket, BufferedReader input, Runnable shutdownHook) throws IOException
	{
		this.server = server;
		this.socket = socket;
		this.input = input;
		this.shutdownHook = shutdownHook;
		this.output = new PrintWriter(socket.getOutputStream());
	}
	
	@Override
	public void run()
	{
		try
		{
			while(this.input.readLine() != null);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		this.shutdownHook.run();
	}
	
	public void sendCommand(String command)
	{
		this.output.println(command);
		this.output.flush();
	}
	
	public void stopServer()
	{
		this.output.println(this.server.getStopCommand());
		this.output.flush();
	}
	
	public String getServerId()
	{
		return this.server.getId();
	}
	
	@Override
	public void close() throws IOException
	{
		this.socket.close();
	}
}