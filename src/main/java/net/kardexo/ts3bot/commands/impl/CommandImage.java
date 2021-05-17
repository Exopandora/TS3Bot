package net.kardexo.ts3bot.commands.impl;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.kardexo.ts3bot.commands.CommandSource;
import net.kardexo.ts3bot.commands.Commands;
import net.kardexo.ts3bot.util.Util;

public class CommandImage
{
	private static final double SQRT_CHARS_PER_PIXEL = Math.sqrt(27D);
	private static final double MAX_CHARS = 8192D;
	private static final double WIDTH_CORRECTION = 1.7D;
	private static final String PIXEL = "\u2588";
	private static final SimpleCommandExceptionType INVALID_URL = new SimpleCommandExceptionType(new LiteralMessage("Invalid URL"));
	private static final SimpleCommandExceptionType ERROR_PROCESSING_IMAGE = new SimpleCommandExceptionType(new LiteralMessage("Error processing image"));
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("image")
				.then(Commands.argument("url", StringArgumentType.greedyString())
						.executes(context -> image(context, Util.extract(StringArgumentType.getString(context, "url"))))));
	}
	
	private static int image(CommandContext<CommandSource> context, String url) throws CommandSyntaxException
	{
		if(url == null)
		{
			throw INVALID_URL.create();
		}
		
		try
		{
			BufferedImage image = ImageIO.read(new URL(url));
			
			double width = image.getWidth() * WIDTH_CORRECTION;
			double height = image.getHeight();
			double scale = Math.sqrt(MAX_CHARS - height) / (SQRT_CHARS_PER_PIXEL * Math.sqrt(height) * Math.sqrt(width));
			
			BufferedImage scaled = new BufferedImage((int) (scale * width), (int) (scale * height), BufferedImage.TYPE_INT_RGB);
			
			Graphics2D graphics = (Graphics2D) scaled.getGraphics();
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
			
			context.getSource().sendFeedback(builder.toString());
			return (int) (width * height);
		}
		catch(Exception e)
		{
			throw ERROR_PROCESSING_IMAGE.create();
		}
	}
}
