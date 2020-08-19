package net.kardexo.launchwrapper;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Scanner;

public class Launchwrapper
{
	private final GameServer gameserver;
	private GameServerManager manager;
	
	public Launchwrapper(GameServer gameserver)
	{
		this.gameserver = gameserver;
	}
	
	public void start() throws InterruptedException, IOException
	{
		Process process = new ProcessBuilder(this.gameserver.getStartCommand().split(" ")).redirectOutput(Redirect.INHERIT).redirectError(Redirect.INHERIT).start();
		
		this.manager = new GameServerManager(process, this.gameserver);
		this.manager.start();
		
		Thread input = new Thread(this::readInput);
		input.setDaemon(true);
		input.start();
		
		process.waitFor();
		
		this.manager.interrupt();
		this.manager.close();
	}
	
	private void readInput()
	{
		try(Scanner scanner = new Scanner(System.in))
		{
			while(scanner.hasNextLine())
			{
				this.manager.sendCommand(scanner.nextLine());
			}
		}
	}
}
