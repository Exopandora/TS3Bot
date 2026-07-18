package net.kardexo.bot.domain.services;

import net.kardexo.bot.api.IPermissionService;
import net.kardexo.bot.domain.client.IBotClient;
import net.kardexo.bot.domain.client.IClient;

import java.util.Map;
import java.util.Set;

public class PermissionService implements IPermissionService {
	private final Map<String, Set<String>> permissions;
	
	public PermissionService(Map<String, Set<String>> permissions) {
		this.permissions = permissions;
	}
	
	@Override
	public boolean hasPermission(IClient client, String permission) {
		if (client instanceof IBotClient) {
			return true;
		}
		Set<String> group = this.permissions.get(permission);
		if (group != null) {
			return group.contains(client.getId());
		}
		return false;
	}
}
