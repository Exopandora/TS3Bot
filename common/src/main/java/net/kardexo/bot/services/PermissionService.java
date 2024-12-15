package net.kardexo.bot.services;

import net.kardexo.bot.domain.api.IClient;
import net.kardexo.bot.services.api.IPermissionService;

import java.util.Map;
import java.util.Set;

public class PermissionService implements IPermissionService
{
	private final Map<String, Set<String>> permissions;
	
	public PermissionService(Map<String, Set<String>> permissions)
	{
		this.permissions = permissions;
	}
	
	@Override
	public boolean hasPermission(IClient client, String permission)
	{
		Set<String> group = this.permissions.get(permission);
		
		if(group != null)
		{
			return group.contains(client.getUniqueId());
		}
		
		return false;
	}
}
