package net.kardexo.launchwrapper;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Start
{
	public static void main(String[] args) throws IOException, InterruptedException
	{
		GameServer gameserver = new ObjectMapper().readValue(new File("launchwrapper.json"), GameServer.class);
		Launchwrapper wrapper = new Launchwrapper(gameserver);
		wrapper.start();
	}
}
