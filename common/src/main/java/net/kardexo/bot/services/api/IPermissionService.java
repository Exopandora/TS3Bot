package net.kardexo.bot.services.api;

import net.kardexo.bot.domain.api.IClient;

public interface IPermissionService
{
	boolean hasPermission(IClient client, String permission);
}
