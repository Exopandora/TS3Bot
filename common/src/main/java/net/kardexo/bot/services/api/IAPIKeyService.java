package net.kardexo.bot.services.api;

public interface IAPIKeyService
{
	String API_KEY_WATCH_2_GETHER = "watch_2_gether";
	String API_KEY_TWITCH = "twitch";
	String API_KEY_YOUTUBE = "youtube";
	String API_KEY_TWITTER = "twitter";
	String API_KEY_LEAGUE_OF_LEGENDS = "league_of_legends";
	String API_KEY_IMAGGA = "imagga";
	
	String requestKey(String key);
	
	String requestToken(String key, String token);
}
