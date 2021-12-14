package net.kardexo.ts3bot.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import net.kardexo.ts3bot.TS3Bot;

public class Util
{
	public static final Pattern URL_PATTERN = Pattern.compile("https?:\\/\\/[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
	public static final Pattern WRAPPED_URL_PATTERN = Pattern.compile("\\[URL\\].*\\[\\/URL\\]");
	public static final String QUERY_SPLIT = "\\?|&";
	public static final Pattern PARAMETER_PATTERN = Pattern.compile("([^=]+)=([^=]+)");
	
	public static String extract(String url)
	{
		if(url == null)
		{
			return null;
		}
		
		if(WRAPPED_URL_PATTERN.matcher(url).matches())
		{
			url = url.substring(5, url.length() - 6);
		}
		
		if(URL_PATTERN.matcher(url).matches())
		{
			return url;
		}
		
		return null;
	}
	
	public static Map<String, String> queryToMap(String query)
	{
		Map<String, String> result = new HashMap<String, String>();
		
		for(String parameter : query.split(Util.QUERY_SPLIT))
		{
			Matcher matcher = PARAMETER_PATTERN.matcher(parameter);
			
			if(matcher.matches())
			{
				result.put(matcher.group(1), matcher.group(2));
			}
		}
		
		return result;
	}
	
	public static String formatDuration(long duration)
	{
		long seconds = duration % 60;
		long minutes = (duration % 3600) / 60;
		long hours = (duration % 86400) / 3600;
		long days = duration / 86400;
		
		if(days > 0)
		{
			return String.format("%d:%02d:%02d:%02d", days, hours, minutes, seconds);
		}
		else if(hours > 0)
		{
			return String.format("%d:%02d:%02d", hours, minutes, seconds);
		}
		
		return String.format("%02d:%02d", minutes, seconds);
	}	
	
	public static String repeat(CharSequence sequence, int repetitions)
	{
		StringBuilder builder = new StringBuilder();
		
		for(int x = 0; x < repetitions; x++)
		{
			builder.append(sequence);
		}
		
		return builder.toString();
	}
	
	public static CloseableHttpClient httpClient()
	{
		return Util.httpClient(CookieSpecs.STANDARD_STRICT);
	}
	
	public static CloseableHttpClient httpClient(String cookieSpec)
	{
		RequestConfig config = RequestConfig.custom()
				.setConnectionRequestTimeout(5000)
				.setCookieSpec(cookieSpec)
				.build();
		CloseableHttpClient client = HttpClientBuilder.create()
				.setUserAgent(TS3Bot.USER_AGENT)
				.setDefaultRequestConfig(config)
				.build();
		return client;
	}
	
	public static File createFile(String fileName) throws IOException
	{
		File file = new File(fileName);
		
		if(!Files.exists(file.toPath()))
		{
			file.createNewFile();
		}
		
		return file;
	}
	
	public static Client clientByUsername(String username)
	{
		for(Client client : TS3Bot.getInstance().getApi().getClients())
		{
			if(username.equalsIgnoreCase(client.getNickname()))
			{
				return client;
			}
		}
		
		return null;
	}
	
	public static Date today()
	{
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}
	
	public static Date tomorrow()
	{
		return Date.from(today().toInstant().plus(1, ChronoUnit.DAYS));
	}
	
	public static <T> T readJsonFile(File file, ObjectMapper objectMapper, TypeReference<T> typeReference, Supplier<T> newInstance) throws IOException
	{
		String contents = Files.readString(file.toPath());
		
		if(contents.isEmpty())
		{
			return newInstance.get();
		}
		
		return objectMapper.readValue(contents, typeReference);
	}
}
