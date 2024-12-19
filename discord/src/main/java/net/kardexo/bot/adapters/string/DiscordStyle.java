package net.kardexo.bot.adapters.string;

import net.kardexo.bot.domain.api.IStyle;

import java.util.Objects;

public class DiscordStyle implements IStyle
{
	protected static final DiscordStyle BOLD = new DiscordStyle("**");
	protected static final DiscordStyle UNDERLINED = new DiscordStyle("__");
	protected static final DiscordStyle ITALIC = new DiscordStyle("*");
	protected static final DiscordStyle STRIKETHROUGH = new DiscordStyle("~~");
	protected static final DiscordStyle COLOR = new DiscordStyle("");
	
	private final String format;
	
	protected DiscordStyle(String format)
	{
		this.format = format;
	}
	
	@Override
	public StringBuilder apply(StringBuilder builder, String string)
	{
		builder.append(this.format);
		builder.append(string);
		builder.append(this.format);
		return builder;
	}
	
	@Override
	public boolean equals(Object object)
	{
		if(!(object instanceof DiscordStyle discordStyle))
		{
			return false;
		}
		
		return Objects.equals(this.format, discordStyle.format);
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(this.format);
	}
}
