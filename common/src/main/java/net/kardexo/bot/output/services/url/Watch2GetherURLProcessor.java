package net.kardexo.bot.output.services.url;

import net.kardexo.bot.domain.chat.message.IURLProcessor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Watch2GetherURLProcessor implements IURLProcessor {
	private static final Pattern WATCH2GETHER_URL = Pattern.compile("https://(?:www\\.)?watch2gether\\.com/rooms/([^?/]+)(?:.*)");
	private static final Pattern W2G_URL = Pattern.compile("https://(?:www\\.)?w2g\\.tv/rooms/([^?/]+)(?:.*)");
	
	@Override
	public String process(String url) {
		String id = this.extractRoomId(url);
		if (id != null) {
			return "Watch2Gether room " + id;
		}
		return null;
	}
	
	@Override
	public boolean isApplicable(String message) {
		return message != null && (WATCH2GETHER_URL.matcher(message).matches() || W2G_URL.matcher(message).matches());
	}
	
	private String extractRoomId(String url) {
		Matcher watch2getherMatcher = WATCH2GETHER_URL.matcher(url);
		if (watch2getherMatcher.matches() && watch2getherMatcher.group(1) != null) {
			return watch2getherMatcher.group(1);
		}
		Matcher w2gMatcher = W2G_URL.matcher(url);
		if (w2gMatcher.matches() && w2gMatcher.group(1) != null) {
			return w2gMatcher.group(1);
		}
		return null;
	}
}
