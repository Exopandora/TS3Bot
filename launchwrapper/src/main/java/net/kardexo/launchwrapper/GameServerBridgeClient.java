package net.kardexo.launchwrapper;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class GameServerBridgeClient implements Runnable, Closeable
{
	private final Process process;
	private final Socket socket;
	private final PrintWriter output;
	private final BufferedReader input;
	
	public GameServerBridgeClient(Process process, Socket socket, PrintWriter output) throws IOException
	{
		this.process = process;
		this.socket = socket;
		this.output = output;
		this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}
	
	@Override
	public void run()
	{
		try
		{
			String line = null;
			
			while((line = this.input.readLine()) != null)
			{
				if(line.isEmpty())
				{
					this.process.destroy();
				}
				else
				{
					this.output.println(line);
					this.output.flush();
				}
			}
		}
		catch(SocketException e)
		{
			return;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void close() throws IOException
	{
		this.socket.close();
	}
}