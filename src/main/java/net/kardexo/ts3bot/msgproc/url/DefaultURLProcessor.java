package net.kardexo.ts3bot.msgproc.url;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.imageio.ImageIO;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;

import com.fasterxml.jackson.databind.JsonNode;

import net.kardexo.ts3bot.TS3Bot;
import net.kardexo.ts3bot.util.Util;

public class DefaultURLProcessor implements IURLProcessor
{
	private static final URI API_URL = URI.create("https://api.imagga.com/v2/tags");
	private static final String MIME_TYPE_TEXT_HTML = "text/html";
	private static final String MIME_TYPE_IMAGE = "image\\/.+";
	private static final double SQRT_CHARS_PER_PIXEL = Math.sqrt(27D);
	private static final double MAX_CHARS = 8192D;
	private static final double WIDTH_CORRECTION = 1.7D;
	private static final String PIXEL = "\u2588";
	
	@Override
	public String process(String url)
	{
		return this.process(url, CookieSpecs.STANDARD_STRICT);
	}
	
	public String process(String url, String cookieSpec)
	{
		try(CloseableHttpClient client = Util.httpClient(cookieSpec))
		{
			List<String> contentTypes = new ArrayList<String>();
			HttpGet httpGet = new HttpGet(URI.create(url));
			httpGet.setHeader("Accept", MIME_TYPE_TEXT_HTML);
			httpGet.setHeader("Accept-Charset", StandardCharsets.UTF_8.toString());
			
			try(CloseableHttpResponse response = client.execute(httpGet))
			{
				String[] contentType = Objects.requireNonNullElse(response.getEntity().getContentType().getValue(), "").split(";");
				Arrays.stream(contentType).map(String::trim).collect(Collectors.toCollection(() -> contentTypes));
				
				if(contentTypes.contains(MIME_TYPE_TEXT_HTML))
				{
					return Jsoup.parse(EntityUtils.toString(response.getEntity())).getElementsByTag("title").first().text();
				}
				else if(contentTypes.stream().anyMatch(string -> string.matches(MIME_TYPE_IMAGE)))
				{
					String image = this.readImage(response.getEntity().getContent());
					
					if(image != null)
					{
						return image;
					}
					
					image = this.tagImage(client, httpGet, url);
					
					if(image != null)
					{
						return image;
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			String host = new URI(url).getHost();
			
			if(host != null)
			{
				return host.replaceAll("www\\.", "");
			}
			
			return host;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	public boolean isApplicable(String url)
	{
		return true;
	}
	
	private String readImage(InputStream input) throws IOException
	{
		BufferedImage image = ImageIO.read(input);
		
		if(image == null)
		{
			return null;
		}
		
		double width = image.getWidth() * WIDTH_CORRECTION;
		double height = image.getHeight();
		double scale = Math.sqrt(MAX_CHARS - height) / (SQRT_CHARS_PER_PIXEL * Math.sqrt(height) * Math.sqrt(width));
		
		BufferedImage scaled = new BufferedImage((int) (scale * width), (int) (scale * height), BufferedImage.TYPE_INT_RGB);
		
		Graphics2D graphics = (Graphics2D) scaled.getGraphics();
		graphics.setPaint(Color.WHITE);
		graphics.fillRect(0, 0, scaled.getWidth(), scaled.getHeight());
		graphics.scale(scale * WIDTH_CORRECTION, scale);
		graphics.drawImage(image, 0, 0, null);
		graphics.dispose();
		
		StringBuilder builder = new StringBuilder();
		int[] pixels = new int[scaled.getWidth() * scaled.getHeight() * 3];
		scaled.getData().getPixels(0, 0, scaled.getWidth(), scaled.getHeight(), pixels);
		
		for(int y = 0; y < scaled.getHeight(); y++)
		{
			builder.append("\n");
			
			for(int x = 0; x < scaled.getWidth(); x++)
			{
				int offset = 3 * (y * scaled.getWidth() + x);
				builder.append("[color=#" + String.format("%02X%02X%02X", pixels[offset], pixels[offset + 1], pixels[offset + 2]) + "]" + PIXEL + "[/color]");
			}
		}
		
		return builder.toString();
	}
	
	private String tagImage(CloseableHttpClient client, HttpGet httpGet, String url) throws URISyntaxException, ClientProtocolException, IOException
	{
		httpGet.setURI(new URIBuilder(API_URL).addParameter("image_url", url).build());
		httpGet.setHeader("Authorization", "Basic " + TS3Bot.getInstance().getApiKeyManager().requestKey(TS3Bot.API_KEY_IMAGGA));
		
		try(CloseableHttpResponse response = client.execute(httpGet))
		{
			JsonNode node = TS3Bot.getInstance().getObjectMapper().readTree(response.getEntity().getContent()).path("result").path("tags");
			
			if(!node.isMissingNode())
			{
				return StreamSupport.stream(node.spliterator(), false)
						.sorted((a, b) -> Double.compare(a.path("confidence").asDouble(), b.path("confidence").asDouble()))
						.limit(10)
						.map(tag -> tag.path("tag").path("en").asText())
						.collect(Collectors.joining(", ", "Image tags: \"", "\""));
			}
		}
		
		return null;
	}
}
