package net.kardexo.bot.domain;

import net.kardexo.bot.domain.api.IStyle;

public class FormattedStringBuilder
{
	private final StringBuilder builder;
	
	public FormattedStringBuilder()
	{
		this.builder = new StringBuilder();
	}
	
	public FormattedStringBuilder append(Object object)
	{
		return this.append(object.toString());
	}
	
	public FormattedStringBuilder append(String string)
	{
		this.builder.append(string);
		return this;
	}
	
	public FormattedStringBuilder append(Object object, IStyle... styles)
	{
		return this.append(object.toString(), styles);
	}
	
	public FormattedStringBuilder append(String string, IStyle... styles)
	{
		if(styles.length == 0)
		{
			this.builder.append(string);
		}
		else if(styles.length == 1)
		{
			styles[0].apply(this.builder, string);
		}
		else
		{
			String styled = string;
			
			for(IStyle style : styles)
			{
				styled = style.apply(styled);
			}
			
			this.builder.append(styled);
		}
		
		return this;
	}
	
	@Override
	public String toString()
	{
		return this.builder.toString();
	}
}
