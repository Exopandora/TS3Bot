package net.kardexo.bot.api;

import net.kardexo.bot.domain.client.IClient;

public interface IPermissionService
{
	boolean hasPermission(IClient client, String permission);
}
