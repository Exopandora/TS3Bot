package net.kardexo.bot.testfixtures;

import net.kardexo.bot.domain.chat.IStyle;

import java.util.Objects;

public class TestStyle implements IStyle {
	protected static final TestStyle BOLD = new TestStyle("bold");
	protected static final TestStyle UNDERLINED = new TestStyle("underlined");
	protected static final TestStyle ITALIC = new TestStyle("italic");
	protected static final TestStyle STRIKETHROUGH = new TestStyle("strike");
	
	private final String code;
	
	protected TestStyle(String code) {
		this.code = code;
	}
	
	@Override
	public StringBuilder apply(StringBuilder builder, String string) {
		builder.append("<");
		builder.append(this.code);
		builder.append(">");
		builder.append(string);
		builder.append("</");
		builder.append(this.code);
		builder.append(">");
		return builder;
	}
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof TestStyle testStyle)) {
			return false;
		}
		
		return Objects.equals(this.code, testStyle.code);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.code);
	}
}
