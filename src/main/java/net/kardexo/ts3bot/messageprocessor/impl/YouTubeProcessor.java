package net.kardexo.ts3bot.messageprocessor.impl;

import java.net.URL;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.api.TextMessageTargetMode;
import com.github.theholywaffle.teamspeak3.api.wrapper.ClientInfo;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.URLs;
import net.kardexo.ts3bot.messageprocessor.IMessageProcessor;

public class YouTubeProcessor implements IMessageProcessor
{
	private static final String BASE_URL = "https://www.googleapis.com/youtube/v3/";
	
	@Override
	public boolean onMessage(String message, TS3Api api, ClientInfo clientInfo, TextMessageTargetMode target)
	{
		if(URLs.isYouTube(message))
		{
			String id = URLs.getYouTubeWatchId(message);
			
			if(id == null)
			{
				return false;
			}
			
			StringBuilder url = new StringBuilder(BASE_URL + "videos");
			
			url.append("?id=" + id);
			url.append("&part=snippet");
			url.append("&key=" + TS3Bot.getInstance().getConfig().getApiYouTube());
			
			try
			{
				JsonNode node = new ObjectMapper().readTree(new URL(url.toString()));
				JsonNode items = node.path("items");
				
				if(items.size() == 1)
				{
					String title = items.get(0).path("snippet").path("title").asText();
					
					if(title != null && !title.isEmpty())
					{
						api.sendTextMessage(target, clientInfo.getId(), title);
						return true;
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		return false;
	}
}
