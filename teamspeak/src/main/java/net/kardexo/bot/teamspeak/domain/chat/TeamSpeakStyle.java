package net.kardexo.bot.teamspeak.domain.chat;

import net.kardexo.bot.domain.chat.IStyle;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class TeamSpeakStyle implements IStyle
{
	protected static final TeamSpeakStyle BOLD = new TeamSpeakStyle("b");
	protected static final TeamSpeakStyle UNDERLINED = new TeamSpeakStyle("u");
	protected static final TeamSpeakStyle ITALIC = new TeamSpeakStyle("i");
	protected static final TeamSpeakStyle STRIKETHROUGH = new TeamSpeakStyle("s");
	
	private final String code;
	private final @Nullable String value;
	
	protected TeamSpeakStyle(String code)
	{
		this(code, null);
	}
	
	protected TeamSpeakStyle(String code, @Nullable String value)
	{
		this.code = code;
		this.value = value;
	}
	
	@Override
	public StringBuilder apply(StringBuilder builder, String string)
	{
		builder.append("[");
		builder.append(this.code);
		
		if(this.value != null)
		{
			builder.append("=");
			builder.append(this.value);
		}
		
		builder.append("]");
		builder.append(string);
		builder.append("[/");
		builder.append(this.code);
		builder.append("]");
		return builder;
	}
	
	@Override
	public boolean equals(Object object)
	{
		if(!(object instanceof TeamSpeakStyle tsStyle))
		{
			return false;
		}
		
		return Objects.equals(this.code, tsStyle.code) && Objects.equals(this.value, tsStyle.value);
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(this.code, this.value);
	}
}
