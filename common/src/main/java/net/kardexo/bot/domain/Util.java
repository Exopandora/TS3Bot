package net.kardexo.bot.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.kardexo.bot.domain.api.IChannel;
import net.kardexo.bot.domain.api.IClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class Util
{
	public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	public static final Pattern URL_PATTERN = Pattern.compile("https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
	public static final Pattern WRAPPED_URL_PATTERN = Pattern.compile("\\[URL].*\\[/URL]");
	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.246";
	
	public static String extractURL(String url)
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
		
		for(String parameter : query.split("[?&]"))
		{
			String[] matcher = parameter.split("=");
			result.put(matcher[0], URLDecoder.decode(matcher[1], StandardCharsets.UTF_8));
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
		return HttpClientBuilder.create()
			.setUserAgent(USER_AGENT)
			.setDefaultRequestConfig(config)
			.build();
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
	
	public static List<String> getClientNamesInChannel(IChannel channel)
	{
		return channel.getClients().stream()
			.map(IClient::getName)
			.toList();
	}
	
	public static String urlEncode(String summonerName)
	{
		return URLEncoder.encode(summonerName, StandardCharsets.UTF_8);
	}
}
