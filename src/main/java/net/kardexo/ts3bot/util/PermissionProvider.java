package net.kardexo.ts3bot.util;

import com.github.theholywaffle.teamspeak3.api.wrapper.ClientInfo;

public interface PermissionProvider
{
	boolean hasPermission(ClientInfo clientInfo, String permission);
}
