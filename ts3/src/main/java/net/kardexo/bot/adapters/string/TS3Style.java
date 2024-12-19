package net.kardexo.bot.adapters.string;

import net.kardexo.bot.domain.api.IStyle;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class TS3Style implements IStyle
{
	protected static final TS3Style BOLD = new TS3Style("b");
	protected static final TS3Style UNDERLINED = new TS3Style("u");
	protected static final TS3Style ITALIC = new TS3Style("i");
	protected static final TS3Style STRIKETHROUGH = new TS3Style("s");
	
	private final String code;
	private final @Nullable String value;
	
	protected TS3Style(String code)
	{
		this(code, null);
	}
	
	protected TS3Style(String code, @Nullable String value)
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
		if(!(object instanceof TS3Style ts3Style))
		{
			return false;
		}
		
		return Objects.equals(this.code, ts3Style.code) && Objects.equals(this.value, ts3Style.value);
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(this.code, this.value);
	}
}
