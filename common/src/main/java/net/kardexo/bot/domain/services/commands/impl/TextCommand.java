package net.kardexo.bot.domain.services.commands.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.kardexo.bot.domain.commands.CommandSource;
import net.kardexo.bot.domain.config.Config;
import net.kardexo.bot.domain.services.commands.Commands;

import java.util.Map.Entry;

public class TextCommand {
	public static void register(CommandDispatcher<CommandSource> dispatcher, Config config) {
		for (Entry<String, JsonNode> entry : config.getShortcuts().getText().entrySet()) {
			dispatcher.register(Commands.literal(entry.getKey())
				.executes(context -> text(context, entry.getValue())));
		}
	}
	
	private static int text(CommandContext<CommandSource> context, JsonNode node) {
		if (node.isArray()) {
			int index = context.getSource().getRandomSource().nextInt(node.size());
			context.getSource().sendFeedback(node.get(index).asText());
			return index;
		}
		context.getSource().sendFeedback(node.asText());
		return 0;
	}
}
